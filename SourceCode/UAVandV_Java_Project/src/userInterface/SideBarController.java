package userInterface;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import dataModel.CampaignData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SideBarController implements Initializable {
	
	
	// PAGES CODES
	public final static int HOME_PAGE_CODE = 0;                           // Home Page Code
	public final static int SELECT_FLIGHT_CONTROL_PAGE_CODE = 1;		  // Select Flight Control Page Code
	public final static int SELECT_UAV_MODEL_PAGE_CODE = 2;				  // Select UAV Model Page Code
	public final static int CONFIGURE_INJECTION_CAMPAIGN_PAGE_CODDE = 3;  // Configure Injection Campaign Page Code
	public final static int RESUME_INJECTION_CAMPAIGN_PAGE_CODE = 4;	  // Resume Injection Campaign Page Code
	public final static int EXECUTE_CAMPAIGN_PAGE_CODE = 5;				  // Execution Campaign Page Code
		
	@FXML
    private BorderPane bp;
    @FXML
    private ImageView ivToolLogo;
    @FXML
    private Button btnHomePage, btnDefineCampaign, btnExecuteCampaign, btnAbout;
    @FXML
    private AnchorPane ap;
    
    
    
    
    
    private Pane root = null;
    private int actualPage = HOME_PAGE_CODE;
    private Button currentBtn = null;
    
    // Controllers
    private HomePageController homePageController = null;
    private SelectFlightControlController selectFlightControlController = null;
    private SelectUavModelController selectUAVModelController = null;
    private ConfigureInjectionCampaignController configureInjCampaignController = null;
    private ResumeInjectionCampaign resumeInjectionCampaignController = null;
    private CampaignExecutionController campaignExecutionController= null;
    
    private boolean generatingCampaign = false;
    private boolean executingCampaign = false;
    

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		this.loadHomePage();
	}

    @FXML
    void onClickButtonAbout(MouseEvent event) {

    }

    @FXML
    void onClickDefineCampaign(MouseEvent event) {
    	switch(actualPage) {
    	case HOME_PAGE_CODE:
    		loadSelectFlightControlPage(btnHomePage);
    		break;
    	}
    }

    @FXML
    void onClickExecuteCampaign(MouseEvent event) {
    	
    	switch(actualPage) {
    	case HOME_PAGE_CODE:
    		try {
				loadExecuteCampaign(getFaultsFilePath(), btnHomePage);
			} catch (IOException e) {
				//Add some feedback to the user. An alert window should be good. 
				return;
			} 	
    		break;
    	}
    	
    	
    	
    }
    
    public String getFaultsFilePath() throws IOException {
    	//Get Faults File Path
    	FileChooser fileChooser =  CommunInterfaceFunctions.getCSVFileChooser();
    	Stage stage = (Stage) bp.getScene().getWindow();	
    	File file = fileChooser.showOpenDialog(stage);
    	String filePath = null;
		filePath = file.getCanonicalPath();
		return filePath;
    }

    @FXML
    void onClickHomePage(MouseEvent event) {
    	boolean goTo = true;
    	if(generatingCampaign) {
    		//Ask if the user want to lose all data
    		goTo = CommunInterfaceFunctions.showAletDialogWithResponse("All data will be loss. Do you want to continue?");
    	}
    	if(goTo) {
    		generatingCampaign = false;
    		loadHomePage(currentBtn);
    	}
    }
    
    /**
	 * Change the color of the menu buttons
	 * @param btn to change the color
	 * @param selected - TRUE if is the button selected | FALSE if is the previous button selected
	 */
	private void changeMenuButtonColor(Button btn, boolean selected) {
		if (selected) {
			if (btn == btnHomePage)
				btn.setStyle(
						"-fx-background-color: #7f9eb2; -fx-border-color: white; -fx-border-width: 2px 0px 2px 0px;");
			else
				btn.setStyle(
						"-fx-background-color: #7f9eb2; -fx-border-color: white; -fx-border-width: 0px 0px 2px 0px;");
		} else {
			if(btn == btnHomePage)
				btn.setStyle(
						"-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2px 0px 2px 0px;");
			else
				btn.setStyle(
						"-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 0px 0px 2px 0px;");
		}
	}
	
	/**
	 * Load a page to the center of the screen
	 * @param page name
	 */
	private void loadPage(String page, int pageCode) {
		
		try {
			FXMLLoader fxmlLoader = new FXMLLoader((getClass().getResource(page + ".fxml")));
			fxmlLoader.setResources(null);
			root = fxmlLoader.load();
			switch (pageCode) {
			case HOME_PAGE_CODE:
				homePageController = fxmlLoader.getController();
				this.actualPage = HOME_PAGE_CODE;
				this.currentBtn = btnHomePage;
				break;
			case SELECT_FLIGHT_CONTROL_PAGE_CODE:
				selectFlightControlController = fxmlLoader.getController();
				this.actualPage = SELECT_FLIGHT_CONTROL_PAGE_CODE;
				this.currentBtn = btnDefineCampaign;
				break;
			case SELECT_UAV_MODEL_PAGE_CODE:
				selectUAVModelController = fxmlLoader.getController();
				this.actualPage = SELECT_UAV_MODEL_PAGE_CODE;
				this.currentBtn = btnDefineCampaign;
				break;
			case CONFIGURE_INJECTION_CAMPAIGN_PAGE_CODDE:
				configureInjCampaignController = fxmlLoader.getController();
				this.actualPage = CONFIGURE_INJECTION_CAMPAIGN_PAGE_CODDE;
				this.currentBtn = btnDefineCampaign;
				break;
			case RESUME_INJECTION_CAMPAIGN_PAGE_CODE:
				resumeInjectionCampaignController = fxmlLoader.getController();
				this.actualPage = RESUME_INJECTION_CAMPAIGN_PAGE_CODE;
				this.currentBtn = btnDefineCampaign;
				break;
			case EXECUTE_CAMPAIGN_PAGE_CODE:
				campaignExecutionController = fxmlLoader.getController();
				this.actualPage = EXECUTE_CAMPAIGN_PAGE_CODE;
				this.currentBtn = btnExecuteCampaign;
				break;
			default:
				break;
			}	
		} catch (IOException e) {
			return;
		}
		bp.setCenter(root);
	}
	
	
	public void loadHomePage(Button previousButton) {
		loadPage("HomePage", HOME_PAGE_CODE);
		this.homePageController.setConfigurations(this);
		if(previousButton != null)
			changeMenuButtonColor(previousButton, false);
		changeMenuButtonColor(btnHomePage, true);
	}
	
	public void loadHomePage() {
		loadPage("HomePage", HOME_PAGE_CODE);
		this.homePageController.setConfigurations(this);
		changeMenuButtonColor(btnHomePage, true);
	}
	
	public void loadSelectFlightControlPage(Button previousButton) {
		loadPage("SelectFlightControl", SELECT_FLIGHT_CONTROL_PAGE_CODE);
		this.selectFlightControlController.setConfigurations(this);
		changeMenuButtonColor(previousButton, false);
		changeMenuButtonColor(btnDefineCampaign, true);
		generatingCampaign = true;
	}
	
	public void loadSelectUAVModel(CampaignData campaignDta) {
		loadPage("SelectUavModel", SELECT_UAV_MODEL_PAGE_CODE);
		this.selectUAVModelController.setConfiguration(this, campaignDta);
		generatingCampaign = true;
	}
	
	public void loadConfigureInjectionCampaign(CampaignData campaignData) {
		loadPage("ConfigureInjectionCampaign", CONFIGURE_INJECTION_CAMPAIGN_PAGE_CODDE);
		this.configureInjCampaignController.setConfigurations(this, campaignData);
		generatingCampaign = true;
	}
	
	public void loadResumeInjectionCampaign(CampaignData campaignData) {
		loadPage("ResumeInjectionCampaign", RESUME_INJECTION_CAMPAIGN_PAGE_CODE);
		this.resumeInjectionCampaignController.setConfigurations(this, campaignData);
		generatingCampaign = true;
	}
	
	
	public void loadExecuteCampaign(String filePath, Button previousButton) {
		loadPage("CampaignExecution", EXECUTE_CAMPAIGN_PAGE_CODE);
		this.campaignExecutionController.defineSettings(filePath);
		if(previousButton != null)
			changeMenuButtonColor(previousButton, false);
		changeMenuButtonColor(btnExecuteCampaign, true);
		generatingCampaign = false;
		executingCampaign = true;
	}
	
	
	
	/*
	 * 
	 * 
	 * Getters and Setters
	 * 
	 * 
	 */
	public boolean isGeneratingCampaign() {
		return generatingCampaign;
	}

	public void setGeneratingCampaign(boolean generatingCampaign) {
		this.generatingCampaign = generatingCampaign;
	}
	
	
	

}
