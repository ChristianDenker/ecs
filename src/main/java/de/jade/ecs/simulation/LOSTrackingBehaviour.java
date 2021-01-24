package de.jade.ecs.simulation;

import java.util.ArrayList;

import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.GeodeticCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.opengis.geometry.DirectPosition;
import org.scheduler.Tick;
import org.scheduler.agent.Agent;
import org.scheduler.agent.behaviour.ITickExecution;
import org.scheduler.agent.state.ShipState;

import de.jade.ecs.model.route.RouteModel;
import de.jade.ecs.model.route.WaypointModel;

public class LOSTrackingBehaviour implements ITickExecution {

	@SuppressWarnings("unused")
	private Agent agent = null;

	double radius_m = 1000;

	private RouteModel routeModel = null;
	private ShipState shipState = null;

	GeometryFactory geoFactory = new GeometryFactory();
	GeodeticCalculator geoCalc = GeodeticCalculator.create(CommonCRS.WGS84.geographic());

	private LineString cartRouteLineString = null;

	public LOSTrackingBehaviour(ShipState shipState, double losRadius, RouteModel routeModel) {
		this.shipState = shipState;
		this.radius_m = losRadius;
		this.routeModel = routeModel;
		this.cartRouteLineString  = routeToLineString(routeModel);
	}

	private LineString routeToLineString(RouteModel routeModel) {
		ArrayList<Coordinate> coordinateArray = new ArrayList<>();
		geoCalc.setStartGeographicPoint(routeModel.getWaypointList().get(0).getLat(),
				routeModel.getWaypointList().get(0).getLon());
		
		coordinateArray.add(new Coordinate(0,0));
		
		for (int i = 1; i < routeModel.getWaypointList().size(); i++) {
			WaypointModel wpModel = routeModel.getWaypointList().get(i);
			wpModel.updateTransitionPoints(routeModel.getWaypointList());
			
			if (wpModel.transitionPointToPredecessor != null) {
//				geoCalc.setEndGeographicPoint(wpModel.transitionPointToPredecessor.getCoordinate()[0],
//						wpModel.transitionPointToPredecessor.getCoordinate()[1]);
				geoCalc.setEndGeographicPoint(wpModel.getLat(), wpModel.getLon());
				double[] cart = polarToCartesian(geoCalc.getGeodesicDistance(), (geoCalc.getStartingAzimuth() + 360 ) % 360);
				coordinateArray.add(new Coordinate(cart[0], cart[1]));
				
			} else {
				geoCalc.setEndGeographicPoint(wpModel.getLat(), wpModel.getLon());
				double[] cart = polarToCartesian(geoCalc.getGeodesicDistance(), (geoCalc.getStartingAzimuth() + 360 ) % 360);
				coordinateArray.add(new Coordinate(cart[0], cart[1]));
			}

		}
		Coordinate[] arr = coordinateArray.toArray(new Coordinate[coordinateArray.size()]);
		LineString lineString = geoFactory.createLineString(arr);
		return lineString;
	}

	@Override
	public void execute(Tick tick) {
		double lon = shipState.getPoint().getX();
		double lat = shipState.getPoint().getY();

		geoCalc.setStartGeographicPoint(routeModel.getWaypointList().get(0).getLat(),
				routeModel.getWaypointList().get(0).getLon());
		geoCalc.setEndGeographicPoint(lat, lon);
		double[] cart = polarToCartesian(geoCalc.getGeodesicDistance(), (geoCalc.getStartingAzimuth() + 360 ) % 360);
		Geometry point = geoFactory.createPoint(new Coordinate(cart[0],cart[1]));
		point = point.buffer(radius_m);
		
		Geometry hits = cartRouteLineString.intersection(point);
		
		if(hits.getCoordinates().length > 0) {
			Coordinate coordinate = hits.getCoordinates()[hits.getCoordinates().length-1];
			double[] polar = cartesianToPolar(coordinate.x,coordinate.y);
			geoCalc.setStartingAzimuth(Math.toDegrees(polar[1]));
			geoCalc.setGeodesicDistance(polar[0]);
			DirectPosition targetPosition = geoCalc.getEndPoint();
			
			geoCalc.setStartGeographicPoint(lat, lon);
			geoCalc.setEndPoint(targetPosition);
			double targetAzimuth = (geoCalc.getStartingAzimuth() + 360) % 360;
			
			shipState.setHeading_commanded_deg(targetAzimuth);
		}
		
		System.out.println();
	}

	@Override
	public void setAgent(Agent agent) {
		this.agent = agent;
	}
	
	/**
	 * returns cartesian coordinates from given polar coordinates
	 * 
	 * @param r
	 * @param theta
	 * @return
	 */
	private static double[] polarToCartesian(double r, double theta) {
		double x = r * Math.cos(Math.toRadians(theta));
		double y = r * Math.sin(Math.toRadians(theta));
		return new double[] { x, y };
	}
	
	/**
	 * returns polar coordinates from given cartesian coordinates
	 * 
	 * @param x
	 * @param y
	 * @return double[]{r, theta}
	 */
	private static double[] cartesianToPolar(double x, double y) {
		double r = Math.sqrt(x * x + y * y);
		double theta = Math.atan2(y, x);
		return new double[] { r, theta };
	}

}
