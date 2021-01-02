package de.jade.ecs.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import de.jade.ecs.util.SVGUtils;

/**
 * RoutePainter
 *
 * 
 */
public class RoutePainter implements Painter<JXMapViewer> {
	private Color color = Color.YELLOW;
	private boolean antiAlias = true;

	private List<GeoPosition> track;

	private BufferedImage RTEWPT03image = null;

	/**
	 * @param track the track
	 */
	public RoutePainter(List<GeoPosition> track) {
		// copy the list so that changes in the
		// original list do not have an effect here
		this.track = Collections.synchronizedList(new ArrayList<GeoPosition>(track));

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
		int lastX = 0;
		int lastY = 0;

		boolean first = true;

		synchronized (track) {
			for (GeoPosition gp : track) {
				// convert geo-coordinate to world bitmap pixel
				Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());

				drawRTEWPT03(g, pt);

				if (first) {
					first = false;
				} else {
					g.setColor(new Color(227,128,57));
					g.setStroke(new BasicStroke(0.64f,BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] {10.0f}, 2.2f));
					g.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
				}

				lastX = (int) pt.getX();
				lastY = (int) pt.getY();
			}
		}
	}

	private void drawRTEWPT03(Graphics2D g, Point2D point) {
		int x = (int) point.getX() - RTEWPT03image.getWidth() / 2;
		int y = (int) point.getY() - RTEWPT03image.getHeight() / 2;
		g.drawImage(RTEWPT03image, x, y, null);
	}

	public List<GeoPosition> getTrack() {
		return track;
	}

}