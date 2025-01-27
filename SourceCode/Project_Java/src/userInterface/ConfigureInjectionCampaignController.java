package userInterface;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.controlsfx.control.CheckComboBox;

import dataModel.CampaignData;
import dataModel.UAVModel;
import faults.CustomCommunicationFault;
import faults.Fault;
import faults.ForceUAVLanding;
import faults.GenericFault;
import faults.GpsModuleDelayedValues;
import faults.GpsModuleFixedValues;
import faults.GpsModuleFreezeValues;
import faults.GpsModuleMinMaxValues;
import faults.GpsModuleRandomValues;
import faults.RandomLatitude;
import faults.RandomLongitude;
import generationFaults.GenerationTool;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class ConfigureInjectionCampaignController implements Initializable {

	@FXML
	private AnchorPane apConfigureInjCampaign;
	@FXML
	private ListView<String> lvFaultsResume;
	@FXML
	private BorderPane bpFaultDefine;
	@FXML
	private Button btnNextCIC, btnAddFaultType;
	@FXML
	private TextField tfNumberOfFaults;
	@FXML
	private ComboBox<String> cbFaultType, cbFaultTarget, cbFaultSubType;
	@FXML
	private MenuButton uavMenuItems;

	private SideBarController sideBarController = null;
	private CampaignData campaignData = null;
	private Pane root = null;
	private ArrayList<String> selectedUavs = new ArrayList<>();

	// ComboBox Option
	private String[] targetOptions = new String[4];
	private String[] ImuOptions = new String[7];
	private String[] GpsOptions = new String[17];
	private String[] communicationOptions = new String[1];
	private String[] faultTypeOption = new String[2];

	private boolean isFaultTypeSelected = false;
	private boolean isFaultTargetSelected = false;
	private boolean isFaultSubTypeSelected = false;

	private String faultTypeSelected = null;
	private String faultTargetSelected = null;
	private String faultSubTypeSelected = null;

	private Initializable controllerFaultConf = null;
	private GenericFaultScreenController screenController = null;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		tfNumberOfFaults.textProperty().addListener(new NumberValidationFieldChangeListener(tfNumberOfFaults));
		btnNextCIC.setDisable(false);
		controllerFaultConf = loadFaultDefinitionPage("GenericFaultScreen");
		screenController = (GenericFaultScreenController) controllerFaultConf;
	}

	public void setConfigurations(SideBarController sideBarController, CampaignData campaignData) {
		this.sideBarController = sideBarController;
		this.campaignData = campaignData;
		configureComboBoxs();
		this.configureSelectUAVList();
	}

	@FXML
	void onClickBtnNext(MouseEvent event) {
		sideBarController.loadResumeInjectionCampaign(campaignData);
	}

	@FXML
	void onClickBtnAddFaultType(MouseEvent event) {
		if(selectedUavs.isEmpty()) {
			CommunInterfaceFunctions.showAletDialog("Please, select the UAV Models that you want to disturb.");
			return;
		}
		SaveData();
//		switch (faultSubTypeSelected) {
//		// Software Failures
//		case "Random Values":
//			saveGpsModuleRandomValues();
//			break;
//		case "Fixed Values":
//			saveGpsModuleFixedValues();
//			break;
//		case "Freeze Values":
//			saveGpsModuleFreezeValues();
//			break;
//		case "Delayed Values":
//			saveGpsModuleDelayedValues();
//			break;
//		case "Min/Max Values":
//			saveGpsModuleMinMaxValues();
//			break;
//			// Communication Failures
//		case "Custom Type":
//			saveCustomCommunicationFaultValues();
//			break;
//			// Security Attacks
//		case "Random Longitude":
//			saveRandomLongitude();
//			break;
//		case "Random Latitude":
//			saveRandomLatitude();
//			break;
//		case "Random Position":
//			saveRandomPosition();
//			break;
//		case "GPS Delay":
//			saveGpsDelay();
//			break;
//		case "Force UAV Landing":
//			saveForceUAVLanding();
//			break;
//		case "Hijack with a second UAV":
//			saveHijackSecondUAV();
//			break;
//		case "Hijack with attacker�s specified position":
//			saveHijackWithAttacker();
//			break;
//		default:
//			break;
//		}

	}

	private Initializable loadFaultDefinitionPage(String page) {
		try {
			//System.out.println("Loading screen : "+page);
			FXMLLoader fxmlLoader = new FXMLLoader((getClass().getResource(page + ".fxml")));
			fxmlLoader.setResources(null);
			root = fxmlLoader.load();
			bpFaultDefine.setCenter(root);
			return fxmlLoader.getController();
		} catch (IOException e) {
			//System.out.println("Could not load screen : "+page);
			// e.printStackTrace();
			return null;
		}
	}

	private void configureComboBoxs() {
		// Fault Type Options
		//faultTypeOption[0] = "Gold Run";
		faultTypeOption[0] = "Gold Run";
		faultTypeOption[1] = "Faulty Run";
		//faultTypeOption[1] = "Use Physics model";
		//faultTypeOption[2] = "Use ML model";
		//faultTypeOption[3] = "Use Hybrid model";
		cbFaultType.setItems(FXCollections.observableArrayList(faultTypeOption));

		// Target Option
		targetOptions[0] = "GPS";
		targetOptions[1] = "IMU";
		targetOptions[2] = "ACC";
		targetOptions[3] = "GYRO";
		cbFaultTarget.setItems(FXCollections.observableArrayList(targetOptions));

		// Software Failure Options
		GpsOptions[0] = "Fixed Values";
		GpsOptions[1] = "Fixed Noise";
		GpsOptions[2] = "Random Noise";
		GpsOptions[3] = "Freeze Values";
		GpsOptions[4] = "Random Values";
		GpsOptions[5] = "Zigzag Values";
		GpsOptions[6] = "Min Latitude";
		GpsOptions[7] = "Max Latitude";
		GpsOptions[8] = "Min Longitude";
		GpsOptions[9] = "Max Longitude";
		GpsOptions[10] = "Min Altitude";
		GpsOptions[11] = "Max Altitude";
		GpsOptions[12] = "Hijack By a UAV";
		GpsOptions[13] = "Force UAV Landing";
		GpsOptions[14] = "Random Longitude";
		GpsOptions[15] = "Random Latitude";
		GpsOptions[16] = "Custom Fault";

		// Security Attacks Options
		ImuOptions[0] = "Fixed Values";
		ImuOptions[1] = "Freeze Values";
		ImuOptions[2] = "Random Values";
		ImuOptions[3] = "Random Noise";
		ImuOptions[4] = "Min Values";
		ImuOptions[5] = "Max Values";
		ImuOptions[6] = "Custom Fault";

		// Communication Failures Options
		communicationOptions[0] = "No Faults";
		
		// Event Listener of Fault Type Combo Box
		cbFaultType.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (!isFaultTypeSelected)
					isFaultTypeSelected = true;
				faultTypeSelected = cbFaultType.getValue();

				isFaultSubTypeSelected = false;
				isFaultTargetSelected = false;
				//faultSubTypeSelected = null;
				//faultTargetSelected = null;

//				switch (faultTypeSelected) {
//				case "Gold Run":
//					cbFaultSubType.setItems(FXCollections.observableArrayList(communicationOptions));
//					cbFaultTarget.setItems(FXCollections.observableArrayList(communicationOptions));
//					break;
//				default:
//					cbFaultSubType.setItems(FXCollections.observableArrayList(GpsOptions));
//					cbFaultTarget.setItems(FXCollections.observableArrayList(targetOptions));
//					break;
//				}

			}
		});

		// Event Listener of Fault Target Combo Box
		cbFaultTarget.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (!isFaultTargetSelected)
					isFaultTargetSelected = true;
				faultTargetSelected = cbFaultTarget.getValue();
				isFaultSubTypeSelected = false;
				faultSubTypeSelected = null;
				screenController.Reset();
				
				switch (faultTargetSelected) {
				case "GPS":
					cbFaultSubType.setItems(FXCollections.observableArrayList(GpsOptions));
					break;
				case "IMU":
					cbFaultSubType.setItems(FXCollections.observableArrayList(ImuOptions));
					break;
				case "ACC":
					cbFaultSubType.setItems(FXCollections.observableArrayList(ImuOptions));
					break;
				case "Gyro":
					cbFaultSubType.setItems(FXCollections.observableArrayList(ImuOptions));
					break;
				default:
					break;
				}
				
			}
		});

		// Event Listener of Fault sub-Type Combo Box
		cbFaultSubType.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (!isFaultSubTypeSelected)
					isFaultSubTypeSelected = true;
				faultSubTypeSelected = cbFaultSubType.getValue();

				screenController.Reset();
				// Call method that shows the screens
				selectScreenToLoad();
			}
		});
	}

	private void configureSelectUAVList() {
		final List<CheckMenuItem> options = new ArrayList<>();
		for (UAVModel uav : campaignData.getUAVModels())
			options.add(new CheckMenuItem(uav.getId()));
		uavMenuItems.getItems().clear();
		uavMenuItems.getItems().addAll(options);
		for (final CheckMenuItem item : options) {
			item.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
				if (newValue) {
					selectedUavs.add(item.getText());
				} else {
					selectedUavs.remove(item.getText());
				}
			});
		}
	}

	private void selectScreenToLoad() {
//		if (isFaultTargetSelected && isFaultSubTypeSelected && isFaultTargetSelected) {
//			switch (faultTypeSelected) {
//			case "GPS":
//				selectSoftwareFailureScreen();
//				break;
//			case "IMU":
//				selectCommunicationFailureScreen();
//				break;
//			case "ACC":
//				selectSecurityAttacksScreen();
//				break;
//			case "GYRO":
//				selectCommunicationFailureScreen();
//				break;
//			default:
//				break;
//			}

//		}
		selectSoftwareFailureScreen();
	}

	private void selectSoftwareFailureScreen() {
		try {
		screenController.SetTitle(faultSubTypeSelected);
		if (faultTargetSelected.equals("GPS")) {
			switch (faultSubTypeSelected) {
			case "Random Values":
				screenController.SetEnable_1(true,"Min Random Latitude");
				screenController.SetEnable_2(true,"Max Random Latitude");
				screenController.SetEnable_3(true,"Min Random Longitude");
				screenController.SetEnable_4(true,"Max Random Longitude");
				screenController.SetEnable_5(true,"Min Random Altitude");
				screenController.SetEnable_6(true,"Max Random Altitude");
				screenController.SetEnable_7(false);
				break;
			case "Fixed Values":
				screenController.SetEnable_1(true,"Latitude Value");
				screenController.SetEnable_2(true,"Longitude Value");
				screenController.SetEnable_3(true,"Altitude Value");
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Freeze Values":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Fixed Noise":
				screenController.SetEnable_1(true,"Latitude Noise");
				screenController.SetEnable_2(true,"Longitude Noise");
				screenController.SetEnable_3(true,"Altitude Noise");
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Random Noise":
				screenController.SetEnable_1(true,"Percent Latitude Noise (%)");
				screenController.SetEnable_2(true,"Percent Longitude Noise (%)");
				screenController.SetEnable_3(true,"Percent Altitude Noise (%)");
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Zigzag Values":
				screenController.SetEnable_1(true,"Latitude Noise");
				screenController.SetEnable_2(true,"Longitude Noise");
				screenController.SetEnable_3(true,"Altitude Noise");
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Min Latitude":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Max Latitude":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Min Longitude":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Max Longitude":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Min Altitude":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Max Altitude":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Hijack By a UAV":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Force UAV Landing":
				screenController.SetEnable_1(true,"Altitude reduction (m/sec)");
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Random Latitude":
				screenController.SetEnable_1(true,"Min Random Latitude");
				screenController.SetEnable_2(true,"Max Random Latitude");
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
			case "Random Longitude":
				screenController.SetEnable_1(true,"Min Random Longitude");
				screenController.SetEnable_2(true,"Max Random Longitude");
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
			case "Custom Fault":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(true, "Chose Python File");
			default:
				break;
			}
		}
		else if (faultTargetSelected.equals("IMU")){
			switch (faultSubTypeSelected) {
			case "Random Values":
				screenController.SetEnable_1(true,"Min Random Gyro");
				screenController.SetEnable_2(true,"Max Random Gyro");
				screenController.SetEnable_3(true,"Min Random Acc");
				screenController.SetEnable_4(true,"Max Random Acc");
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Fixed Values":
				screenController.SetEnable_1(true,"Gyro Value");
				screenController.SetEnable_2(true,"Acc Value");
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Freeze Values":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Random Noise":
				screenController.SetEnable_1(true,"Percent Gyro Noise (%)");
				screenController.SetEnable_2(true,"Percent Acc Noise (%)");
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Min Value":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Max Value":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Custom Fault":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(true, "Chose Python File");
			default:
				break;
			}
		}
		else if (faultTargetSelected.equals("ACC")){
			switch (faultSubTypeSelected) {
			case "Random Values":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(true,"Min Random Acc");
				screenController.SetEnable_4(true,"Max Random Acc");
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Fixed Values":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(true,"Acc Value");
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Freeze Values":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Random Noise":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(true,"Percent Acc Noise (%)");
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Min Value":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Max Value":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Custom Fault":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(true, "Chose Python File");
			default:
				break;
			}
		}
		else if (faultTargetSelected.equals("GYRO")){
			switch (faultSubTypeSelected) {
			case "Random Values":
				screenController.SetEnable_1(true,"Min Random Gyro");
				screenController.SetEnable_2(true,"Max Random Gyro");
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Fixed Values":
				screenController.SetEnable_1(true,"Gyro Value");
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Freeze Values":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Random Noise":
				screenController.SetEnable_1(true,"Percent Gyro Noise (%)");
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Min Value":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Max Value":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(false);
				break;
			case "Custom Fault":
				screenController.SetEnable_1(false);
				screenController.SetEnable_2(false);
				screenController.SetEnable_3(false);
				screenController.SetEnable_4(false);
				screenController.SetEnable_5(false);
				screenController.SetEnable_6(false);
				screenController.SetEnable_7(true, "Chose Python File");
			default:
				break;
			}
		}
		}
        catch(NullPointerException e)
        {
            //System.out.print("NullPointerException caught");
            screenController.Reset();
        }
	}

	private void selectCommunicationFailureScreen() {
		controllerFaultConf = loadFaultDefinitionPage("CustomCommunicationFault");
//		switch (faultSubTypeSelected) {
//		case "Custom Type":
//			controllerFaultConf = loadFaultDefinitionPage("CustomCommunicationFault");
//			break;
//		default:
//			break;
//		}
	}

	private void selectSecurityAttacksScreen() {
		switch (faultSubTypeSelected) {
		case "Random Longitude":
			controllerFaultConf = loadFaultDefinitionPage("RandomLongitude");
			break;
		case "Random Latitude":
			controllerFaultConf = loadFaultDefinitionPage("RandomLatitude");
			break;
		case "Random Position":
			controllerFaultConf = loadFaultDefinitionPage("RandomPosition");
			break;
		case "GPS Delay":
			controllerFaultConf = loadFaultDefinitionPage("GpsDelay");
			break;
		case "Force UAV Landing":
			controllerFaultConf = loadFaultDefinitionPage("ForceUAVLanding");
			break;
		case "Hijack with a second UAV":
			controllerFaultConf = loadFaultDefinitionPage("HijackSecondUAV");
			break;
		case "Hijack with attacker specified position":
			controllerFaultConf = loadFaultDefinitionPage("HijackwithAttacker");
			break;
		default:
			break;
		}
	}
	
	private void SaveData() {

		GenericFault newFault = new GenericFault(
				Integer.parseInt(screenController.GetValue_startTime()),
				Integer.parseInt(screenController.GetValue_endTime()), faultTypeSelected, faultSubTypeSelected,
				faultTargetSelected, Integer.parseInt(screenController.GetValue_1()),
				Integer.parseInt(screenController.GetValue_2()), Integer.parseInt(screenController.GetValue_3()),
				Integer.parseInt(screenController.GetValue_4()), Integer.parseInt(screenController.GetValue_5()),
				Integer.parseInt(screenController.GetValue_6()));
		ArrayList<String> uavs = new ArrayList<>();
		uavs.addAll(selectedUavs);
		newFault.setUavModelsToAffect(uavs);
		campaignData.addFault(newFault);
		lvFaultsResume.getItems().add(faultSubTypeSelected + " | No of Faults: " + tfNumberOfFaults.getText());
	}

	/*
	 * 
	 * Software Failures Save Methods
	 * 
	 */

	private void saveGpsModuleRandomValues() {
		GpsFailureRandomValueController controller = (GpsFailureRandomValueController) controllerFaultConf;
		GpsModuleRandomValues newFault = new GpsModuleRandomValues(
				Integer.parseInt(controller.getTlStartInjWindowTime()),
				Integer.parseInt(controller.getTlEndInjWindowTime()), Integer.parseInt(controller.getTlEndInjRunTime()),
				Integer.parseInt(tfNumberOfFaults.getText()), faultTypeSelected, faultSubTypeSelected,
				faultTargetSelected, Integer.parseInt(controller.getTlMinGpsLatitude()),
				Integer.parseInt(controller.getTlMaxGpsLatitude()), Integer.parseInt(controller.getTlMinGpsLongitude()),
				Integer.parseInt(controller.getTlMaxGpsLongitude()), Integer.parseInt(controller.getTlMinGpsAltitude()),
				Integer.parseInt(controller.getTlMaxGpsAltitude()));
		ArrayList<String> uavs = new ArrayList<>();
		uavs.addAll(selectedUavs);
		newFault.setUavModelsToAffect(uavs);
		campaignData.addFault(newFault);
		lvFaultsResume.getItems().add(faultSubTypeSelected + " | N� of Faults: " + tfNumberOfFaults.getText());
	}

	private void saveGpsModuleFixedValues() {
		GpsFailureFixedValueController controller = (GpsFailureFixedValueController) controllerFaultConf;
		GpsModuleFixedValues newFault = new GpsModuleFixedValues(Integer.parseInt(controller.getTfStartInjWindowTime()),
				Integer.parseInt(controller.getTfEndInjWindowTime()), Integer.parseInt(controller.getTfEndInjRunTime()),
				Integer.parseInt(tfNumberOfFaults.getText()), faultTypeSelected, faultSubTypeSelected,
				faultTargetSelected, Integer.parseInt(controller.getTfLatitude()),
				Integer.parseInt(controller.getTfLongitude()), Integer.parseInt(controller.getTfAltitude()));
		ArrayList<String> uavs = new ArrayList<>();
		uavs.addAll(selectedUavs);
		newFault.setUavModelsToAffect(uavs);
		campaignData.addFault(newFault);
		lvFaultsResume.getItems().add(faultSubTypeSelected + " | N� of Faults: " + tfNumberOfFaults.getText());
	}

	private void saveGpsModuleFreezeValues() {
		GpsFailureFreezeValuesController controller = (GpsFailureFreezeValuesController) controllerFaultConf;
		GpsModuleFreezeValues newFault = new GpsModuleFreezeValues(
				Integer.parseInt(controller.getTfStartInjWindowTime()),
				Integer.parseInt(controller.getTfEndInjWindowTime()), Integer.parseInt(controller.getTfEndInjRunTime()),
				Integer.parseInt(tfNumberOfFaults.getText()), faultTypeSelected, faultSubTypeSelected,
				faultTargetSelected);
		ArrayList<String> uavs = new ArrayList<>();
		uavs.addAll(selectedUavs);
		newFault.setUavModelsToAffect(uavs);
		campaignData.addFault(newFault);
		lvFaultsResume.getItems().add(faultSubTypeSelected + " | N� of Faults: " + tfNumberOfFaults.getText());
	}

	private void saveGpsModuleDelayedValues() {
		GpsFailureDelayedValuesController controller = (GpsFailureDelayedValuesController) controllerFaultConf;
		GpsModuleDelayedValues newFault = new GpsModuleDelayedValues(
				Integer.parseInt(controller.getTfStartInjWindowTime()),
				Integer.parseInt(controller.getTfEndInjWindowTime()), Integer.parseInt(controller.getTfEndInjRunTime()),
				Integer.parseInt(tfNumberOfFaults.getText()), faultTypeSelected, faultSubTypeSelected,
				faultTargetSelected, Integer.parseInt(controller.getTfDelay()));
		ArrayList<String> uavs = new ArrayList<>();
		uavs.addAll(selectedUavs);
		newFault.setUavModelsToAffect(uavs);
		campaignData.addFault(newFault);
		lvFaultsResume.getItems().add(faultSubTypeSelected + " | N� of Faults: " + tfNumberOfFaults.getText());
	}

	private void saveGpsModuleMinMaxValues() {
		GpsFailureMinMaxValueController controller = (GpsFailureMinMaxValueController) controllerFaultConf;
		GpsModuleMinMaxValues newFault = new GpsModuleMinMaxValues(
				Integer.parseInt(controller.getTlStartInjWindowTime()),
				Integer.parseInt(controller.getTlEndInjWindowTime()), Integer.parseInt(controller.getTlEndInjRunTime()),
				Integer.parseInt(tfNumberOfFaults.getText()), faultTypeSelected, faultSubTypeSelected,
				faultTargetSelected, Integer.parseInt(controller.getTlMinGpsLatitude()),
				Integer.parseInt(controller.getTlMaxGpsLatitude()), Integer.parseInt(controller.getTlMinGpsLongitude()),
				Integer.parseInt(controller.getTlMaxGpsLongitude()), Integer.parseInt(controller.getTlMinGpsAltitude()),
				Integer.parseInt(controller.getTlMaxGpsAltitude()));
		ArrayList<String> uavs = new ArrayList<>();
		uavs.addAll(selectedUavs);
		newFault.setUavModelsToAffect(uavs);
		campaignData.addFault(newFault);
		lvFaultsResume.getItems().add(faultSubTypeSelected + " | N� of Faults: " + tfNumberOfFaults.getText());
	}

	/*
	 * 
	 * Communication Faults Save Methods
	 * 
	 */

	private void saveCustomCommunicationFaultValues() {
		CustomCommunicationFaultController controller = (CustomCommunicationFaultController) controllerFaultConf;
		CustomCommunicationFault newFault = new CustomCommunicationFault(
				Integer.parseInt(controller.getTfStartInjWindowTime()),
				Integer.parseInt(controller.getTfEndInjWindowTime()), Integer.parseInt(controller.getTfEndInjRunTime()),
				Integer.parseInt(tfNumberOfFaults.getText()), faultTypeSelected, faultSubTypeSelected,
				faultTargetSelected, Integer.parseInt(controller.getTfBWText()),
				Integer.parseInt(controller.getTfJiterText()), Integer.parseInt(controller.getTfLossText()),
				Integer.parseInt(controller.getTfLatencyText()));
		ArrayList<String> uavs = new ArrayList<>();
		uavs.addAll(selectedUavs);
		newFault.setUavModelsToAffect(uavs);
		campaignData.addFault(newFault);
		lvFaultsResume.getItems().add(faultSubTypeSelected + " | N� of Faults: " + tfNumberOfFaults.getText());
	}

	/*
	 * 
	 * Security Attacks Save Methods
	 * 
	 */

	private void saveGpsDelay() {
		GpsDelayController controller = (GpsDelayController) controllerFaultConf;
		GpsModuleDelayedValues newFault = new GpsModuleDelayedValues(
				Integer.parseInt(controller.getTfStartInjWindowTime()),
				Integer.parseInt(controller.getTfEndInjWindowTime()), Integer.parseInt(controller.getTfEndInjRunTime()),
				Integer.parseInt(tfNumberOfFaults.getText()), faultTypeSelected, faultSubTypeSelected,
				faultTargetSelected, Integer.parseInt(controller.getTfDelay()));
		ArrayList<String> uavs = new ArrayList<>();
		uavs.addAll(selectedUavs);
		newFault.setUavModelsToAffect(uavs);
		campaignData.addFault(newFault);
		lvFaultsResume.getItems().add(faultSubTypeSelected + " | N� of Faults: " + tfNumberOfFaults.getText());
	}

	private void saveRandomPosition() {
		RandomPositionController controller = (RandomPositionController) controllerFaultConf;
		GpsModuleRandomValues newFault = new GpsModuleRandomValues(
				Integer.parseInt(controller.getTlStartInjWindowTime()),
				Integer.parseInt(controller.getTlEndInjWindowTime()), Integer.parseInt(controller.getTlEndInjRunTime()),
				Integer.parseInt(tfNumberOfFaults.getText()), faultTypeSelected, faultSubTypeSelected,
				faultTargetSelected, Integer.parseInt(controller.getTlMinGpsLatitude()),
				Integer.parseInt(controller.getTlMaxGpsLatitude()), Integer.parseInt(controller.getTlMinGpsLongitude()),
				Integer.parseInt(controller.getTlMaxGpsLongitude()), Integer.parseInt(controller.getTlMinGpsAltitude()),
				Integer.parseInt(controller.getTlMaxGpsAltitude()));
		ArrayList<String> uavs = new ArrayList<>();
		uavs.addAll(selectedUavs);
		newFault.setUavModelsToAffect(uavs);
		campaignData.addFault(newFault);
		lvFaultsResume.getItems().add(faultSubTypeSelected + " | N� of Faults: " + tfNumberOfFaults.getText());
	}

	private void saveRandomLatitude() {
		RandomLatitudeController controller = (RandomLatitudeController) controllerFaultConf;
		RandomLatitude newFault = new RandomLatitude(Integer.parseInt(controller.getTlStartInjWindowTime()),
				Integer.parseInt(controller.getTlEndInjWindowTime()), Integer.parseInt(controller.getTlEndInjRunTime()),
				Integer.parseInt(tfNumberOfFaults.getText()), faultTypeSelected, faultSubTypeSelected,
				faultTargetSelected, Integer.parseInt(controller.getTlMinGpsLatitude()),
				Integer.parseInt(controller.getTlMaxGpsLatitude()));
		newFault.setUavModelsToAffect(selectedUavs);
		campaignData.addFault(newFault);
		lvFaultsResume.getItems().add(faultSubTypeSelected + " | N� of Faults: " + tfNumberOfFaults.getText());
	}

	private void saveRandomLongitude() {
		RandomLongitudeController controller = (RandomLongitudeController) controllerFaultConf;
		RandomLongitude newFault = new RandomLongitude(Integer.parseInt(controller.getTlStartInjWindowTime()),
				Integer.parseInt(controller.getTlEndInjWindowTime()), Integer.parseInt(controller.getTlEndInjRunTime()),
				Integer.parseInt(tfNumberOfFaults.getText()), faultTypeSelected, faultSubTypeSelected,
				faultTargetSelected, Integer.parseInt(controller.getTlMinGpsLongitude()),
				Integer.parseInt(controller.getTlMaxGpsLongitude()));
		ArrayList<String> uavs = new ArrayList<>();
		uavs.addAll(selectedUavs);
		newFault.setUavModelsToAffect(uavs);
		campaignData.addFault(newFault);
		lvFaultsResume.getItems().add(faultSubTypeSelected + " | N� of Faults: " + tfNumberOfFaults.getText());
	}

	private void saveForceUAVLanding() {
		ForceUAVLandingController controller = (ForceUAVLandingController) controllerFaultConf;
		ForceUAVLanding newFault = new ForceUAVLanding(Integer.parseInt(controller.getTlStartInjWindowTime()),
				Integer.parseInt(controller.getTlEndInjWindowTime()), Integer.parseInt(controller.getTlEndInjRunTime()),
				Integer.parseInt(tfNumberOfFaults.getText()), faultTypeSelected, faultSubTypeSelected,
				faultTargetSelected, Integer.parseInt(controller.getTlMinGpsAltitude()),
				Integer.parseInt(controller.getTlMaxGpsAltitude()));
		ArrayList<String> uavs = new ArrayList<>();
		uavs.addAll(selectedUavs);
		newFault.setUavModelsToAffect(uavs);
		campaignData.addFault(newFault);
		lvFaultsResume.getItems().add(faultSubTypeSelected + " | N� of Faults: " + tfNumberOfFaults.getText());
	}

	private void saveHijackSecondUAV() {
		HijackSecondUAVXController controller = (HijackSecondUAVXController) controllerFaultConf;
		GpsModuleFixedValues newFault = new GpsModuleFixedValues(Integer.parseInt(controller.getTfStartInjWindowTime()),
				Integer.parseInt(controller.getTfEndInjWindowTime()), Integer.parseInt(controller.getTfEndInjRunTime()),
				Integer.parseInt(tfNumberOfFaults.getText()), faultTypeSelected, faultSubTypeSelected,
				faultTargetSelected, Integer.parseInt(controller.getTfLatitude()),
				Integer.parseInt(controller.getTfLongitude()), Integer.parseInt(controller.getTfAltitude()));
		ArrayList<String> uavs = new ArrayList<>();
		uavs.addAll(selectedUavs);
		newFault.setUavModelsToAffect(uavs);
		campaignData.addFault(newFault);
		lvFaultsResume.getItems().add(faultSubTypeSelected + " | N� of Faults: " + tfNumberOfFaults.getText());
	}

	private void saveHijackWithAttacker() {
		HijackwithAttackerController controller = (HijackwithAttackerController) controllerFaultConf;
		GpsModuleFixedValues newFault = new GpsModuleFixedValues(Integer.parseInt(controller.getTfStartInjWindowTime()),
				Integer.parseInt(controller.getTfEndInjWindowTime()), Integer.parseInt(controller.getTfEndInjRunTime()),
				Integer.parseInt(tfNumberOfFaults.getText()), faultTypeSelected, faultSubTypeSelected,
				faultTargetSelected, Integer.parseInt(controller.getTfLatitude()),
				Integer.parseInt(controller.getTfLongitude()), Integer.parseInt(controller.getTfAltitude()));
		ArrayList<String> uavs = new ArrayList<>();
		uavs.addAll(selectedUavs);
		newFault.setUavModelsToAffect(uavs);
		campaignData.addFault(newFault);
		lvFaultsResume.getItems().add(faultSubTypeSelected + " | N� of Faults: " + tfNumberOfFaults.getText());
	}

}
