
package de.jade.ecs.route;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import de.jade.ecs.RouteManagerController;
import de.jade.ecs.model.route.WaypointModel;

/**
 * RouteClickListener is used to edit routes via Mouse interaction
 * 
 */
public class RouteClickListener extends MouseAdapter {

	private final JXMapViewer viewer;

	/**
	 * Creates a mouse listener for the jxmapviewer which returns the GeoPosition of
	 * the the point where the mouse was clicked.
	 * 
	 * @param viewer the jxmapviewer
	 */
	public RouteClickListener(JXMapViewer viewer) {
		this.viewer = viewer;
	}

	/**
	 * Gets called on mouseClicked events, calculates the GeoPosition and fires the
	 * mapClicked method that the extending class needs to implement.
	 * 
	 * @param evt the mouse event
	 */
	@Override
	public void mouseClicked(MouseEvent evt) {
		final boolean left = SwingUtilities.isLeftMouseButton(evt);
		final boolean singleClick = (evt.getClickCount() == 1);

		if ((left && singleClick && RouteManagerController.INSTANCE.isEditing)) {
			mapClicked(viewer.convertPointToGeoPosition(new Point(evt.getX(), evt.getY())));
		}
	}

	/**
	 * This method needs to be implemented in the extending class to handle the map
	 * clicked event.
	 * 
	 * @param location The {@link GeoPosition} of the click event
	 */
	public void mapClicked(GeoPosition location) {
		WaypointModel waypointModel = new WaypointModel(
				"" + (RouteManagerController.INSTANCE.waypointTableView.getItems().size() + 1), location.getLatitude(),
				location.getLongitude());
		RouteManagerController.INSTANCE.waypointTableView.getItems().add(waypointModel);

		/** update transition points **/
		waypointModel.updateTransitionPoints(RouteManagerController.INSTANCE.waypointTableView.getItems());
	}
}
