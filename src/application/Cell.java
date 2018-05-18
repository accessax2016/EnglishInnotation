package application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import model.Subtitle;

public class Cell extends ListCell<Subtitle>{
	HBox hBox = new HBox();
	Label lblStartTime = new Label();
	Label lblContent = new Label();
	Pane pane = new Pane();
	Button btn = new Button("Thích");

	public Cell() {
		super();

		hBox.getChildren().addAll(lblStartTime, lblContent, pane, btn);
		hBox.setHgrow(pane, Priority.ALWAYS);
		btn.setVisible(false);
		btn.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				// TODO Auto-generated method stub
				if (getItem().getIsFavorite()) {
					lblContent.setTextFill(Color.BLACK);
					lblStartTime.setTextFill(Color.BLACK);
					btn.setText("Thích");
					getItem().setIsFavorite(false);
				}
				else {
					lblContent.setTextFill(Color.BLUE);
					lblStartTime.setTextFill(Color.BLUE);
					btn.setText("Bỏ Thích");
					getItem().setIsFavorite(true);
				}
				
			}
		});
		this.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				// TODO Auto-generated method stub
				btn.setVisible(true);
			}
		});
		this.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				// TODO Auto-generated method stub
				btn.setVisible(false);
			}
		});
	}
	
	public void updateItem(Subtitle subtitle, boolean empty) {
		super.updateItem(subtitle, empty);
		setText(null);
		setGraphic(null);
		
		if (subtitle != null && !empty) {
			lblStartTime.setText(subtitle.getStartTime() + "	");
			lblContent.setText(subtitle.getContent());
			
			setGraphic(hBox);
		}
	}
	
	
}
