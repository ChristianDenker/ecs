package de.jade.ecs;

import de.jade.ecs.map.ChartViewer;
import de.jade.ecs.route.RouteClickListener;
import javafx.animation.TranslateTransition;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ECS_UIController {

	@FXML
	private AnchorPane fullPane = null;

	@FXML
	private StackPane mainStackPane = null;

//	@FXML
	private SwingNode swingNode = null;

	@FXML
	private Button menuButton = null;

	@FXML
	private AnchorPane navPane = null;
	
	private ChartViewer chartViewer = null;
	
	@FXML
	private StackPane routeManagerTab = null;
	
	@FXML
	private RouteManagerController routeManagerController = null;

	/**
	 * Ctor
	 */
	public ECS_UIController() {
	}

	@FXML
	private void initialize() {
		System.out.println(getClass().getName() + " initialized.");

		createChartViewer();

		/** prepare open & close animation **/
		prepareSlideMenuAnimation();

		/** make navPane resizable **/
		DragResizer.makeResizable(navPane);
	}
	
	

	private void createChartViewer() {
		swingNode = new SwingNode();

		chartViewer = new ChartViewer();
		swingNode.setContent(chartViewer.getJXMapViewer());
		RouteManagerController.INSTANCE.chartViewer = chartViewer;
		
		chartViewer.getJXMapViewer().addMouseListener(new RouteClickListener(chartViewer.getJXMapViewer()));

		mainStackPane.getChildren().add(swingNode);
	}

	private void prepareSlideMenuAnimation() {
		TranslateTransition openNav = new TranslateTransition(new Duration(350), navPane);
		openNav.setToY(fullPane.getHeight());
		TranslateTransition closeNav = new TranslateTransition(new Duration(350), navPane);
		menuButton.setOnAction((ActionEvent evt) -> {
			if (navPane.getTranslateY() != 0) {
				openNav.play();
			} else {
				closeNav.setToY(fullPane.getHeight() + navPane.getPrefHeight());
				closeNav.play();
			}
		});
	}

	@FXML
	public void buttonClicked(Event e) {
		System.out.println("Button clicked");
	}

}
