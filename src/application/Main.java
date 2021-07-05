package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.event.*;
import javafx.scene.input.*;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("appLayout.fxml"));
			Scene scene = new Scene(root,1200,800);
			primaryStage.setScene(scene);
			primaryStage.setTitle("DIY media player");
			scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent doubleClicked) {
					if (doubleClicked.getClickCount() == 2) 
						primaryStage.setFullScreen(true);
				}				
			});
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
