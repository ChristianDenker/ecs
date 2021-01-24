package de.jade.ecs.simulation;

import java.time.LocalTime;

import org.apache.sis.geometry.DirectPosition2D;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.GeodeticCalculator;
import org.opengis.geometry.DirectPosition;
import org.scheduler.Tick;
import org.scheduler.agent.Agent;
import org.scheduler.agent.behaviour.ITickExecution;
import org.scheduler.agent.dynamics.container.Container;
import org.scheduler.agent.dynamics.container.ContainerInput;
import org.scheduler.agent.dynamics.container.ContainerState;
import org.scheduler.agent.state.ShipState;

public class ContainershipDynamics implements ITickExecution {

	private ShipState shipState = null;

	public GeodeticCalculator geodeticCalculator = GeodeticCalculator.create(CommonCRS.WGS84.geographic());

	@SuppressWarnings("unused")
	private Agent agent = null;

	private DirectPosition initialPosition = null;

	private ContainerState containerShipState = null;

	private ContainerInput containerCommands = null;

	private Container containerModel = null;

	/**
	 * Ctor
	 * 
	 * @param shipState
	 */
	public ContainershipDynamics(ShipState shipState) {
		this.shipState = shipState;
		this.initialPosition = new DirectPosition2D(shipState.getPoint().getY(), shipState.getPoint().getX()); //(Point2D) shipState.getPoint().clone();
		

		this.containerShipState = new ContainerState();
		containerShipState.setPsi(Math.toRadians(shipState.getHeading_current_deg()));

		this.containerCommands = new ContainerInput(Math.toRadians(0), 160);
		this.containerModel = new Container(containerShipState, containerCommands);

	}

	
	public ContainerInput getContainerCommands() {
		return containerCommands;
	}


	public void setContainerCommands(ContainerInput containerCommands) {
		this.containerCommands = containerCommands;
	}


	@Override
	public void execute(Tick tick) {
		geodeticCalculator.setStartPoint(initialPosition);

		/**
		 * calculations for step size, that is necessary to change simulation's
		 * frequency or speed
		 **/
		LocalTime lt = LocalTime.of(0, 0, 0, 0);
		lt = (LocalTime) tick.getTemporalAdjuster().adjustInto(lt);
		long milliOfDay = lt.toNanoOfDay() / 1_000_000;

		/** update the rudder and set it to the model input **/
		double rudderAngle_deg = updateRudder(shipState);
		containerCommands.setDelta_c(Math.toRadians(rudderAngle_deg));

		/** step the model **/
		int amountOf100msSteps = (int) (milliOfDay / 100);
		for (int i = 0; i < amountOf100msSteps; i++) {
			containerModel.step(0.100);
		}
		double residualTime = milliOfDay - (amountOf100msSteps * 100);
		if (residualTime > 0) {
			containerModel.step(residualTime);
		}

		/** cartesian to polar **/
		double r = Math.sqrt((containerModel.getContainerState().getX() * containerModel.getContainerState().getX())
				+ (containerModel.getContainerState().getY() * containerModel.getContainerState().getY()));
		double theta = Math.atan2(containerModel.getContainerState().getY(), containerModel.getContainerState().getX());

		/** calc new location **/
		geodeticCalculator.setStartingAzimuth(Math.toDegrees(theta));
		geodeticCalculator.setGeodesicDistance(r);
		DirectPosition destination = geodeticCalculator.getEndPoint();

		/** update ShipState **/
		shipState.getPoint().setLocation(destination.getOrdinate(1), destination.getOrdinate(0));
		double heading = Math.toDegrees(containerModel.getContainerState().getPsi()) % 360 % -360;
		if (heading < 0)
			heading = 360 + heading;
		shipState.setHeading_current_deg(heading);
		shipState.setSpeed_current_kn(containerModel.getContainerState().calcSpeed() * 3.6 / 1.852);
	}

	// you may use these variables to store data inbetween calls of updateRudder()
	double previousError = 0;
	double errorIntegral = 0;

	/**
	 * 
	 * @param shipState - the ShipState contains commanded and current heading
	 * @return - the rudder angle to be set in degrees
	 */
	private double updateRudder(ShipState shipState) {

		double newRudderAngle = 0;

		/**
		 * add your PID-Controller code here
		 */

		double Kp = 5;
		double Ki = 0;
		double Kd = 105;

		double errorTerm = (shipState.getHeading_commanded_deg() - shipState.getHeading_current_deg() + 540) % 360
				- 180;

		double prop = Kp * errorTerm;
		double integ = (Ki * errorTerm) + errorIntegral;
		errorIntegral = integ;
		double deriv = Kd * (errorTerm - previousError);
		previousError = errorTerm;

		newRudderAngle = prop + integ + deriv;
//		System.out.println("heading: " + shipState.getHeading_current_deg() + "\t| currentRudder: "
//				+ Math.toDegrees(containerModel.getContainerState().getDelta()) + "\t| newRudder: " + newRudderAngle
//				+ "\t| errorTerm: " + errorTerm + "\t| prop: " + prop + " |\tintegral: " + integ + "\t | deriv: "
//				+ deriv);
		return newRudderAngle;

	}

	@Override
	public void setAgent(Agent agent) {
		this.agent = agent;
	}

}
