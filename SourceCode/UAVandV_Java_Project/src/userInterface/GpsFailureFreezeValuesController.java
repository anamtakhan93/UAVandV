package userInterface;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

public class GpsFailureFreezeValuesController implements Initializable {

	@FXML
    private TextField tfStartInjWindowTime, tfEndInjWindowTime, tfEndInjRunTime;

	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		//Time Intervals
		tfStartInjWindowTime.textProperty().addListener(new NumberValidationFieldChangeListener(tfStartInjWindowTime, tfEndInjWindowTime, false));
		tfEndInjWindowTime.textProperty().addListener(new NumberValidationFieldChangeListener(tfEndInjWindowTime, tfStartInjWindowTime, tfEndInjRunTime));
		tfEndInjRunTime.textProperty().addListener(new NumberValidationFieldChangeListener(tfEndInjRunTime, tfEndInjWindowTime, true));
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
