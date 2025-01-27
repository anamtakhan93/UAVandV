package userInterface;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class GenerateCampaignChangeListener implements ChangeListener<String> {

	
	TextField tf;
	TextField aux;
	Button btn;
	
	public GenerateCampaignChangeListener(TextField tf, Button btn, TextField aux) {
		super();
		this.tf = tf;
		this.btn = btn;
		this.aux = aux;
	}
	
	@Override
	public void changed(ObservableValue<? extends String> observableValue, String oldV, String newV) {
		char [] value = newV.toCharArray();
		boolean change = false;
		if(newV == "") {
			tf.setText(oldV);
			btn.setDisable(true);
			return;
		}
		for(int i=0; i<value.length; i++) {
			if(value[i] != ' ') {
				change = true;
				break;
			}
		}
		if(change) {
			tf.setText(newV);
			btn.setDisable(false);
		}else {
			tf.setText(oldV);
			btn.setDisable(true);
		}
		
		//Verify the other TextField
		if(!verifyAux())
			btn.setDisable(true);
	}
	
	public boolean verifyAux() {
		char [] value = aux.getText().toCharArray();
		boolean change = false;
		if(aux.getText() == "")
			return false;
		for(int i=0; i<value.length; i++) {
			if(value[i] != ' ') {
				change = true;
				break;
			}
		}
		return change;
	}


}
