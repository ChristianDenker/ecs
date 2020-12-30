package de.jade.ecs.routeModel;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

public class RouteModel {
	
	SimpleStringProperty name = new SimpleStringProperty();

	SimpleListProperty<WaypointModel> waypointList = new SimpleListProperty<>();

	public RouteModel() {
		super();
	}
	public RouteModel(String name, ObservableList<WaypointModel> waypointList) {
		super();
		this.name = new SimpleStringProperty(name);
		this.waypointList = new SimpleListProperty<>(waypointList);
	}
	
	
	public String getName() {
		return name.get();
	}
	public void setName(String name) {
		this.name.set(name);
	}
	public ObservableList<WaypointModel> getWaypointList() {
		return waypointList.get();
	}
	public void setWaypointList(ObservableList<WaypointModel> waypointList) {
		this.waypointList.set(waypointList);
	}
	
	
	
}
