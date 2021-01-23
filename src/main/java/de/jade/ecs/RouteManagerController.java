package de.jade.ecs;

import java.util.Iterator;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.GeodeticCalculator;

import de.jade.ecs.map.ChartViewer;
import de.jade.ecs.map.RoutePainter;
import de.jade.ecs.model.route.RouteModel;
import de.jade.ecs.model.route.WaypointModel;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class RouteManagerController {

	public static RouteManagerController INSTANCE = null;

	public ChartViewer chartViewer = null;

	public boolean isEditing = false;

	public RouteModel routeToEdit = null;

	public RoutePainter routeToEditPainter = null;

	public GeodeticCalculator geodeticCalculator = GeodeticCalculator.create(CommonCRS.WGS84.geographic());

	@FXML
	private ListView<RouteModel> routeListView = null;

	@FXML
	public TextField routeName = null;

	@FXML
	private Button newRouteButton = null;

	@FXML
	private Button saveRouteButton = null;

	@FXML
	private Button cancelRouteButton = null;

	@FXML
	private StackPane routeStackPane = null;

	@FXML
	private GridPane routeOverviewGrid = null;

	@FXML
	public GridPane routeCreateGrid = null;

	@FXML
	public TableView<WaypointModel> waypointTableView = null;

	@FXML
	private TableColumn<String, Double> latTableColumn = null;

	@FXML
	private TableColumn<String, Double> lonTableColumn = null;

	@FXML
	private TableColumn<String, Double> radiusTableColumn = null;

	/**
	 * Ctor
	 */
	public RouteManagerController() {
		INSTANCE = this;
	}

	@FXML
	private void initialize() {
		System.out.println(getClass().getName() + " initialized.");

		routeOverviewGrid.toFront();

		setupRouteOverview();

		setupRouteCreateMenu();

		/** style **/
		JMetro jMetro = new JMetro(Style.DARK);
		jMetro.setAutomaticallyColorPanes(true);
		jMetro.setParent(routeName);

	}

	private void setupRouteCreateMenu() {
		saveRouteButton.setOnAction((ActionEvent evt) -> {
			routeOverviewGrid.toFront();

			routeToEdit.setName(routeName.getText());
			if (!ECS_UIController.INSTANCE.getSettings().getRouteModelList().contains(routeToEdit)) {
				ECS_UIController.INSTANCE.getSettings().getRouteModelList().add(routeToEdit);
			}
			ECS_UIController.INSTANCE.saveSettings();

			/**
			 * remove WPTs from JXMapViewer and set waypointCanvas-Reference in
			 * WaypointModel to null
			 **/
			routeToEditPainter.getTrack().forEach(wpModel -> {
				chartViewer.getJXMapViewer().remove(wpModel.waypointCanvas);
				wpModel.waypointCanvas = null;
			});

			chartViewer.removePainter(routeToEditPainter);

			routeListView.refresh();

			isEditing = false;
		});

		cancelRouteButton.setOnAction((ActionEvent evt) -> {
			routeOverviewGrid.toFront();
			/**
			 * remove WPTs from JXMapViewer and set waypointCanvas-Reference in
			 * WaypointModel to null
			 **/
			routeToEditPainter.getTrack().forEach(wpModel -> {
				chartViewer.getJXMapViewer().remove(wpModel.waypointCanvas);
				wpModel.waypointCanvas = null;
			});
			chartViewer.removePainter(routeToEditPainter);
			isEditing = false;
		});

		latTableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		latTableColumn.addEventHandler(TableColumn.editCommitEvent(),
				new EventHandler<CellEditEvent<String, Double>>() {
					/**
					 * This updates manual inputs entered into waypointTableView
					 */
					@Override
					public void handle(CellEditEvent<String, Double> event) {
						WaypointModel wpModel = (WaypointModel) RouteManagerController.INSTANCE.waypointTableView
								.getItems().get(event.getTablePosition().getRow());
						if (wpModel != null) {
							wpModel.setLat(event.getNewValue());
							wpModel.updateTransitionPoints(
									RouteManagerController.INSTANCE.waypointTableView.getItems());
							chartViewer.getJXMapViewer().updateUI();
						}
					}
				});

		lonTableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		lonTableColumn.addEventHandler(TableColumn.editCommitEvent(),
				new EventHandler<CellEditEvent<String, Double>>() {
					/**
					 * This updates manual inputs entered into waypointTableView
					 */
					@Override
					public void handle(CellEditEvent<String, Double> event) {
						WaypointModel wpModel = (WaypointModel) RouteManagerController.INSTANCE.waypointTableView
								.getItems().get(event.getTablePosition().getRow());
						if (wpModel != null) {
							wpModel.setLon(event.getNewValue());
							wpModel.updateTransitionPoints(
									RouteManagerController.INSTANCE.waypointTableView.getItems());
							chartViewer.getJXMapViewer().updateUI();
						}
					}
				});

		radiusTableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		radiusTableColumn.addEventHandler(TableColumn.editCommitEvent(),
				new EventHandler<CellEditEvent<String, Double>>() {
					/**
					 * This updates manual inputs entered into waypointTableView
					 */
					@Override
					public void handle(CellEditEvent<String, Double> event) {
						WaypointModel wpModel = (WaypointModel) RouteManagerController.INSTANCE.waypointTableView
								.getItems().get(event.getTablePosition().getRow());
						if (wpModel != null) {
							wpModel.setTurnRadius_meters(event.getNewValue());
							wpModel.updateTransitionPoints(
									RouteManagerController.INSTANCE.waypointTableView.getItems());
							chartViewer.getJXMapViewer().updateUI();
						}
					}
				});

//		ObservableList<WaypointModel> waypointList = FXCollections.observableArrayList();
//		waypointList.add(new WaypointModel("WP1", 54.0, 8.0));
//		waypointTableView.setItems(waypointList);
//		waypointTableView.setEditable(true);
	}

	private void setupRouteOverview() {

		/** on newRouteButton clicked **/
		newRouteButton.setOnAction((ActionEvent evt) -> {
			routeCreateGrid.toFront();

			routeName.setText("");
			routeToEdit = new RouteModel();

			setupWaypoints();

			isEditing = true;
		});

		/** setup route ListView **/
		routeListView.setCellFactory(new Callback<ListView<RouteModel>, ListCell<RouteModel>>() {
			@Override
			public ListCell<RouteModel> call(ListView<RouteModel> param) {
				return new RouteListCell();
			}
		});

		routeListView.setItems(ECS_UIController.INSTANCE.getSettings().getRouteModelList());
	}

	public void setupWaypoints() {
		waypointTableView.setItems(routeToEdit.getWaypointList());

		routeToEditPainter = new RoutePainter(routeToEdit.getWaypointList());
		chartViewer.addPainter(routeToEditPainter);

		waypointTableView.getItems().addListener(new ListChangeListener<WaypointModel>() {

			/** update Route on RoutePainter **/

			@Override
			public void onChanged(Change<? extends WaypointModel> c) {
				routeToEditPainter.getTrack().clear();
				routeToEditPainter.getTrack().addAll(c.getList());
				chartViewer.getJXMapViewer().updateUI();
			}
		});

		/** setup transition points **/
		{
			if (waypointTableView.getItems().size() > 2) {
				CircularFifoQueue<WaypointModel> fifoQueue = new CircularFifoQueue<>(3);
				Iterator<WaypointModel> wpIterator = waypointTableView.getItems().iterator();
				fifoQueue.add(wpIterator.next());
				fifoQueue.add(wpIterator.next());

				while (wpIterator.hasNext()) {
					fifoQueue.add(wpIterator.next());
					fifoQueue.get(1).updateTransitionPoints(fifoQueue.get(0), fifoQueue.get(2));
				}
			}
		}

		chartViewer.getJXMapViewer().updateUI();
	}
}
