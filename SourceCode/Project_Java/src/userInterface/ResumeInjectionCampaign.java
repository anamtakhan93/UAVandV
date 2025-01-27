package userInterface;

import java.net.URL;
import java.util.ResourceBundle;

import dataModel.CampaignData;
import generationFaults.GenerationTool;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class ResumeInjectionCampaign implements Initializable {

	@FXML
    private AnchorPane apResumeInjCampaign;
    @FXML
    private TextField tfCampaingName,  tfResponsable;
    @FXML
    private TextArea taResume;
    @FXML
    private Button btnGenerateCampagin;

    
    private CampaignData campaignData;
    private SideBarController sideBarController;
    
	
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		tfCampaingName.textProperty().addListener(new GenerateCampaignChangeListener(tfCampaingName, btnGenerateCampagin, tfResponsable));
		tfResponsable.textProperty().addListener(new GenerateCampaignChangeListener(tfResponsable, btnGenerateCampagin, tfCampaingName));
	}
	
	public void setConfigurations(SideBarController sideBarController, CampaignData campaignData) {
		this.sideBarController = sideBarController;
		this.campaignData = campaignData;
		//Put Campaign Information in the ListView
		taResume.setText(campaignData.getResume());
	}
	
	@FXML
    void onClickGenerate(MouseEvent event) {
		GenerationTool gen = new GenerationTool(campaignData, apResumeInjCampaign, tfCampaingName.getText());
		try {
			gen.saveFaults();
			sideBarController.setGeneratingCampaign(false);
			CommunInterfaceFunctions.showAletDialog("Campaign has been saved successfully!");
		} catch (Exception e) {
			CommunInterfaceFunctions.showAletDialog(e.getMessage());
		}
    }	

}
