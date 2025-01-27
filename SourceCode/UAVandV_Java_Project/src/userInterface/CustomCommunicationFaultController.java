package userInterface;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;

public class CustomCommunicationFaultController implements Initializable {

	
    @FXML
    private TextField tfBW,  tfJiter,  tfLoss, tfLatency;

    @FXML
    private TextField tfStartInjWindowTime, tfEndInjWindowTime, tfEndInjRunTime;
	
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		tfBW.textProperty().addListener(new NumberValidationFieldChangeListener(tfBW));
		tfJiter.textProperty().addListener(new NumberValidationFieldChangeListener(tfJiter));
		tfLoss.textProperty().addListener(new NumberValidationFieldChangeListener(tfLoss));
		tfLatency.textProperty().addListener(new NumberValidationFieldChangeListener(tfLatency));
		//Time Intervals
		tfStartInjWindowTime.textProperty().addListener(new NumberValidationFieldChangeListener(tfStartInjWindowTime, tfEndInjWindowTime, false));
		tfEndInjWindowTime.textProperty().addListener(new NumberValidationFieldChangeListener(tfEndInjWindowTime, tfStartInjWindowTime, tfEndInjRunTime));
		tfEndInjRunTime.textProperty().addListener(new NumberValidationFieldChangeListener(tfEndInjRunTime, tfEndInjWindowTime, true));
	}
	
	
	
	
	public String getTfJiterText() {
		return tfJiter.getText();
	}
	
	public String getTfLossText() {
		return tfLoss.getText();
	}
	
	public String getTfBWText() {
		return tfBW.getText();
	}
	
	public String getTfLatencyText() {
		return tfLatency.getText();
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
