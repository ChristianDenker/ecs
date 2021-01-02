package de.jade.ecs;

import java.util.ArrayList;

import org.jxmapviewer.viewer.GeoPosition;

import de.jade.ecs.map.ChartViewer;
import de.jade.ecs.map.RoutePainter;
import de.jade.ecs.model.route.RouteModel;
import de.jade.ecs.model.route.WaypointModel;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
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
			if(!ECS_UIController.INSTANCE.getSettings().getRouteModelList().contains(routeToEdit)) {
				ECS_UIController.INSTANCE.getSettings().getRouteModelList().add(routeToEdit);
			}
			ECS_UIController.INSTANCE.saveSettings();
			chartViewer.removePainter(routeToEditPainter);
			
			routeListView.refresh();

			isEditing = false;
		});

		cancelRouteButton.setOnAction((ActionEvent evt) -> {
			routeOverviewGrid.toFront();
			chartViewer.removePainter(routeToEditPainter);
			isEditing = false;
		});

		latTableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		lonTableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));

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
		
		ArrayList<GeoPosition> geoPositions = new ArrayList<GeoPosition>();
		routeToEdit.getWaypointList().forEach((wpModel) -> geoPositions.add(new GeoPosition(wpModel.getLat(), wpModel.getLon())) );
		
		routeToEditPainter = new RoutePainter(geoPositions);
		chartViewer.addPainter(routeToEditPainter);
		
		waypointTableView.getItems().addListener(new ListChangeListener<WaypointModel>() {

			/** update Route on RoutePainter **/
			
			@Override
			public void onChanged(Change<? extends WaypointModel> c) {
				routeToEditPainter.getTrack().clear();
				ArrayList<GeoPosition> geoPositions = new ArrayList<GeoPosition>();
				c.getList().forEach((wpModel) -> geoPositions.add(new GeoPosition(wpModel.getLat(), wpModel.getLon())) );
				routeToEditPainter.getTrack().addAll(geoPositions);
				chartViewer.getJXMapViewer().updateUI();
			}
		});
		
		chartViewer.getJXMapViewer().updateUI();
	}
}
