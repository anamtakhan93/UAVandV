package userInterface;

import java.net.URL;
import java.util.ResourceBundle;

import dataModel.CampaignData;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class SelectFlightControlController implements Initializable {

	@FXML
	private Button btnNext;
	@FXML
	private Text tlDescription;
	@FXML
	private ImageView ivPX4, ivArduPilot, ivRotorS;
	
	
	
	private SideBarController sideBarController = null;
	private int selectedFlightControl = CampaignData.PX4_CONTROLLER;
	private Tooltip mouseToolTip = new Tooltip("");
	
	
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {

	}
	
	public void setConfigurations(SideBarController sideBarController) {
		this.sideBarController = sideBarController;
		this.selectedFlightControl = CampaignData.PX4_CONTROLLER;
		enableNextButton();
	}
	
	
	@FXML
	void onClickBtnNext(MouseEvent event) {
		//Save the data
		CampaignData campaignData = new CampaignData();
		campaignData.setFlightController(selectedFlightControl);
		// Load next Page
		sideBarController.loadSelectUAVModel(campaignData);
	}

	@FXML
	void onClickArduPilot(MouseEvent event) {
		//this.selectedFlightControl = CampaignData.ARDU_PILOT_CONTROLLER;
		//TODO
	}

	@FXML
	void onClickPX4(MouseEvent event) {
		this.selectedFlightControl = CampaignData.PX4_CONTROLLER;
		enableNextButton();
	}
	
	@FXML
    void onClickRotorS(MouseEvent event) {
		//TODO
    }
	
	@FXML
    void onMouseEnteredBtnNext(MouseEvent event) {
		//showMouseToolTip("Please, select one flight control.", event);
    }

    @FXML
    void onMouseExitBtnNext(MouseEvent event) {
    	//hideMouseToolTip();
    }
	

	public void enableNextButton() {
		btnNext.setDisable(false);
	}
	
	/**
	 * Show a pop-up with {@param msg} tooltip when mouse is over a component. 
	 * @param msg - Message to display
	 * @param event - Trigger event
	 */
	public void showMouseToolTip(String msg, MouseEvent event) {
		mouseToolTip.setText(msg);
        Node node = (Node) event.getSource();
        mouseToolTip.show(node, event.getScreenX() + 50, event.getScreenY());
	}
	
	/**
	 * Hide the pop-up tooltip when mouse is no more over the target component
	 */
	public void hideMouseToolTip() {
		mouseToolTip.hide();
	}
	

}
