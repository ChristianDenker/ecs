package de.jade.ecs.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import de.jade.ecs.RouteManagerController;
import de.jade.ecs.model.route.WaypointModel;
import de.jade.ecs.util.SVGUtils;

/**
 * RoutePainter
 *
 * 
 */
public class RoutePainter implements Painter<JXMapViewer> {
	private Color color = Color.YELLOW;
	private boolean antiAlias = false;

	private List<WaypointModel> track;

	private BufferedImage RTEWPT03image = null;

	private boolean isDebug = false;

	/**
	 * @param track the track
	 */
	public RoutePainter(List<WaypointModel> track) {
		// copy the list so that changes in the
		// original list do not have an effect here
		this.track = Collections.synchronizedList(new ArrayList<WaypointModel>(track));
		try {
			RTEWPT03image = SVGUtils.rasterize(new File("src/main/resources/s421/portrayal/Symbols/RTEWPT03.svg"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
		g = (Graphics2D) g.create();

		// convert from viewport to world bitmap
		Rectangle rect = map.getViewportBounds();
		g.translate(-rect.x, -rect.y);

		if (antiAlias)
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// do the drawing again
		g.setColor(color);
		g.setStroke(new BasicStroke(1));

		drawRoute(g, map);

		g.dispose();
	}

	/**
	 * @param g   the graphics object
	 * @param map the map
	 */
	private void drawRoute(Graphics2D g, JXMapViewer map) {

		/** draw waypoints **/
		for (WaypointModel gp : track) {

			// convert geo-coordinate to world bitmap pixel
			Point2D pt = RouteManagerController.INSTANCE.chartViewer.getJXMapViewer()
					.convertGeoPositionToPoint(new GeoPosition(gp.getLat(), gp.getLon()));

			drawRTEWPT03(g, pt, gp);

		}

		/** paint leg between waypoints **/

		WaypointModel lastWpModel = null;

		Iterator<WaypointModel> wpIter = track.iterator();
		if (wpIter.hasNext()) {
			lastWpModel = wpIter.next();
		}
		while (wpIter.hasNext()) {

			WaypointModel wpModel = lastWpModel;

			Point2D fromPoint2d = null;
			if (wpModel.transitionPointToSuccessor == null) {
				fromPoint2d = RouteManagerController.INSTANCE.chartViewer.getJXMapViewer()
						.convertGeoPositionToPoint(new GeoPosition(wpModel.getLat(), wpModel.getLon()));
			} else {
				fromPoint2d = RouteManagerController.INSTANCE.chartViewer.getJXMapViewer()
						.convertGeoPositionToPoint(new GeoPosition(wpModel.transitionPointToSuccessor.getCoordinate()));
			}

			WaypointModel wpModel2 = wpIter.next();

			Point2D toPoint2d = null;
			if (wpModel2.transitionPointToPredecessor == null) {
				toPoint2d = RouteManagerController.INSTANCE.chartViewer.getJXMapViewer()
						.convertGeoPositionToPoint(new GeoPosition(wpModel2.getLat(), wpModel2.getLon()));
			} else {
				toPoint2d = RouteManagerController.INSTANCE.chartViewer.getJXMapViewer().convertGeoPositionToPoint(
						new GeoPosition(wpModel2.transitionPointToPredecessor.getCoordinate()));
			}

			Rectangle bounds = RouteManagerController.INSTANCE.chartViewer.getJXMapViewer().getViewportBounds();
			int fromX = (int) (bounds.x + fromPoint2d.getX());
			int fromY = (int) (bounds.y + fromPoint2d.getY());

			int toX = (int) (bounds.x + toPoint2d.getX());
			int toY = (int) (bounds.y + toPoint2d.getY());

			g.setColor(new Color(227, 128, 57));
			g.setStroke(new BasicStroke(0.64f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 10.0f },
					2.2f));
			g.drawLine(fromX, fromY, toX, toY);

			/** Paint Circle **/
			if (wpModel2.transitionPointToSuccessor != null) {

				if (isDebug) {
					Point2D circlePointToPredecessor = RouteManagerController.INSTANCE.chartViewer.getJXMapViewer()
							.convertGeoPositionToPoint(
									new GeoPosition(wpModel2.transitionPointToPredecessor.getCoordinate()));
					int circlePointToPredecessorX = (int) (bounds.x + circlePointToPredecessor.getX());
					int circlePointToPredecessorY = (int) (bounds.y + circlePointToPredecessor.getY());
					g.drawOval(circlePointToPredecessorX - 5, circlePointToPredecessorY - 5, 10, 10);

					Point2D circlePointToSuccessor = RouteManagerController.INSTANCE.chartViewer.getJXMapViewer()
							.convertGeoPositionToPoint(
									new GeoPosition(wpModel2.transitionPointToSuccessor.getCoordinate()));
					int circlePointToSuccessorX = (int) (bounds.x + circlePointToSuccessor.getX());
					int circlePointToSuccessorY = (int) (bounds.y + circlePointToSuccessor.getY());
					g.drawOval(circlePointToSuccessorX - 5, circlePointToSuccessorY - 5, 10, 10);
				}

				Point2D circle = RouteManagerController.INSTANCE.chartViewer.getJXMapViewer()
						.convertGeoPositionToPoint(new GeoPosition(wpModel2.turningCircleCenter.getCoordinate()));
				int circleX = (int) (bounds.x + circle.getX());
				int circleY = (int) (bounds.y + circle.getY());
				if (isDebug) g.drawOval(circleX - 5, circleY - 5, 10, 10);
				double screenRadius = Math.sqrt(Math.pow(toX - circleX, 2) + Math.pow(toY - circleY, 2));

				double first = (90 - wpModel2.circleCenterBearingToPointToPredecessor + 360) % 360;
				double second = (90 - wpModel2.circleCenterBearingToPointToSuccessor + 360) % 360;
				double arcLength = WaypointModel.getDifference(first, second);
				Arc2D.Double arc = new Arc2D.Double(circleX - screenRadius, circleY - screenRadius, screenRadius * 2,
						screenRadius * 2, WaypointModel.isBearing1LeftOfBearing2(first, second) ? second : first,
						arcLength, Arc2D.OPEN);
				g.draw(arc);
			}

			lastWpModel = wpModel2;

		}

	}

//	private int[] getPointOnMap(WaypointModel wpModel) {
//
//		Point2D pt = wpModel.waypointCanvas.getLocation();
//		Rectangle bounds = RouteManagerController.INSTANCE.chartViewer.getJXMapViewer().getViewportBounds();
//		int x = (int) (bounds.x + pt.getX() + wpModel.waypointCanvas.getWidth() / 2);
//		int y = (int) (bounds.y + pt.getY() + wpModel.waypointCanvas.getHeight() / 2);
//
//		return new int[] { x, y };
//	}

	private void drawRTEWPT03(Graphics2D g, Point2D point, WaypointModel waypointModel) {

		int buttonX = (int) (point.getX() - RTEWPT03image.getWidth() / 2);
		int buttonY = (int) (point.getY() - RTEWPT03image.getHeight() / 2);

		if (waypointModel.waypointCanvas == null) {
			waypointModel.waypointCanvas = new WaypointCanvas(waypointModel);

			RouteManagerController.INSTANCE.chartViewer.getJXMapViewer().add(waypointModel.waypointCanvas);
			waypointModel.waypointCanvas.setLocation(buttonX, buttonY);

		} else {
			waypointModel.waypointCanvas.setLocation(buttonX, buttonY);
			waypointModel.waypointCanvas.movingAdapter.startPoint = waypointModel.waypointCanvas.getLocation(); // resets
																												// startPosition
		}

	}

	public List<WaypointModel> getTrack() {
		return track;
	}

}