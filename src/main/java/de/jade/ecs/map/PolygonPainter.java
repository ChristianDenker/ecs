package de.jade.ecs.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

/**
 * PolygonPainter
 *
 * 
 */
public class PolygonPainter implements Painter<JXMapViewer> {
	private Color color = Color.YELLOW;
	private boolean antiAlias = true;

	private List<GeoPosition> track;

	/**
	 * @param track the track
	 */
	public PolygonPainter(List<GeoPosition> track) {
		// copy the list so that changes in the
		// original list do not have an effect here
		this.track = Collections.synchronizedList(new ArrayList<GeoPosition>(track));
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

		fillPolygon(g, map);

		g.dispose();
	}

	/**
	 * @param g   the graphics object
	 * @param map the map
	 */
	private void fillPolygon(Graphics2D g, JXMapViewer map) {

		int[] xpoints = new int[track.size()];
		int[] ypoints = new int[track.size()];

		synchronized (track) {
			for (int i = 0; i < track.size(); i++) {
				// convert geo-coordinate to world bitmap pixel
				Point2D pt = map.getTileFactory().geoToPixel(track.get(i), map.getZoom());
				xpoints[i] = (int) pt.getX();
				ypoints[i] = (int) pt.getY();
			}
		}

		Polygon polygonAWT = new Polygon(xpoints, ypoints, track.size());
		g.fillPolygon(polygonAWT);
	}

}