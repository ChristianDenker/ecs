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

/**
 * WaypointCanvas is used for portrayal of Waypoints
 * 
 * @author chris
 *
 */
public class WaypointCanvas extends JComponent {

	private static final long serialVersionUID = 1L;

	private BufferedImage RTEWPT03image = null;

	private WaypointModel waypointModel = null;

	public MovingAdapter movingAdapter = null;

	/**
	 * Ctor
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

		private WaypointModel pre_predecessorWpModel = null;
		private WaypointModel predecessorWpModel = null;
		private WaypointModel successorWpModel = null;
		private WaypointModel post_successorWpModel = null;

		/**
		 * Ctor
		 */
		public MovingAdapter() {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			startPoint = getLocation();

			{ /**
				 * get successors and predecessors wpModel of this wpModel, to update transition
				 * points between line and circular segments while dragging (public void
				 * mouseDragged(..))
				 **/
				int waypointModelIndex = RouteManagerController.INSTANCE.waypointTableView.getItems()
						.indexOf(waypointModel);

				int predecessorIndex = (waypointModelIndex == 0) ? -1 : waypointModelIndex - 1; // can result in -2
				int pre_predecessorIndex = (predecessorIndex == 0) ? -1 : predecessorIndex - 1;
				int successorIndex = (waypointModelIndex == RouteManagerController.INSTANCE.waypointTableView.getItems()
						.size() - 1) ? -1 : waypointModelIndex + 1;
				int post_successorIndex = (successorIndex == RouteManagerController.INSTANCE.waypointTableView
						.getItems().size() - 1) ? -1 : successorIndex + 1;

				if (pre_predecessorIndex > -1) {
					pre_predecessorWpModel = RouteManagerController.INSTANCE.waypointTableView.getItems()
							.get(pre_predecessorIndex);
				}
				if (predecessorIndex > -1) {
					predecessorWpModel = RouteManagerController.INSTANCE.waypointTableView.getItems()
							.get(predecessorIndex);
				}
				if (successorIndex > -1) {
					successorWpModel = RouteManagerController.INSTANCE.waypointTableView.getItems().get(successorIndex);
				}
				if (post_successorIndex > -1) {
					post_successorWpModel = RouteManagerController.INSTANCE.waypointTableView.getItems()
							.get(post_successorIndex);
				}
			}

		}

		@Override
		public void mouseReleased(MouseEvent e) {
//			System.out.println("released");
		}

		@Override
		public void mouseDragged(MouseEvent e) {

			int buttonX = (int) (e.getX() - RTEWPT03image.getWidth() / 2 + startPoint.getX());
			int buttonY = (int) (e.getY() - RTEWPT03image.getHeight() / 2 + startPoint.getY());

			GeoPosition pt = RouteManagerController.INSTANCE.chartViewer.getJXMapViewer()
					.convertPointToGeoPosition(new Point(buttonX, buttonY));
			waypointModel.setLat(pt.getLatitude());
			waypointModel.setLon(pt.getLongitude());

			waypointModel.updateTransitionPoints(predecessorWpModel, successorWpModel);
			if (predecessorWpModel != null)
				predecessorWpModel.updateTransitionPoints(pre_predecessorWpModel, waypointModel);
			if (successorWpModel != null)
				successorWpModel.updateTransitionPoints(waypointModel, post_successorWpModel);
			RouteManagerController.INSTANCE.waypointTableView.refresh();
			RouteManagerController.INSTANCE.chartViewer.getJXMapViewer().updateUI();

		}
	}

}