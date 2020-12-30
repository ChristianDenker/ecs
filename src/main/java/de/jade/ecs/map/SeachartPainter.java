package de.jade.ecs.map;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

import render.Renderer;
import s57.S57map;

/**
 * SeachartPainter
 * 
 * Painter for {@link S57map}
 * 
 * @author chris
 *
 */
public class SeachartPainter implements Painter<JXMapViewer> {

	private boolean antiAlias = true;

	private S57map s57map;
	private ChartViewer simViewer;

	/**
	 * Ctor
	 * 
	 * @param s57map    - the s57map
	 * @param simViewer - the viewer to render onto
	 */
	public SeachartPainter(S57map s57map, ChartViewer simViewer) {
		this.s57map = s57map;
		this.simViewer = simViewer;
	}

	@Override
	public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
		g = (Graphics2D) g.create();

		// convert from viewport to world bitmap
		Rectangle rect = map.getViewportBounds();
		g.translate(-rect.x, -rect.y);
		Renderer.reRender((Graphics2D) g, map.getViewportBounds(), 14, 4, s57map, simViewer);

		if (antiAlias)
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g.dispose();
	}

}