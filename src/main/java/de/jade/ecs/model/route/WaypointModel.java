package de.jade.ecs.model.route;

import org.apache.sis.referencing.GeodeticCalculator;
import org.opengis.geometry.DirectPosition;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.jade.ecs.RouteManagerController;
import de.jade.ecs.map.WaypointCanvas;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

public class WaypointModel {
	private SimpleStringProperty name = new SimpleStringProperty();
	private SimpleDoubleProperty lat = new SimpleDoubleProperty();
	private SimpleDoubleProperty lon = new SimpleDoubleProperty();
	private SailingMode sailingMode = SailingMode.LOXODROME;
	private SimpleDoubleProperty turnRadius_meters = new SimpleDoubleProperty();

	@JsonIgnore
	public DirectPosition transitionPointToPredecessor = null;
	@JsonIgnore
	public DirectPosition transitionPointToSuccessor = null;
	@JsonIgnore
	public DirectPosition turningCircleCenter = null;

	@JsonIgnore
	public WaypointCanvas waypointCanvas = null;
	@JsonIgnore
	public double circleCenterBearingToPointToPredecessor;
	@JsonIgnore
	public double circleCenterBearingToPointToSuccessor;

	public WaypointModel() {
		super();
	}

	public WaypointModel(String name, Double lat, Double lon) {
		super();
		this.name = new SimpleStringProperty(name);
		this.lat = new SimpleDoubleProperty(lat);
		this.lon = new SimpleDoubleProperty(lon);
		turnRadius_meters.set(300);
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

	public SailingMode getSailingMode() {
		return sailingMode;
	}

	public void setSailingMode(SailingMode sailingMode) {
		this.sailingMode = sailingMode;
	}

	public Double getTurnRadius_meters() {
		return turnRadius_meters.get();
	}

	public void setTurnRadius_meters(Double turnRadius_meters) {
		this.turnRadius_meters.set(turnRadius_meters);
	}

	@JsonIgnore
	public SimpleDoubleProperty getLatSimpleDoubleProperty() {
		return lat;
	}

	@JsonIgnore
	public SimpleDoubleProperty getLonSimpleDoubleProperty() {
		return lon;
	}

	@JsonIgnore
	public void updateTransitionPoints(WaypointModel predecessorWpModel, WaypointModel successorWpModel) {
		if (predecessorWpModel == null || successorWpModel == null) {
			return;
		}

		GeodeticCalculator geoCalc = RouteManagerController.INSTANCE.geodeticCalculator;
		
		/** firstDirection **/
		geoCalc.setStartGeographicPoint(predecessorWpModel.getLat(), predecessorWpModel.getLon());
		geoCalc.setEndGeographicPoint(this.getLat(), this.getLon());
		double firstDirection = -1;
		if (predecessorWpModel.getSailingMode() == SailingMode.LOXODROME) {
			firstDirection = geoCalc.getConstantAzimuth();
		}
		if (predecessorWpModel.getSailingMode() == SailingMode.ORTHODROME) {
			firstDirection = geoCalc.getEndingAzimuth();
		}
		firstDirection = (firstDirection + 360) % 360;
		
		/** firstDirectionInverse **/
		double firstDirectionInverse = (firstDirection + 180) % 360;
		
		/** secondDirection **/
		geoCalc.setStartGeographicPoint(this.getLat(), this.getLon());
		geoCalc.setEndGeographicPoint(successorWpModel.getLat(), successorWpModel.getLon());
		double secondDirection = -1;
		if (this.getSailingMode() == SailingMode.LOXODROME) {
			secondDirection = geoCalc.getConstantAzimuth();
		}
		if (this.getSailingMode() == SailingMode.ORTHODROME) {
			secondDirection = geoCalc.getStartingAzimuth();
		}
		secondDirection = (secondDirection + 360) % 360;

		double minAngleBetweenDirections = getDifference(firstDirectionInverse, secondDirection);

		double lengthWPTtoCircleCenter = this.getTurnRadius_meters()
				/ Math.sin(Math.toRadians(minAngleBetweenDirections / 2));
		double distanceToTransitionPoints = Math.abs(this.getTurnRadius_meters() / Math.tan(Math.toRadians(minAngleBetweenDirections / 2)));

		/** calc transitionPointToPredecessor **/
		geoCalc.setStartGeographicPoint(this.getLat(), this.getLon());
		geoCalc.setStartingAzimuth(firstDirectionInverse);
		geoCalc.setGeodesicDistance(distanceToTransitionPoints);
		transitionPointToPredecessor = geoCalc.getEndPoint();

		/** calc transitionPointToSuccessor **/
		geoCalc.setStartGeographicPoint(this.getLat(), this.getLon());
		geoCalc.setStartingAzimuth(secondDirection);
		geoCalc.setGeodesicDistance(distanceToTransitionPoints);
		transitionPointToSuccessor = geoCalc.getEndPoint();

		/**
		 * For explanation see JMoravitz' & Teoc's posts here:
		 * https://math.stackexchange.com/questions/1088902/what-is-the-radius-of-a-circle-given-two-points-and-the-center-of-the-circle-is
		 **/

		/** calc turningCircleCenter **/
		double direction = getDifference(firstDirectionInverse, secondDirection) / 2;
		double circleBearing = isBearing1LeftOfBearing2(firstDirection, secondDirection)
				? firstDirectionInverse + direction
				: secondDirection + direction;

		geoCalc.setStartGeographicPoint(this.getLat(), this.getLon());
		geoCalc.setStartingAzimuth(circleBearing);
		geoCalc.setGeodesicDistance(lengthWPTtoCircleCenter);
		turningCircleCenter = geoCalc.getEndPoint();
		
		/** calc circleCenterBearingToPointToPredecessor **/
		geoCalc.setStartPoint(turningCircleCenter);
		geoCalc.setEndPoint(transitionPointToPredecessor);
		circleCenterBearingToPointToPredecessor = (geoCalc.getStartingAzimuth() + 360) % 360;

		/** calc circleCenterBearingToPointToSuccessor **/
		geoCalc.setStartPoint(turningCircleCenter);
		geoCalc.setEndPoint(transitionPointToSuccessor);
		circleCenterBearingToPointToSuccessor = (geoCalc.getStartingAzimuth() + 360) % 360;
	}

	/** updateTransitionPoints
	 * 
	 * updates transition points of this' and waypointModel predecessors and successors
	 * 
	 * @param wayPointModelList
	 */
	@JsonIgnore
	public void updateTransitionPoints(ObservableList<WaypointModel> wayPointModelList){
		int waypointModelIndex = wayPointModelList.indexOf(this);

		int predecessorIndex = (waypointModelIndex == 0) ? -1 : waypointModelIndex - 1; // can result in -2
		int pre_predecessorIndex = (predecessorIndex == 0) ? -1 : predecessorIndex - 1;
		int successorIndex = (waypointModelIndex == wayPointModelList
				.size() - 1) ? -1 : waypointModelIndex + 1;
		int post_successorIndex = (successorIndex == wayPointModelList.size() - 1) ? -1 : successorIndex + 1;

		WaypointModel pre_predecessorWpModel = null;
		if (pre_predecessorIndex > -1) {
			pre_predecessorWpModel = wayPointModelList
					.get(pre_predecessorIndex);
		}
		WaypointModel predecessorWpModel = null;
		if (predecessorIndex > -1) {
			predecessorWpModel = wayPointModelList
					.get(predecessorIndex);
		}
		WaypointModel successorWpModel = null;
		if (successorIndex > -1) {
			successorWpModel = wayPointModelList.get(successorIndex);
		}
		WaypointModel post_successorWpModel = null;
		if (post_successorIndex > -1) {
			post_successorWpModel = wayPointModelList
					.get(post_successorIndex);
		}
		
		this.updateTransitionPoints(predecessorWpModel, successorWpModel);
		if (predecessorWpModel != null)
			predecessorWpModel.updateTransitionPoints(pre_predecessorWpModel, this);
		if (successorWpModel != null)
			successorWpModel.updateTransitionPoints(this, post_successorWpModel);
		
	}
	
	/**
	 * returns cartesian coordinates from given polar coordinates
	 * 
	 * @param r
	 * @param theta
	 * @return new double[] { x, y }
	 *
	 */
	@JsonIgnore
	public static double[] polarToCartesian(double r, double theta) {
		double x = r * Math.cos(Math.toRadians(theta));
		double y = r * Math.sin(Math.toRadians(theta));
		return new double[] { x, y };
	}

	/** returns difference between two bearings ranging 0-360 each
	 * 
	 * @param bearing1 - value [0 - 360]
	 * @param bearing2 - value [0 - 360]
	 * @return difference between bearing1 and bearing2. E.g. getDifference(350°,20°) returns 30°.
	 */
	@JsonIgnore
	public static double getDifference(double bearing1, double bearing2) {
		return Math.min((bearing1 - bearing2) < 0 ? bearing1 - bearing2 + 360 : bearing1 - bearing2, (bearing2 - bearing1) < 0 ? bearing2 - bearing1 + 360 : bearing2 - bearing1);
	}

	/** returns a, iff a is closer to c, otherwise returns b
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return a or b
	 */
	@JsonIgnore
	public static double closerToC(double a, double b, double c) {
		if (Math.abs(c - a) < Math.abs(c - b))
			return a;
		else
			return b;
	}

	/** returns true, iff bearing 2 is left/port of bearing1(heading) 
	 * 
	 * @param bearing1 - [0-360]
	 * @param bearing2 - [0-360]
	 * @return true, iff bearing1 is left/portside of bearing2, otherwise false
	 */
	@JsonIgnore
	public static boolean isBearing1LeftOfBearing2(double bearing1, double bearing2) {
		if (Math.signum(((bearing1 - bearing2 + 540) % 360) - 180) > 0) {
			return true;
		} else {
			return false;
		}
	}

}
