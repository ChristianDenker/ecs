package de.jade.ecs.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;

import org.jxmapviewer.viewer.GeoPosition;

import de.jade.ecs.RouteManagerController;
import de.jade.ecs.model.route.WaypointModel;
import de.jade.ecs.util.SVGUtils;

/** WaypointCanvas is used for portrayal of Waypoints
 * 
 * @author chris
 *
 */
public class WaypointCanvas extends JComponent {

	private static final long serialVersionUID = 1L;

	private BufferedImage RTEWPT03image = null;

	private WaypointModel waypointModel = null;

	public MovingAdapter movingAdapter = null;

	/** Ctor
	 * 
	 * @param waypointModel
	 */
	public WaypointCanvas(WaypointModel waypointModel) {
		super();
		this.waypointModel = waypointModel;
		try {
			RTEWPT03image = SVGUtils.rasterize(new File("src/main/resources/s421/portrayal/Symbols/RTEWPT03.svg"));
			setSize(RTEWPT03image.getWidth(), RTEWPT03image.getHeight());
			setPreferredSize(new Dimension(RTEWPT03image.getWidth(), RTEWPT03image.getHeight()));

			movingAdapter = new MovingAdapter();
			addMouseListener(movingAdapter);
			addMouseMotionListener(movingAdapter);

			setBackground(new Color(0, 0, 0, 0));
			setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(RTEWPT03image, 0, 0, null);
	}

	class MovingAdapter extends MouseAdapter {

		public Point startPoint = null;

		/**
		 * Ctor
		 */
		public MovingAdapter() {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			startPoint = getLocation();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			System.out.println("released");
		}

		@Override
		public void mouseDragged(MouseEvent e) {

			int buttonX = (int) (e.getX() - RTEWPT03image.getWidth()  / 2 + startPoint.getX());
			int buttonY = (int) (e.getY() - RTEWPT03image.getHeight() / 2 + startPoint.getY());

//			System.out.print("buttonX: " + buttonX + " | buttonY: " + buttonY + " | ");
			GeoPosition pt = RouteManagerController.INSTANCE.chartViewer.getJXMapViewer()
					.convertPointToGeoPosition(new Point(buttonX, buttonY));
			waypointModel.setLat(pt.getLatitude());
			waypointModel.setLon(pt.getLongitude());
			RouteManagerController.INSTANCE.waypointTableView.refresh();
			RouteManagerController.INSTANCE.chartViewer.getJXMapViewer().updateUI();

//			System.out.println("new Lat: " + pt.getLatitude() + " | new Lon: " + pt.getLongitude());
		}
	}

}