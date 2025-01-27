package userInterface;

import java.io.File;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class CommunInterfaceFunctions {
	
	/**
	 * Show an alert message
	 * @param message
	 */
	public static void showAletDialog(String message) {
		Alert alert = new Alert(AlertType.INFORMATION, message, ButtonType.OK);
		alert.setHeaderText("");
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.setTitle("Warning");	
		alert.showAndWait();
	}
	
	/**
	 * Show an alert message
	 * 
	 * @param message
	 */
	public static boolean showAletDialogWithResponse(String message) {
		Alert alert = new Alert(AlertType.WARNING, message, ButtonType.YES, ButtonType.NO);
		alert.setHeaderText("");
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.setTitle("Alert");
		ButtonType result = alert.showAndWait().orElse(ButtonType.NO);
		if (ButtonType.NO.equals(result)) {
			return false;
		}
		return true;
	}
	
	/*
	 * Returns a File Chooser to select .CSV Files
	 */
	public static FileChooser getCSVFileChooser() {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Configuration Files");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
		return fileChooser;
    }
	
	/*
	 * Returns a File Chooser to select Gezebo Configuration Files
	 */
	public static FileChooser getGezeboFileChooser() {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Select Gezebo Configuration Files");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Gezebo Files (*.world)", "*.world"));
		return fileChooser;
    }
	
	/*
	 * Returns a File Chooser to select Mission Plan Files
	 */
	public static FileChooser getMissionPlanChooser() {
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Select Mission Plan Files");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Mission Plan (*.plan)", "*.plan"));
		return fileChooser;
    }
	
	/**
	 * Get a Directory Path
	 * @return
	 * @throws Exception 
	 */
	public static String getDirectoryPath(Pane p) throws Exception {
		Stage stage = (Stage) p.getScene().getWindow();
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(stage);
		if(selectedDirectory == null){
			throw new Exception();
		}else{
		     return selectedDirectory.getAbsolutePath();
		}
	}

	
	
	
	
}
