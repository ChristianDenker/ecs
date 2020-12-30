package de.jade.ecs;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;


public class ECS_UI extends Application {

	@Override
	public void start(Stage primaryStage) {
		FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/ECS_UI.fxml"));
        Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
        Scene scene = new Scene(root); //, 512, 512);
    
        primaryStage.setTitle("ECS");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        ECS_UIController controllerRef = loader.getController();

        System.out.println(controllerRef + " loaded.");
        
        applyMetroColorTheme(scene);
	}

	private void applyMetroColorTheme(Scene scene) {
		JMetro jMetro = new JMetro(Style.DARK);
        jMetro.setAutomaticallyColorPanes(true);
        jMetro.setScene(scene);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
