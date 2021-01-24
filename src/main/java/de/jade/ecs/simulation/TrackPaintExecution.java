package de.jade.ecs.simulation;

import org.jxmapviewer.viewer.GeoPosition;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.scheduler.Tick;
import org.scheduler.agent.Agent;
import org.scheduler.agent.behaviour.ITickExecution;
import org.scheduler.agent.state.ShipState;

import de.jade.ecs.map.ChartViewer;
import de.jade.ecs.map.ChartViewer.LinePainter;

/** TackPaintExecution
 * 
 * Creates a new LinePainter in constructor and adds a new GeoPosition to the LinePainter with every call of execute()
 *
 */
public class TrackPaintExecution implements ITickExecution {

	private ShipState shipState = null;
	private ChartViewer chartViewer = null;
	private LinePainter tracePainter = null;

	/**
	 * Constructor
	 * 
	 * uses the first Point in shipState as initial point for the LinePainter
	 * 
	 * @param shipState 
	 */
	public TrackPaintExecution(ChartViewer simViewer, ShipState shipState) {
		this.chartViewer = simViewer;
		this.shipState = shipState;
		tracePainter = createLinePainter("POINT(" + shipState.getPoint().getX() + " " + shipState.getPoint().getY() + ")");

	}

	@Override
	public void execute(Tick tick) {
		tracePainter.getTrack().add(new GeoPosition(shipState.getPoint().getY(), shipState.getPoint().getX()));
		// automatically updates UI
		chartViewer.getJXMapViewer().updateUI();

	}

	@Override
	public void setAgent(Agent agent) {
		// not used
	}
	
	/** adds a Linepainter to the chartViewer
	 * 
	 * @param wktString - e.g. "POINT(8 54)"
	 * @return - the linePainter
	 */
	public LinePainter createLinePainter(String wktString) {
		/** read WKT from String and createLinepainter **/
		WKTReader wktReader = new WKTReader();
		Geometry geo = null;
		try {
			geo = wktReader.read(wktString);
		} catch (ParseException e1) {
			System.out.println("Maybe WKT is invalid?");
			e1.printStackTrace();
		}
		LinePainter tracePainter = chartViewer.addLinePainter(geo);
		
		return tracePainter;
	}

	/**
	 * 
	 * @return - the LinePainter/TracePainter
	 */
	public LinePainter getTracePainter() {
		return tracePainter;
	}

}