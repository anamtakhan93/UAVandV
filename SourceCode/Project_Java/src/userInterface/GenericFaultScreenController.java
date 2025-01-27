package userInterface;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.control.Button;

public class GenericFaultScreenController implements Initializable {

	@FXML
    private TextField v1, v2, v3, v4, v5, v6,start_time,end_time;
	@FXML
    private Text l1, l2, l3, l4, l5, l6, l7, title;
	@FXML
    private Button v7;
	
	
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		v1.setDisable(true);
		v2.setDisable(true);
		v3.setDisable(true);
		v4.setDisable(true);
		v5.setDisable(true);
		v6.setDisable(true);
		v7.setDisable(true);
		start_time.textProperty().addListener(new NumberValidationFieldChangeListener(start_time, end_time, false));
		end_time.textProperty().addListener(new NumberValidationFieldChangeListener(start_time, end_time, true));
		v1.textProperty().addListener(new NumberValidationFieldChangeListener(v1));
		v2.textProperty().addListener(new NumberValidationFieldChangeListener(v2));
		v3.textProperty().addListener(new NumberValidationFieldChangeListener(v3));
		v4.textProperty().addListener(new NumberValidationFieldChangeListener(v4));
		v5.textProperty().addListener(new NumberValidationFieldChangeListener(v5));
		v6.textProperty().addListener(new NumberValidationFieldChangeListener(v6));
	}

	
	
	public void SetEnable_1(boolean val, String Label_text) {
		v1.setText("0");
		v1.setDisable(!val);
		if(val) {
			l1.setText(Label_text);
		}
		else {
			l1.setText("-");
		}
	}	
	public void SetEnable_2(boolean val, String Label_text) {
		v2.setText("0");
		v2.setDisable(!val);
		if(val) {
			l2.setText(Label_text);
		}
		else {
			l2.setText("-");
		}
	}	
	public void SetEnable_3(boolean val, String Label_text) {
		v3.setText("0");
		v3.setDisable(!val);
		if(val) {
			l3.setText(Label_text);
		}
		else {
			l3.setText("-");
		}
	}	
	public void SetEnable_4(boolean val, String Label_text) {
		v4.setText("0");
		v4.setDisable(!val);
		if(val) {
			l4.setText(Label_text);
		}
		else {
			l4.setText("-");
		}
	}	
	public void SetEnable_5(boolean val, String Label_text) {
		v5.setText("0");
		v5.setDisable(!val);
		if(val) {
			l5.setText(Label_text);
		}
		else {
			l5.setText("-");
		}
	}	
	public void SetEnable_6(boolean val, String Label_text) {
		v6.setText("0");
		v6.setDisable(!val);
		if(val) {
			l6.setText(Label_text);
		}
		else {
			l6.setText("-");
		}
	}	
	public void SetEnable_7(boolean val, String Label_text) {
		v7.setDisable(!val);
		if(val) {
			l7.setText(Label_text);
		}
		else {
			l7.setText("-");
		}
	}
	
	public void SetEnable_1(boolean val) {
		v1.setText("0");
		v1.setDisable(!val);
		l1.setText("-");
	}
	public void SetEnable_2(boolean val) {
		v2.setText("0");
		v2.setDisable(!val);
		l2.setText("-");
	}
	public void SetEnable_3(boolean val) {
		v3.setText("0");
		v3.setDisable(!val);
		l3.setText("-");
	}
	public void SetEnable_4(boolean val) {
		v4.setText("0");
		v4.setDisable(!val);
		l4.setText("-");
	}
	public void SetEnable_5(boolean val) {
		v5.setText("0");
		v5.setDisable(!val);
		l5.setText("-");
	}
	public void SetEnable_6(boolean val) {
		v6.setText("0");
		v6.setDisable(!val);
		l6.setText("-");
	}
	public void SetEnable_7(boolean val) {
		v7.setDisable(!val);
		//l7.setDisable(!val);
		l7.setText("-");
	}
	
	public String GetValue_1() {
		return v1.getText();
	}	
	public String GetValue_2() {
		return v2.getText();
	}	
	public String GetValue_3() {
		return v3.getText();
	}	
	public String GetValue_4() {
		return v4.getText();
	}	
	public String GetValue_5() {
		return v5.getText();
	}	
	public String GetValue_6() {
		return v6.getText();
	}	
	public String GetValue_startTime() {
		return start_time.getText();
	}	
	public String GetValue_endTime() {
		return end_time.getText();
	}
	
	public void SetTitle(String Label_text) {
		title.setText(Label_text);
	}
	
	public void Reset() {
		title.setText("Fault Type");
		SetEnable_1(false);
		SetEnable_2(false);
		SetEnable_3(false);
		SetEnable_4(false);
		SetEnable_5(false);
		SetEnable_6(false);
	}



}
