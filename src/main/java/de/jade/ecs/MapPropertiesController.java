package de.jade.ecs;

import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.wms.WMSTileFactory;

import de.jade.ecs.util.WMSServiceImagePNG;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.StackPane;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class MapPropertiesController {

	@FXML
	private StackPane mapPropertiesTab = null;
	
	@FXML
	private ChoiceBox<String> useMapServiceChoiceBox = null;
	
	public static MapPropertiesController INSTANCE = null;


	/**
	 * Ctor
	 */
	public MapPropertiesController() {
		INSTANCE = this;
	}

	@FXML
	private void initialize() {
		System.out.println(getClass().getName() + " initialized.");

		setupMapPropertiesOverviewGrid();

		/** style **/
		JMetro jMetro = new JMetro(Style.DARK);
		jMetro.setAutomaticallyColorPanes(true);
//		jMetro.setParent(losRadiusTextField);

	}

	private void setupMapPropertiesOverviewGrid() {
		ObservableList<String> list = FXCollections.observableArrayList();
		list.add("seamap");
		list.add("landmap");
		SimpleListProperty<String> mapServiceList = new SimpleListProperty<>(list);
		
		useMapServiceChoiceBox.setItems(mapServiceList);
//		routeToTrackChoiceBox.setConverter(new StringConverter<RouteModel>() {
//
//			@Override
//			public String toString(RouteModel routeModel) {
//				return routeModel.getName();
//			}
//
//			@Override
//			public RouteModel fromString(String string) {
//				return null;
//			}
//
//		});
		useMapServiceChoiceBox.setOnAction(  (ActionEvent evt) -> {
			String str = useMapServiceChoiceBox.getValue();
			
			if(str.contains("seamap")) {

				WMSServiceImagePNG wms = new WMSServiceImagePNG("https://wms.sevencs.com/?SERVICE=WMS&CSBOOL=183&CSVALUE=10,5,15,10,1,2,1,,,,1&", "ENC");//"OSM-WMS");
				WMSTileFactory tileFactory = new WMSTileFactory(wms);
				tileFactory.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36");
				
				ECS_UIController.INSTANCE.chartViewer.getJXMapViewer().setTileFactory(tileFactory);
			} else {
				// Create a TileFactoryInfo for OpenStreetMap
				TileFactoryInfo info = new OSMTileFactoryInfo();
				DefaultTileFactory tileFactory = new DefaultTileFactory(info);
				ECS_UIController.INSTANCE.chartViewer.getJXMapViewer().setTileFactory(tileFactory);
			}
			System.out.println(str);
		});
		

	}

}
