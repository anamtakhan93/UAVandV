package userInterface;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

public class ForceUAVLandingController implements Initializable {

	@FXML
    private TextField tlMinGpsAltitude;
    @FXML
    private TextField tlMaxGpsAltitude;
    @FXML
    private TextField tlStartInjWindowTime, tlEndInjWindowTime, tlEndInjRunTime;
	
	
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		//Latitude Change Listeners
		tlMinGpsAltitude.textProperty().addListener(new NumberValidationFieldChangeListener(tlMinGpsAltitude, tlMaxGpsAltitude, false));
		tlMaxGpsAltitude.textProperty().addListener(new NumberValidationFieldChangeListener(tlMaxGpsAltitude, tlMinGpsAltitude, true));
		//Time Intervals
		tlStartInjWindowTime.textProperty().addListener(new NumberValidationFieldChangeListener(tlStartInjWindowTime, tlEndInjWindowTime, false));
		tlEndInjWindowTime.textProperty().addListener(new NumberValidationFieldChangeListener(tlEndInjWindowTime, tlStartInjWindowTime, tlEndInjRunTime));
		tlEndInjRunTime.textProperty().addListener(new NumberValidationFieldChangeListener(tlEndInjRunTime, tlEndInjWindowTime, true));
	}
	
	public String getTlMinGpsAltitude() {
		return tlMinGpsAltitude.getText();
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
