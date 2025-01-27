package userInterface;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class NumberValidationFieldChangeListener implements ChangeListener<String> {

	
	TextField tf;
	TextField other;
	TextField tfMin;
	TextField tfMax;
	boolean tfInterval = false;
	boolean limits = false;
	boolean comparation = false;  // True if the is to compare the value with another Text Field
	boolean above = false;		  // False if the value should be below the other | True if should be above
	int max = 0;
	int min = 0;
	
	/**
	 * Just to verify if the input text is a number
	 * @param tf
	 */
	public NumberValidationFieldChangeListener(TextField tf) {
		super();
		this.tf = tf;
	}
	
	/**
	 * Verify if the value if between the value of two other input text
	 * @param tf
	 * @param tfMin
	 * @param tfMax
	 */
	public NumberValidationFieldChangeListener(TextField tf, TextField tfMin, TextField tfMax) {
		super();
		this.tf = tf;
		this.tfMin = tfMin;
		this.tfMax = tfMax;
		this.tfInterval = true;
	}
	
	/**
	 * To Verify if the input text is a number and compare them with other input text value
	 * @param tf
	 * @param other
	 * @param above
	 */
	public NumberValidationFieldChangeListener(TextField tf, TextField other, boolean above) {
		super();
		this.tf = tf;
		this.above = above;
		this.other = other;
		this.comparation = true;
	}
	
	/**
	 * Verify if the input text is a number and if it is between a min and max value
	 * @param tf
	 * @param min
	 * @param max
	 */
	public NumberValidationFieldChangeListener(TextField tf, int min, int max) {
		super();
		this.tf = tf;
		this.limits = true;
		this.min = min;
		this.max = max;
	}
	
	/**
	 * Verify if the input text is a number, if it is between a min and max value, and compare them with other value
	 * @param tf
	 * @param min
	 * @param max
	 * @param other
	 * @param above
	 */
	public NumberValidationFieldChangeListener(TextField tf, int min, int max, TextField other, boolean above) {
		super();
		this.tf = tf;
		this.limits = true;
		this.min = min;
		this.max = max;
		this.above = above;
		this.other = other;
		this.comparation = true;
	}
	
	@Override
	public void changed(ObservableValue<? extends String> observableValue, String oldV, String newV) {
		try {

            int value = Integer.parseInt(newV);
            if(!tfInterval) {
	            if(!limits && !comparation) { //If no limits, just put the new value
	            	tf.setText(newV);
	            }
	            else if(limits && !comparation){ //If have limits but is not to compare with other value
	            	if(value >= min && value <= max)
	            		tf.setText(newV);
	            	else
	            		tf.setText(oldV);
	            }
	            else if(!limits && comparation) { //If have no limits bit is to compare with other value
	            	int otherValue = Integer.parseInt(other.getText());
	        		if(above) {
	        			if(value >= otherValue) //Above the other value
	        				tf.setText(newV);
	        			else
	        				tf.setText(oldV);
	        		}else {
	        			if(value <= otherValue) 	//Below the other value
	        				tf.setText(newV);
	        			else
	        				tf.setText(oldV);
	        		}
	            }
	            else if(limits && comparation) { //If have limits and is to compare with other value
	            	if(value >= min && value <= max) {
	            		int otherValue = Integer.parseInt(other.getText());
	            		if(above) {
	            			if(value >= otherValue) //Above the other value
	            				tf.setText(newV);
	            			else
	            				tf.setText(oldV);
	            		}else {
	            			if(value <= otherValue) 	//Below the other value
	            				tf.setText(newV);
	            			else
	            				tf.setText(oldV);
	            		}
	            	}
	            	else { 
	            		tf.setText(oldV);
	            	}
	            }
            }
            else { //End if(!tfInterval)  Verify if the value is between two other input text values
            	int minTf = Integer.parseInt(tfMin.getText());
            	int maxTf = Integer.parseInt(tfMax.getText());
            	if(value >= minTf && value <= maxTf)
            		tf.setText(newV);
            	else
            		tf.setText(oldV);
            }
        } catch (Exception e) { //If is not a number
            tf.setText(oldV);
        }
	}
	


}
