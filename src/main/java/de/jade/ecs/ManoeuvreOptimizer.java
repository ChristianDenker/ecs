package de.jade.ecs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.GeodeticCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.opengis.geometry.DirectPosition;
import org.scheduler.agent.dynamics.container.Container;
import org.scheduler.agent.dynamics.container.ContainerInput;
import org.scheduler.agent.dynamics.container.ContainerState;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.jade.ecs.model.SettingsModel;
import de.jade.ecs.model.route.RouteModel;
import de.jade.ecs.model.route.WaypointModel;

public class ManoeuvreOptimizer {

	private RouteModel routeModel = null;

	private GeodeticCalculator geoCalc = GeodeticCalculator.create(CommonCRS.WGS84.geographic());
	private GeodeticCalculator geoCalcCircleSampling = GeodeticCalculator.create(CommonCRS.WGS84.geographic());
	private static GeometryFactory geoFactory = new GeometryFactory();

	public ManoeuvreOptimizer(RouteModel routeModel) {
		this.routeModel = routeModel;
	}

	public static void main(String[] args) {

		RouteManagerController rc = new RouteManagerController();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			SettingsModel read = objectMapper.readValue(new File("settingsModel.json"), SettingsModel.class);

			System.out.println(read.getRouteModelList().size());

			read.getRouteModelList().get(0);

			ManoeuvreOptimizer opti = new ManoeuvreOptimizer(read.getRouteModelList().get(0));
			LineString routeLineString = opti.routeToLineString();

			System.out.println("str: " + routeLineString.toText());

			GeodeticCalculator geoCalcOpti = GeodeticCalculator.create(CommonCRS.WGS84.geographic());
			geoCalcOpti.setStartGeographicPoint(read.getRouteModelList().get(0).getWaypointList().get(0).getLat(),
					read.getRouteModelList().get(0).getWaypointList().get(0).getLon());
			geoCalcOpti.setEndGeographicPoint(read.getRouteModelList().get(0).getWaypointList().get(1).getLat(),
					read.getRouteModelList().get(0).getWaypointList().get(1).getLon());
			double startHeading = (geoCalcOpti.getStartingAzimuth() + 360) % 360;
			ContainerState containerShipState = new ContainerState();
			containerShipState.setPsi(Math.toRadians(startHeading));

			ContainerInput containerCommands = new ContainerInput(Math.toRadians(0), 160);
			Container containerModel = new Container(containerShipState, containerCommands);
			containerModel.getContainerState().setU(6);
			containerModel.step(0.1);

			long start = System.currentTimeMillis();

			ArrayList<Coordinate> drivenCoordinatesArray = new ArrayList<>();

			Point shipPoint = geoFactory.createPoint(new Coordinate(0, 0));
			Point endPoint = geoFactory
					.createPoint(routeLineString.getCoordinates()[routeLineString.getCoordinates().length - 1]);

			FileWriter writer = new FileWriter("out.txt");

			double rudderAngleStepsize = 0.1d;
			while (endPoint.distance(shipPoint) > 10) {
				Double[][] evaluations = new Double[/*((int) (20 / rudderAngleStepsize) + 1) * */ 3][5];
				int counter = 0;
				int secondsToStepAhead = 300;

				/** evaluate rudder = 0 **/
				Double[] evaluateZero = evaluate(routeLineString, containerModel.clone(), secondsToStepAhead, 0,
						containerModel.getContainerInput().getN_c());
				if (evaluateZero[1] == secondsToStepAhead) {
					evaluations[counter] = evaluateZero;
				} else {
					/** find best rudder in [-10, +10] **/
//					for (double newAngle = -10.0d; newAngle <= 10.0d; newAngle += rudderAngleStepsize) { // 20
						CompletableFuture<Double[]>[] arr = new CompletableFuture[3];
						for (double newRPM = 100; newRPM <= 160.0d; newRPM += 30) {// 5
//							double angel = newAngle;
							double rpm = newRPM;
							CompletableFuture<Double[]> completableFuture = CompletableFuture
									.supplyAsync(() -> {
										/** gradient descent **/
										double precision = 0.0000001;
										double stepSize = 5;
										double previousStep = 1;
										double initialX = 1;
										double currentX = initialX;
										double previousX = initialX;
										Double[] fit = evaluate(routeLineString, containerModel.clone(), secondsToStepAhead, previousX , rpm);
										double previousY = /*(fit[2]/fit[1]/secondsToStepAhead) + */ ((secondsToStepAhead-fit[1])*100) ;//+ (fit[4] * 100);
										int iter = 100;

										currentX += stepSize;

										while (previousStep > precision && iter > 0) {
											iter--;
											Double[] fit2 = evaluate(routeLineString, containerModel.clone(), secondsToStepAhead, currentX , rpm);
											double currentY = /* fit2[2]/fit2[1]/secondsToStepAhead +*/ ((secondsToStepAhead-fit2[1])*100) ;//+ (fit2[4] * 1000);
											if (currentY > previousY || currentX > 10 || currentX < -10) {
												stepSize /= -2;
											}
											previousX = currentX;
											currentX += stepSize;
											previousY = currentY;
											previousStep = StrictMath.abs(currentX - previousX);
										}
//										return currentX;
										Double[] result  = evaluate(routeLineString, containerModel.clone(), secondsToStepAhead, currentX , rpm);
//										System.out.println(Arrays.toString(result));
										return result;
									
									}
									);
											

							arr[(int) ((newRPM - 100) / 30)] = completableFuture;

//						    
//							Double[] fit = evaluate(routeLineString, containerModel.clone(), secondsToStepAhead,
//									newAngle, newRPM);
//					System.out.println("angle: " + newAngle + " | eval resultSeconds:" + fit[0]
//							+ " | resultDistanceIntegral:" + fit[1]);

						}
						for (CompletableFuture<Double[]> fut : arr) {
							try {
								evaluations[counter] = fut.get();
							} catch (InterruptedException | ExecutionException e) {
								e.printStackTrace();
							}
							counter++;
						}
//					}
					/** sort by resultSeconds, max resultSeconds is top **/
					java.util.Arrays.sort(evaluations, new java.util.Comparator<Double[]>() {
						public int compare(Double[] a, Double[] b) {
							return Double.compare(b[1], a[1]);
						}
					});

					ArrayList<Double[]> asdf = new ArrayList<>();
					double threshold = evaluations[0][1] - 1;
					Arrays.stream(evaluations).filter(subArray -> subArray[1] > threshold)
							.sorted(new Comparator<Double[]>() {

								@Override
								public int compare(Double[] a, Double[] b) {
									return Double.compare(a[2], b[2]);
								}
							}).forEach(c -> asdf.add(c));
					evaluations = asdf.toArray(new Double[evaluations.length - 1][5]);

					if (evaluations[0][1] == 0) {
						/** no solution found **/
						break;
					}
				}

				Double[] bestFit = evaluations[0];

				/** write out.txt **/
				writer.append(containerModel.getContainerState().getX() + "; "
						+ containerModel.getContainerState().getY() + "; " + bestFit[0] + "; " + bestFit[3] + "; "
								+ containerModel.getContainerState().calcSpeed()+ "; "
										+ Math.toDegrees(containerModel.getContainerState().getPsi()) + "\r\n");
				writer.flush();

				containerModel.getContainerInput().setDelta_c(Math.toRadians(bestFit[0]));
				containerModel.getContainerInput().setN_c(bestFit[3]);
				if ((Math.round(bestFit[0] * 1000) / 1000.0) == 0) {
//					System.out.println(Math.round(bestFit[0] * 1000) / 1000.0);
					// step 10 seconds
					for (int i = 0; i < 10 * (10); i++) {
						containerModel.step(0.1);
					}
				} else {
					// step 5 second
					for (int i = 0; i < 10 * 5 ; i++) {
						containerModel.step(0.1);
					}
				}

				/** update shipPoint **/
				shipPoint.getCoordinate().setX(containerModel.getContainerState().getX());
				shipPoint.getCoordinate().setY(containerModel.getContainerState().getY());
				shipPoint.geometryChanged();

				drivenCoordinatesArray.add((Coordinate) shipPoint.getCoordinate().clone());
			}
			System.out.println("time: " + (System.currentTimeMillis() - start));
			System.out.println("#coordinates: " + drivenCoordinatesArray.size());
			System.out.flush();
			Coordinate[] arr = drivenCoordinatesArray.toArray(new Coordinate[drivenCoordinatesArray.size() / 2]);
			LineString drivenLineString = geoFactory.createLineString(arr);

			String s = drivenLineString.toText();
			System.out.println(s.substring(0, (int) (s.length() / 2)));
			System.out.println(s.substring((int) (s.length() / 2), s.length()));

			System.out.println();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * 
	 * @param routeLineString
	 * @param containerModelClone
	 * @param newRPM
	 * @param newAngle
	 * @param rudderAngle
	 * @return - new double[] {newRudderAngle, resultSeconds,
	 *         resultDistanceIntegral, newRPM};
	 */
	private static Double[] evaluate(LineString routeLineString, Container containerModelClone,
			double secondsToStepAhead, double newRudderAngle, double newRPM) {
		double maxDistanceFromRoute_m = 10;
		
		boolean lockTime = true;

		double resultSeconds = secondsToStepAhead;
		double resultDistanceIntegral = 0;

		containerModelClone.getContainerInput().setDelta_c(Math.toRadians(newRudderAngle));
		containerModelClone.getContainerInput().setN_c(newRPM);

		Point tempPoint = geoFactory.createPoint(new Coordinate(containerModelClone.getContainerState().getX(), containerModelClone.getContainerState().getY()));
		
		double lastdistance = 0;
		// step the secondsToStep
		for (int second = 0; second <= secondsToStepAhead; second+=20) {
			// step 1 second
			for (int i = 0; i < 10 *20  ; i++) {
				containerModelClone.step(0.1);
			}

			// update Point
			tempPoint.getCoordinate().setX(containerModelClone.getContainerState().getX());
			tempPoint.getCoordinate().setY(containerModelClone.getContainerState().getY());
			tempPoint.geometryChanged();
			// evaluate
			double distance = routeLineString.distance(tempPoint);// * (secondsToStepAhead -second) ;
			resultDistanceIntegral += distance;
			if (distance > maxDistanceFromRoute_m+1 && lockTime) {
				resultSeconds = second;
				lockTime = false;
				lastdistance = distance;
				break;
			}

		}
		return new Double[] { newRudderAngle, resultSeconds, resultDistanceIntegral, newRPM, lastdistance };
	}

//	public static int localMinUtil(int low, int high) {
//
//		// Find index of middle element 
//		int mid = low + (high - low) / 2;
//
//		// Compare middle element with its neighbours 
//		// (if neighbours exist) 
//		if (mid <= 0.001)
//			return mid;
//
//		// If middle element is not minima and its left 
//		// neighbour is smaller than it, then left half 
//		// must have a local minima. 
//		else if (mid > 0.001)
//			return localMinUtil(arr, low, mid - 1, n);
//
//		// If middle element is not minima and its right 
//		// neighbour is smaller than it, then right half 
//		// must have a local minima. 
//		return localMinUtil(arr, mid + 1, high, n);
//	}

	private LineString routeToLineString() {
		ArrayList<Coordinate> coordinateArray = new ArrayList<>();
		geoCalc.setStartGeographicPoint(routeModel.getWaypointList().get(0).getLat(),
				routeModel.getWaypointList().get(0).getLon());

		coordinateArray.add(new Coordinate(0, 0));

		for (int i = 1; i < routeModel.getWaypointList().size(); i++) {
			WaypointModel wpModel = routeModel.getWaypointList().get(i);
			wpModel.updateTransitionPoints(routeModel.getWaypointList());

			if (wpModel.transitionPointToPredecessor != null) {
//				geoCalc.setEndGeographicPoint(wpModel.transitionPointToPredecessor.getCoordinate()[0],
//						wpModel.transitionPointToPredecessor.getCoordinate()[1]);
//				geoCalc.setEndGeographicPoint(wpModel.getLat(), wpModel.getLon());
//				double[] cart = polarToCartesian(geoCalc.getGeodesicDistance(),
//						(geoCalc.getStartingAzimuth() + 360) % 360);
//				coordinateArray.add(new Coordinate(cart[0], cart[1]));

				geoCalcCircleSampling.setStartPoint(wpModel.turningCircleCenter);

				double currentBearing = wpModel.circleCenterBearingToPointToPredecessor;
				double angleStepSize = 1;

				double bearing1 = wpModel.circleCenterBearingToPointToPredecessor;
				double bearing2 = wpModel.circleCenterBearingToPointToSuccessor;

				boolean turnsClockwise = ((bearing1 - bearing2) < 0 ? bearing1 - bearing2 + 360
						: bearing1 - bearing2) > ((bearing2 - bearing1) < 0 ? bearing2 - bearing1 + 360
								: bearing2 - bearing1);

				while (true) {
					if (turnsClockwise) {
						currentBearing += angleStepSize;
						currentBearing %= 360;
						if (wpModel.circleCenterBearingToPointToSuccessor > currentBearing
								&& wpModel.circleCenterBearingToPointToSuccessor - angleStepSize < currentBearing) {
							break;
						}

					} else {
						currentBearing -= angleStepSize;
						if (currentBearing < 0)
							currentBearing += 360;
						if (wpModel.circleCenterBearingToPointToSuccessor < currentBearing
								&& wpModel.circleCenterBearingToPointToSuccessor + angleStepSize > currentBearing) {
							break;
						}

					}
					geoCalcCircleSampling.setStartingAzimuth(currentBearing);
					geoCalcCircleSampling.setGeodesicDistance(wpModel.getTurnRadius_meters());
					DirectPosition endpoint = geoCalcCircleSampling.getEndPoint();

					geoCalc.setEndGeographicPoint(endpoint.getOrdinate(0), endpoint.getOrdinate(1));
					double[] cart = polarToCartesian(geoCalc.getGeodesicDistance(),
							(geoCalc.getStartingAzimuth() + 360) % 360);
					coordinateArray.add(new Coordinate(cart[0], cart[1]));

				}

			} else {
				geoCalc.setEndGeographicPoint(wpModel.getLat(), wpModel.getLon());
				double[] cart = polarToCartesian(geoCalc.getGeodesicDistance(),
						(geoCalc.getStartingAzimuth() + 360) % 360);
				coordinateArray.add(new Coordinate(cart[0], cart[1]));
			}

		}
		Coordinate[] arr = coordinateArray.toArray(new Coordinate[coordinateArray.size()]);
		LineString lineString = geoFactory.createLineString(arr);
		return lineString;
	}

	/**
	 * returns cartesian coordinates from given polar coordinates
	 * 
	 * @param r
	 * @param theta
	 * @return
	 */
	private static double[] polarToCartesian(double r, double theta) {
		double x = r * Math.cos(Math.toRadians(theta));
		double y = r * Math.sin(Math.toRadians(theta));
		return new double[] { x, y };
	}

	/**
	 * returns polar coordinates from given cartesian coordinates
	 * 
	 * @param x
	 * @param y
	 * @return double[]{r, theta}
	 */
	private static double[] cartesianToPolar(double x, double y) {
		double r = Math.sqrt(x * x + y * y);
		double theta = Math.atan2(y, x);
		return new double[] { r, theta };
	}

}
