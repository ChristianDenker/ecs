package de.jade.ecs.model.route;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RouteModel {

	private SimpleStringProperty name = new SimpleStringProperty();

	private SimpleListProperty<WaypointModel> waypointList = new SimpleListProperty<>();

	public RouteModel() {
		super();
		ObservableList<WaypointModel> obsvWpList = FXCollections.observableArrayList();
		this.waypointList = new SimpleListProperty<>(obsvWpList);
	}

	public RouteModel(String name, List<WaypointModel> waypointList) {
		super();
		this.name = new SimpleStringProperty(name);

		ObservableList<WaypointModel> obsvWpList = FXCollections.observableArrayList();
		if(waypointList != null)
			obsvWpList.addAll(waypointList);
		this.waypointList = new SimpleListProperty<>(obsvWpList);
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	@JsonIgnore
	public ObservableList<WaypointModel> getWaypointList() {
		return waypointList.get();
	}

	@JsonIgnore
	public void setWaypointList(ObservableList<WaypointModel> waypointList) {
		this.waypointList.set(waypointList);
	}

	@JsonProperty(value = "waypointList")
	public ArrayList<WaypointModel> getWaypointArrayList() {

		ArrayList<WaypointModel> list = new ArrayList<WaypointModel>();
		if (waypointList.get() != null && !waypointList.get().isEmpty()) {
			list.addAll(waypointList);
		}
		return list;
	}
	@JsonProperty(value = "waypointList")
	public void setWaypointList(ArrayList<WaypointModel> waypointList) {
		ObservableList<WaypointModel> obsvWpList = FXCollections.observableArrayList();
		obsvWpList.addAll(waypointList);
		this.waypointList.set(obsvWpList);
	}

}
