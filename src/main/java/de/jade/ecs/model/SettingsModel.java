package de.jade.ecs.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.jade.ecs.model.route.RouteModel;
import de.jade.ecs.model.route.WaypointModel;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SettingsModel {

	private SimpleListProperty<RouteModel> routeModelList = new SimpleListProperty<>();

	public SettingsModel() {
		super();
		if(routeModelList.get() == null) {
			routeModelList.set(FXCollections.observableArrayList());
		}
	}

	public SettingsModel(ObservableList<RouteModel> routeModelList) {
		super();
		this.routeModelList = new SimpleListProperty<>(routeModelList);
	}

	@JsonIgnore
	public ObservableList<RouteModel> getRouteModelList() {
		return routeModelList.get();
	}

	@JsonIgnore
	public void setRouteModelList(ObservableList<RouteModel> routeModelList) {
		this.routeModelList.set(routeModelList);
	}

	@JsonProperty(value = "routeModelList")
	public ArrayList<RouteModel> getRouteModelArrayList() {

		ArrayList<RouteModel> list = new ArrayList<RouteModel>();
		if (routeModelList.get() != null && !routeModelList.get().isEmpty()) {
			list.addAll(routeModelList);
		}
		return list;
	}
	@JsonProperty(value = "routeModelList")
	public void setRouteModelList(ArrayList<RouteModel> waypointList) {
		ObservableList<RouteModel> obsvWpList = FXCollections.observableArrayList();
		obsvWpList.addAll(waypointList);
		this.routeModelList.set(obsvWpList);
	}

	public static void main(String[] args) {
		ObjectMapper objectMapper = new ObjectMapper();
//		objectMapper.enableDefaultTyping();
		SimpleListProperty<RouteModel> list = new SimpleListProperty<>(FXCollections.observableArrayList());
		ObservableList<WaypointModel> waypointList = FXCollections.observableArrayList();
		waypointList.add(new WaypointModel("WP1",54.0,8.0));
		list.add(new RouteModel("route1", waypointList));
		SettingsModel settingsModel = new SettingsModel(list);
		try {
			objectMapper.writeValue(new File("settingsModel.json"), settingsModel);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			SettingsModel read = objectMapper.readValue(new File("settingsModel.json"), SettingsModel.class);

			
			System.out.println(read.getRouteModelList().size());
			

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
