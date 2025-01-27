package userInterface;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import CSVModule.CSVReader;
import dataModel.CampaignData;
import dataModel.UAVModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SelectUavModelController implements Initializable{

	
	public final static String ALL_UAVS ="All UAV's";
	
	
	@FXML
    private AnchorPane apSelectUAVModel;
    @FXML
    private Text tlDescription, tlDescription1, lbGezeboWorld;
    @FXML
    private Text lbUAVSelected;
    @FXML
    private ListView<String> listViewUAV;
    @FXML
    private TextField tfID; //tfModel, tfIp
    @FXML
    private Button btnPlusUpload, btnPlusIpAndModel, btnNextUAV, btnSelectGezeboWorld;
    @FXML
    private RadioButton rbtnIP, rbtnUpload;
    private ToggleGroup rbtnGroup;
    
    
    
    private SideBarController sideBarController = null;
    private CampaignData campaignData = null;
    private boolean gezeboWorldSelected = false;
    private String gezeboWorldPath = null;
    
    @Override
	public void initialize(URL url, ResourceBundle rb) {
		//getUavsFromConfigurationFile("uavModels.csv");
		//tfIp.textProperty().addListener(new IPValidationChangeListener(tfIp));
		//createToggleGroup();
	}
    
    public void setConfiguration(SideBarController sideBarController, CampaignData campaignData) {
    	this.sideBarController = sideBarController;
    	this.campaignData = campaignData;
    }

    @FXML
    void onClickBtnNext(MouseEvent event) {
    	// Load Configure Injection Campaign Page
    	sideBarController.loadConfigureInjectionCampaign(campaignData);
    }
    
    @FXML
    void onBtnPlusIpModelClicked(MouseEvent event) {
    	String missionPlanPath = null;
    	
    	//Validate Model Name
    	//if(tfModel.getText().isEmpty()) {
    	//	CommunInterfaceFunctions.showAletDialog("Please, insert the UAV Model.");
    	//	return;
    	//}
    	//Validate IP address
    	//if(!IPValidationChangeListener.validIP(tfIp.getText())){
    	//	CommunInterfaceFunctions.showAletDialog("Please, insert a valid IP adress.");
    	//	return;
    	//}
    	//Validate ID
    	if(tfID.getText().isEmpty()) {
    		CommunInterfaceFunctions.showAletDialog("Please, insert an unique ID for the drone.");
    		return;
    	}
    	
    	//Get Mission plan file path
    	FileChooser fileChooser = CommunInterfaceFunctions.getMissionPlanChooser();
    	Stage stage = (Stage) apSelectUAVModel.getScene().getWindow();	
		try {
			File file = fileChooser.showOpenDialog(stage);
			missionPlanPath = file.getCanonicalPath();
			campaignData.setMissionPlanPath(missionPlanPath);
		} catch (Exception e) {
			CommunInterfaceFunctions.showAletDialog("An error occurs uploading Mission Plan File.");
			return;
		}
		if(missionPlanPath == null)
			return;
    	//Enable Next Button
    	if(gezeboWorldSelected)
	    	btnNextUAV.setDisable(false);
    	
    	//Create UAV
    	UAVModel newUAV = new UAVModel(tfID.getText(), "127.0.0.1", "iris", missionPlanPath);
    	String [] split = missionPlanPath.split("\\\\");
    	try {
			campaignData.addUAVModel(newUAV);
			listViewUAV.getItems().add(newUAV.getModel() + " | " + newUAV.getIp() + " | " + split[split.length-1]);
		} catch (Exception e) {
			CommunInterfaceFunctions.showAletDialog("UAV Model already existis!");
		}
    }

    
    @FXML
    void onBtnPlusUploadClicked(MouseEvent event){
    	FileChooser fileChooser = CommunInterfaceFunctions.getCSVFileChooser();
    	Stage stage = (Stage) apSelectUAVModel.getScene().getWindow();	
		try {
			File file = fileChooser.showOpenDialog(stage);
			String filePath = file.getCanonicalPath();
			getUavsFromConfigurationFile(filePath);
		} catch (Exception e) {}
		
    }
    
    @FXML
    void onSelectGezeboWorld(MouseEvent event) {
    	FileChooser fileChooser = CommunInterfaceFunctions.getGezeboFileChooser();
    	Stage stage = (Stage) apSelectUAVModel.getScene().getWindow();	
		try {
			File file = fileChooser.showOpenDialog(stage);
			gezeboWorldPath = file.getCanonicalPath();
			lbGezeboWorld.setText(gezeboWorldPath);
			campaignData.setGezeboWorldPath(gezeboWorldPath);
			gezeboWorldSelected = true;
		} catch (Exception e) {}
    	//Enable Next Button
    	if(gezeboWorldSelected)
	    	btnNextUAV.setDisable(false);
    }
    
	
    private void getUavsFromConfigurationFile(String filePath) {
    	try {
			for (UAVModel uav : CSVReader.readUAVSFromCSV(filePath)) { 
				campaignData.addUAVModel(uav);
				listViewUAV.getItems().add(uav.getModel() + " | " + uav.getIp() + " | " + uav.getIp());
			}
		} catch (Exception e) {
			CommunInterfaceFunctions.showAletDialog(e.getMessage());
		}
    }
    

    
    /*
     * Create the Toogle Group responsible for the Radio Buttons
     */
    private void createToggleGroup() {
    	rbtnGroup = new ToggleGroup();
    	rbtnIP.setToggleGroup(rbtnGroup);
    	rbtnUpload.setToggleGroup(rbtnGroup);
    	rbtnGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> ob, Toggle o, Toggle n) {
				RadioButton rb = (RadioButton)rbtnGroup.getSelectedToggle(); 
                if (rb != null) {
                	if(rb.getId().equals("rbtnIP")) {
                		btnPlusIpAndModel.setDisable(false);
                		//tfIp.setDisable(false);
                		//tfModel.setDisable(false);
                		tfID.setDisable(false);
                		btnPlusUpload.setDisable(true);
                	}else { //Radio Button Upload
                		btnPlusIpAndModel.setDisable(true);
                		//tfIp.setDisable(true);
                		//tfModel.setDisable(true);
                		tfID.setDisable(true);
                		btnPlusUpload.setDisable(false);
                	}
                }
			}
		});
    }
        
    
 
}
