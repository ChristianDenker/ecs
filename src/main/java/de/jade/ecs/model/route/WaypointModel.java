package de.jade.ecs.model.route;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class WaypointModel {
	private SimpleStringProperty name = new SimpleStringProperty();
	private SimpleDoubleProperty lat = new SimpleDoubleProperty();
	private SimpleDoubleProperty lon = new SimpleDoubleProperty();

	public WaypointModel() {
		super();
	}

	public WaypointModel(String name, Double lat, Double lon) {
		super();
		this.name = new SimpleStringProperty(name);
		this.lat = new SimpleDoubleProperty(lat);
		this.lon = new SimpleDoubleProperty(lon);
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public Double getLat() {
		return lat.get();
	}

	public void setLat(Double lat) {
		this.lat.set(lat);
	}

	public Double getLon() {
		return lon.get();
	}

	public void setLon(Double lon) {
		this.lon.set(lon);
	}

}
