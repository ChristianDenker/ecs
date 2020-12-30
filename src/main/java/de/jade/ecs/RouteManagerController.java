package de.jade.ecs;

import de.jade.ecs.map.ChartViewer;
import de.jade.ecs.routeModel.WaypointModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

	@FXML
	private ListView<String> routeListView = null;
	
	@FXML
	private TextField routeName = null;
	
	@FXML
	private Button newRouteButton = null;
	
	@FXML
	private Button saveRouteButton = null;
	
	@FXML
	private StackPane routeStackPane = null;
	
	@FXML
	private GridPane routeOverviewGrid = null;
	
	@FXML
	private GridPane routeCreateGrid = null;
	
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
		routeListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {

			@Override
			public ListCell<String> call(ListView<String> param) {
				return new RouteListCell();
			}

		});
		
		routeListView.getItems().add("Bla");
		
		routeOverviewGrid.toFront();
		
		/** on newRouteButton clicked **/
		newRouteButton.setOnAction((ActionEvent evt) -> {
			routeCreateGrid.toFront();
			routeName.setText("");
			ObservableList<WaypointModel> waypointList = FXCollections.observableArrayList();
			waypointTableView.setItems(waypointList);
			isEditing = true;
			
		});
		
		saveRouteButton.setOnAction((ActionEvent evt) -> {
			routeOverviewGrid.toFront();
			isEditing = false;
		});
		
		latTableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		lonTableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		
		ObservableList<WaypointModel> waypointList = FXCollections.observableArrayList();
		waypointList.add(new WaypointModel("WP1", 54.0, 8.0));
		waypointTableView.setItems(waypointList);
		waypointTableView.setEditable(true);

		/** style **/
		JMetro jMetro = new JMetro(Style.DARK);
		jMetro.setAutomaticallyColorPanes(true);
		jMetro.setParent(routeName);

	}
}
