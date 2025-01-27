package userInterface;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

public class GpsFailureRandomValueController implements Initializable {

	@FXML
    private TextField tlMinGpsLatitude, tlMinGpsLongitude, tlMinGpsAltitude;
    @FXML
    private TextField tlMaxGpsLatitude, tlMaxGpsLongitude, tlMaxGpsAltitude;
    @FXML
    private TextField tlStartInjWindowTime, tlEndInjWindowTime, tlEndInjRunTime;
	
	
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		//Latitude Change Listeners
		tlMinGpsLatitude.textProperty().addListener(new NumberValidationFieldChangeListener(tlMinGpsLatitude, -90, 90, tlMaxGpsLatitude, false));
		tlMaxGpsLatitude.textProperty().addListener(new NumberValidationFieldChangeListener(tlMaxGpsLatitude, -90, 90, tlMinGpsLatitude, true));
		//Longitude Change Listeners
		tlMinGpsLongitude.textProperty().addListener(new NumberValidationFieldChangeListener(tlMinGpsLongitude, -180, 180, tlMaxGpsLongitude, false));
		tlMaxGpsLongitude.textProperty().addListener(new NumberValidationFieldChangeListener(tlMaxGpsLongitude, -180, 180, tlMinGpsLongitude, true));
		//Latitude Change Listeners
		tlMinGpsAltitude.textProperty().addListener(new NumberValidationFieldChangeListener(tlMinGpsAltitude, tlMaxGpsAltitude, false));
		tlMaxGpsAltitude.textProperty().addListener(new NumberValidationFieldChangeListener(tlMaxGpsAltitude, tlMinGpsAltitude, true));
		//Time Intervals
		tlStartInjWindowTime.textProperty().addListener(new NumberValidationFieldChangeListener(tlStartInjWindowTime, tlEndInjWindowTime, false));
		tlEndInjWindowTime.textProperty().addListener(new NumberValidationFieldChangeListener(tlEndInjWindowTime, tlStartInjWindowTime, tlEndInjRunTime));
		tlEndInjRunTime.textProperty().addListener(new NumberValidationFieldChangeListener(tlEndInjRunTime, tlEndInjWindowTime, true));
	}
	
	public String getTlMinGpsLatitude() {
		return tlMinGpsLatitude.getText();
	}
	
	public String getTlMinGpsLongitude() {
		return tlMinGpsLongitude.getText();
	}
	
	public String getTlMinGpsAltitude() {
		return tlMinGpsAltitude.getText();
	}
	
	public String getTlMaxGpsLatitude() {
		return tlMaxGpsLatitude.getText();
	}
	
	public String getTlMaxGpsLongitude() {
		return tlMaxGpsLongitude.getText();
	}
	
	public String getTlMaxGpsAltitude() {
		return tlMaxGpsAltitude.getText();
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
