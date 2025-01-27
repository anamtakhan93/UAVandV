package userInterface;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

public class IPValidationChangeListener implements ChangeListener<String> {

	
	TextField tf;
	
	public IPValidationChangeListener(TextField tf) {
		super();
		this.tf = tf;
	}
	
	@Override
	public void changed(ObservableValue<? extends String> observableValue, String oldV, String newV) {
		boolean change = validIP(newV);
		
		if(change) {
			tf.setText(newV);
			tf.setStyle("");
		}
		else {
			tf.setText(oldV);
			if(!validIP(oldV))
				tf.setStyle("-fx-text-box-border: #B22222; -fx-focus-color: #B22222;");
			else
				tf.setStyle("");
		}
	}
	
	public static boolean validIP (String ip) {
	    try {
	        if ( ip == null || ip.isEmpty() ) {
	            return false;
	        }

	        String[] parts = ip.split( "\\." );
	        if ( parts.length != 4 ) {
	            return false;
	        }

	        for ( String s : parts ) {
	            int i = Integer.parseInt( s );
	            if ( (i < 0) || (i > 255) ) {
	                return false;
	            }
	        }
	        if ( ip.endsWith(".") ) {
	            return false;
	        }

	        return true;
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	}

}
