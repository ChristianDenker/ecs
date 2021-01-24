package de.jade.ecs;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.jade.ecs.map.ChartViewer;
import de.jade.ecs.model.SettingsModel;
import de.jade.ecs.route.RouteClickListener;
import javafx.animation.TranslateTransition;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ECS_UIController {

	public static ECS_UIController INSTANCE = null;
	
	private SettingsModel settings = null;
	
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
		loadSettings();
		INSTANCE = this;
	}

	@FXML
	private void initialize() {
		System.out.println(getClass().getName() + " initialized.");
		
		

		createChartViewer();

		/** prepare open & close animation for menu **/
		prepareSlideMenuAnimation();

		/** make navPane resizable **/
		DragResizer.makeResizable(navPane);
	}
	
	

	private void loadSettings() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			SettingsModel read = objectMapper.readValue(new File("settingsModel.json"), SettingsModel.class);
			setSettings(read);
			System.out.println("Settings loaded.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveSettings() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			objectMapper.writeValue(new File("settingsModel.json"), settings);
			System.out.println("Settings saved.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createChartViewer() {
		swingNode = new SwingNode();

		chartViewer = new ChartViewer();

		swingNode.setContent(chartViewer.getJXMapViewer());

		RouteManagerController.INSTANCE.chartViewer = chartViewer;
		SimulationController.INSTANCE.chartViewer = chartViewer;
		
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

	public SettingsModel getSettings() {
		return settings;
	}

	public void setSettings(SettingsModel settings) {
		this.settings = settings;
	}

}
