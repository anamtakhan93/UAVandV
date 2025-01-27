package userInterface;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

public class RandomLatitudeController implements Initializable {

	@FXML
    private TextField tlMinGpsLatitude;
    @FXML
    private TextField tlMaxGpsLatitude;
    @FXML
    private TextField tlStartInjWindowTime, tlEndInjWindowTime, tlEndInjRunTime;
	
	
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		//Latitude Change Listeners
		tlMinGpsLatitude.textProperty().addListener(new NumberValidationFieldChangeListener(tlMinGpsLatitude, -90, 90, tlMaxGpsLatitude, false));
		tlMaxGpsLatitude.textProperty().addListener(new NumberValidationFieldChangeListener(tlMaxGpsLatitude, -90, 90, tlMinGpsLatitude, true));
		//Time Intervals
		tlStartInjWindowTime.textProperty().addListener(new NumberValidationFieldChangeListener(tlStartInjWindowTime, tlEndInjWindowTime, false));
		tlEndInjWindowTime.textProperty().addListener(new NumberValidationFieldChangeListener(tlEndInjWindowTime, tlStartInjWindowTime, tlEndInjRunTime));
		tlEndInjRunTime.textProperty().addListener(new NumberValidationFieldChangeListener(tlEndInjRunTime, tlEndInjWindowTime, true));
	}
	
	public String getTlMinGpsLatitude() {
		return tlMinGpsLatitude.getText();
	}
	
	public String getTlMaxGpsLatitude() {
		return tlMaxGpsLatitude.getText();
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
