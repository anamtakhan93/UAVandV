package userInterface;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

public class HijackwithAttackerController implements Initializable{

	@FXML
    private TextField tfLatitude, tfLongitude, tfAltitude, tfStartInjWindowTime, tfEndInjWindowTime, tfEndInjRunTime;
	
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		tfLatitude.textProperty().addListener(new NumberValidationFieldChangeListener(tfLatitude, -90, 90));
		tfLongitude.textProperty().addListener(new NumberValidationFieldChangeListener(tfLongitude, -180, 180));
		tfAltitude.textProperty().addListener(new NumberValidationFieldChangeListener(tfAltitude));
		//Time Intervals
		tfStartInjWindowTime.textProperty().addListener(new NumberValidationFieldChangeListener(tfStartInjWindowTime, tfEndInjWindowTime, false));
		tfEndInjWindowTime.textProperty().addListener(new NumberValidationFieldChangeListener(tfEndInjWindowTime, tfStartInjWindowTime, tfEndInjRunTime));
		tfEndInjRunTime.textProperty().addListener(new NumberValidationFieldChangeListener(tfEndInjRunTime, tfEndInjWindowTime, true));
	}
	
	public String getTfLatitude() {
		return tfLatitude.getText();
	}
	
	public String getTfLongitude() {
		return tfLongitude.getText();
	}
	
	public String getTfAltitude() {
		return tfAltitude.getText();
	}
	
	public String getTfStartInjWindowTime() {
		return tfStartInjWindowTime.getText();
	}
	
	public String getTfEndInjWindowTime() {
		return tfEndInjWindowTime.getText();
	}
	
	public String getTfEndInjRunTime() {
		return tfEndInjRunTime.getText();
	}

}
