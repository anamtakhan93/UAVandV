package userInterface;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application{
	
	//Run configuration of the project
	// --module-path C:\javafx\lib --add-modules=javafx.fxml,javafx.controls
	
	@Override 
	public void start(Stage primaryStage) { 
		try { 
			BorderPane root = (BorderPane) FXMLLoader.load(getClass().getResource("SideBar.fxml"));
			Scene scene = new Scene(root);

			primaryStage.setMinWidth(1180);
			primaryStage.setMinHeight(810);
			//primaryStage.setResizable(false);
			primaryStage.setTitle("UAV Fault Injection Tool");
			
			primaryStage.setScene(scene);
			primaryStage.show(); 
		}catch(Exception e) { 
			e.printStackTrace(); 
		} 
	}
	 
	 
	public static void main(String[] args) {
		launch(args);
	}
}
