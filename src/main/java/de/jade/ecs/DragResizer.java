package de.jade.ecs;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * https://www.java-forum.org/thema/javafx-resize-und-draganddrop-eines-pane.163434/
 * 
 * @author HaukeG 
 *
 */
public class DragResizer {

	private static final int RESIZE_MARGIN = 3;

	private final Region region;

	private double y;
	private double x;

	private boolean initMinHeight;
	@SuppressWarnings("unused")
	private boolean initMinWidth;
	private boolean moving;
	private boolean dragging;

	private double lastX;
	private double lastY;

	private Cursor usedCursor;

	private DragResizer(Region aRegion) {
		region = aRegion;
	}

	public static void makeResizable(Region region) {
		final DragResizer resizer = new DragResizer(region);

		region.setOnMousePressed(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				resizer.mousePressed(event);
			}
		});
		region.setOnMouseDragged(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				resizer.mouseDragged(event);
			}
		});
		region.setOnMouseMoved(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				resizer.mouseOver(event);
			}
		});
		region.setOnMouseReleased(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				resizer.mouseReleased(event);
			}
		});
	}

	protected void mouseReleased(MouseEvent event) {
		dragging = false;
		region.setCursor(Cursor.DEFAULT);
	}

	protected void mouseOver(MouseEvent event) {
		// wenn cursor null ist, darf nicht vergroesstert / verkleinert werden
		usedCursor = getCorrectCursor(event);

		if (usedCursor != null || dragging) {
			region.setCursor(usedCursor);
		} else {
			region.setCursor(Cursor.DEFAULT);
		}
	}

	private Cursor getCorrectCursor(MouseEvent event) {
		if (event.getY() > (region.getHeight() - RESIZE_MARGIN)) {
			if (event.getX() > (region.getWidth() - RESIZE_MARGIN)) {
				return Cursor.SE_RESIZE;
			}
			if (event.getX() < RESIZE_MARGIN) {
				return Cursor.SW_RESIZE;
			}
			return Cursor.S_RESIZE;
		}
		if (event.getY() < RESIZE_MARGIN) {
			if (event.getX() > (region.getWidth() - RESIZE_MARGIN)) {
				return Cursor.NE_RESIZE;
			}
			if (event.getX() < RESIZE_MARGIN) {
				return Cursor.NW_RESIZE;
			}
			return Cursor.N_RESIZE;
		}
		if (event.getX() < RESIZE_MARGIN) {
			return Cursor.W_RESIZE;
		}
		if (event.getX() > (region.getWidth() - RESIZE_MARGIN)) {
			return Cursor.E_RESIZE;
		}
		return null;
	}

	
	
	protected void mouseDragged(MouseEvent event) {

		double diffX = event.getX() - x;
		double diffY = event.getY() - y;

		if (moving) {
			region.setLayoutX(region.getLayoutX() + diffX);
			region.setLayoutY(region.getLayoutY() + diffY);
			return;
		}

		if (!dragging) {
			return;
		}

		if (usedCursor.equals(Cursor.S_RESIZE)
				|| usedCursor.equals(Cursor.SW_RESIZE)
				|| usedCursor.equals(Cursor.SE_RESIZE)) {
	
			region.setMinHeight(event.getY());
			region.setMaxHeight(event.getY());
			region.setPrefHeight(event.getY());
		}
		if (usedCursor.equals(Cursor.E_RESIZE)
				|| usedCursor.equals(Cursor.SE_RESIZE)
				|| usedCursor.equals(Cursor.NE_RESIZE)) {
			region.setMinWidth(event.getSceneX() - region.getLayoutX());
			region.setMaxWidth(event.getSceneX() - region.getLayoutX());
			region.setPrefWidth(event.getSceneX() - region.getLayoutX());
		}
		if (usedCursor.equals(Cursor.W_RESIZE) || usedCursor.equals(Cursor.SW_RESIZE) || usedCursor.equals(Cursor.NW_RESIZE)) {
			region.setLayoutX(event.getSceneX());
		
			region.setMinWidth(region.getMinWidth() + (lastX - event.getSceneX()));
			region.setMaxWidth(region.getMaxWidth() + (lastX - event.getSceneX()));
			region.setPrefWidth(region.getPrefWidth() + (lastX - event.getSceneX()));
		
		}
		if (usedCursor.equals(Cursor.N_RESIZE) || usedCursor.equals(Cursor.NE_RESIZE) || usedCursor.equals(Cursor.NW_RESIZE)) {
			region.setLayoutY(region.getLayoutY() + (event.getSceneY() - lastY));
			region.setMinHeight(region.getMinHeight() + (lastY - event.getSceneY()));
			region.setMaxHeight(region.getMaxHeight() + (lastY - event.getSceneY()));
			region.setPrefHeight(region.getPrefHeight() + (lastY - event.getSceneY()));
		}
		
		lastX = event.getSceneX();
		lastY = event.getSceneY();
	}
	

	protected void mousePressed(MouseEvent event) {
		lastX = event.getSceneX();
		lastY = event.getSceneY();
		
		y = event.getY();
		x = event.getX();
		
		if (getCorrectCursor(event) == null) {
			moving = true;
			return;
		}
		moving = false;
		dragging = true;

		if (!initMinHeight) {
			region.setMinHeight(region.getHeight());
			region.setMinWidth(region.getWidth());
			initMinHeight = true;
			initMinWidth = true;
		}

		
	}
}