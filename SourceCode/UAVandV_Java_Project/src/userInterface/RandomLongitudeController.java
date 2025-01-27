package userInterface;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

public class RandomLongitudeController implements Initializable {

	@FXML
    private TextField tlMinGpsLongitude;
    @FXML
    private TextField tlMaxGpsLongitude;
    @FXML
    private TextField tlStartInjWindowTime, tlEndInjWindowTime, tlEndInjRunTime;
	
	
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		//Longitude Change Listeners
		tlMinGpsLongitude.textProperty().addListener(new NumberValidationFieldChangeListener(tlMinGpsLongitude, -180, 180, tlMaxGpsLongitude, false));
		tlMaxGpsLongitude.textProperty().addListener(new NumberValidationFieldChangeListener(tlMaxGpsLongitude, -180, 180, tlMinGpsLongitude, true));

		//Time Intervals
		tlStartInjWindowTime.textProperty().addListener(new NumberValidationFieldChangeListener(tlStartInjWindowTime, tlEndInjWindowTime, false));
		tlEndInjWindowTime.textProperty().addListener(new NumberValidationFieldChangeListener(tlEndInjWindowTime, tlStartInjWindowTime, tlEndInjRunTime));
		tlEndInjRunTime.textProperty().addListener(new NumberValidationFieldChangeListener(tlEndInjRunTime, tlEndInjWindowTime, true));
	}
	

	
	public String getTlMinGpsLongitude() {
		return tlMinGpsLongitude.getText();
	}
	
	public String getTlMaxGpsLongitude() {
		return tlMaxGpsLongitude.getText();
	}
	
	public String getTlStartInjWindowTime() {
		return tlStartInjWindowTime.getText();
	}
	
	public String getTlEndInjWindowTime() {
		return tlEndInjWindowTime.getText();
	}
	
	public String getTlEndInjRunTime() {
		return tlEndInjRunTime.getText();
	}

}
