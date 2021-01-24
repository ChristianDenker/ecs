package de.jade.ecs;

import java.awt.geom.Point2D;
import java.lang.reflect.Field;

import org.greenrobot.eventbus.EventBus;
import org.scheduler.Scheduler;
import org.scheduler.SchedulerConfiguration;
import org.scheduler.agent.Agent;
import org.scheduler.agent.state.ShipState;

import de.jade.ecs.map.ChartViewer;
import de.jade.ecs.model.route.RouteModel;
import de.jade.ecs.simulation.ContainershipDynamics;
import de.jade.ecs.simulation.LOSTrackingBehaviour;
import de.jade.ecs.simulation.TrackPaintExecution;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class SimulationController {

	@FXML
	private StackPane simulationStackPane = null;
	@FXML
	private Button startButton = null;
	@FXML
	private Button stopButton = null;
	@FXML
	private ChoiceBox<RouteModel> routeToTrackChoiceBox = null;
	@FXML
	private TextField losRadiusTextField = null;

	public static SimulationController INSTANCE = null;

	public ChartViewer chartViewer = null;

	private Scheduler scheduler = null;

	private TrackPaintExecution trackPaintExecution = null;

	/**
	 * Ctor
	 */
	public SimulationController() {
		INSTANCE = this;
	}

	@FXML
	private void initialize() {
		System.out.println(getClass().getName() + " initialized.");

		setupSimulationOverviewGrid();

		/** style **/
		JMetro jMetro = new JMetro(Style.DARK);
		jMetro.setAutomaticallyColorPanes(true);
		jMetro.setParent(losRadiusTextField);

	}

	private void setupSimulationOverviewGrid() {
		routeToTrackChoiceBox.setItems(ECS_UIController.INSTANCE.getSettings().getRouteModelList());
		routeToTrackChoiceBox.setConverter(new StringConverter<RouteModel>() {

			@Override
			public String toString(RouteModel routeModel) {
				return routeModel.getName();
			}

			@Override
			public RouteModel fromString(String string) {
				return null;
			}

		});

		/** allow only number input **/
		losRadiusTextField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d{0,7}([\\.]\\d{0,4})?")) {
					losRadiusTextField.setText(oldValue);
				}
			}
		});

		startButton.setOnAction((ActionEvent evt) -> {
			if (scheduler == null) {

				/** remove previous paint **/
				if (trackPaintExecution != null) {
					chartViewer.removeLinePainter(trackPaintExecution.getTracePainter());
				}

				double freq = 1;
				double simulationSpeed = 1000;
				scheduler = new Scheduler(new SchedulerConfiguration(freq, simulationSpeed));

				ShipState shipStateA = new ShipState(111111111, new Point2D.Double(8, 54), 75, 20);

				ContainershipDynamics dynamics = new ContainershipDynamics(shipStateA);
				dynamics.getContainerCommands().setN_c(50);

				double losRadius = Double.parseDouble(losRadiusTextField.getText());

				LOSTrackingBehaviour trackBehaviour = new LOSTrackingBehaviour(shipStateA, losRadius,
						routeToTrackChoiceBox.getSelectionModel().getSelectedItem());

				trackPaintExecution = new TrackPaintExecution(chartViewer, shipStateA);

				Agent agentA = new Agent(scheduler, dynamics,
						trackBehaviour /* , new NmeaPosReportUdpOutputBehaviour(shipStateA, 2947) */,
						trackPaintExecution);
				scheduler.registerAgent(agentA);

				scheduler.start();

//				try {
//					TimeUnit.SECONDS.sleep(5);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//
//				/** sets the course to 000° **/
//				shipStateA.setHeading_commanded_deg(0);
//
//				while (true) {
//
//					dynamics.getContainerCommands().setN_c(50);
//
//					System.out.println("currentSpeed_kn: " + shipStateA.getSpeed_current_kn());
//					try {
//						TimeUnit.SECONDS.sleep(1);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
			}
		});

		stopButton.setOnAction((ActionEvent evt) -> {
			if (scheduler != null) {
				scheduler.stop();
				scheduler = null;

				/** clear out singleton instance of EvensBus class, to make restart possible **/
				try {
					Field field = EventBus.class.getDeclaredField("defaultInstance");
					field.setAccessible(true);
					field.set(EventBus.class, null);
					field.setAccessible(false);
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
						| IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		});

	}

}
