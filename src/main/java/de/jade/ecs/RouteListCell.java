package de.jade.ecs;

import de.jade.ecs.model.route.RouteModel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class RouteListCell extends ListCell<RouteModel> {
	HBox hbox = new HBox();
	Label label = new Label("(empty)");
	Pane pane = new Pane();
	Button buttonShow = new Button("show");
	Button buttonDelete = new Button("delete");
	RouteModel lastItem;

	public RouteListCell() {
		super();
		hbox.getChildren().addAll(label, pane, buttonShow, buttonDelete);
		HBox.setHgrow(pane, Priority.ALWAYS);
		
		buttonShow.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("RouteListCell| " + lastItem + " : " + event);
				RouteManagerController.INSTANCE.routeCreateGrid.toFront();
				
				RouteManagerController.INSTANCE.routeToEdit = lastItem;
				RouteManagerController.INSTANCE.routeName.setText(lastItem.getName());
				
				RouteManagerController.INSTANCE.setupWaypoints();
				
				RouteManagerController.INSTANCE.isEditing = true;
			}
		});
		
		buttonDelete.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ECS_UIController.INSTANCE.getSettings().getRouteModelList().remove(lastItem);
				ECS_UIController.INSTANCE.saveSettings();
			}
		});
		
	}

	@Override
	protected void updateItem(RouteModel item, boolean empty) {
		super.updateItem(item, empty);
		setText(null); // No text in label of super class
		if (empty) {
			lastItem = null;
			setGraphic(null);
		} else {
			lastItem = item;
			label.setText(item != null ? item.getName() : "<null>");
			setGraphic(hbox);
		}
	}
}
