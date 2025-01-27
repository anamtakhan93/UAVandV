package userInterface;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;

import executionFaults.ExecutionTool;
import executionFaults.LocalExecutionTool;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import utils.Timer;

public class CampaignExecutionController implements Initializable {

	
	
	@FXML
    private AnchorPane apExecutionPage;

    @FXML
    private Text tlTitle, tlExperimentName;

    @FXML
    private Button btnPlay, btnPause,  btnAbort;

    @FXML
    private TextField tfTimer;

    @FXML
    private TextArea tfLogWindow;
	@FXML
	private ComboBox<String> cbExecuteOn;
    
    
    private SideBarController sideBarController;
    //private Timer timer;
    private Semaphore abortSemaphore;
    private boolean exitFlag;
    //private ExecutionTool injector;
    private LocalExecutionTool injector;
    private String filePath = "";
	// ComboBox Option
	private String[] ExeOptions = new String[2];

    
    
    @Override
	public void initialize(URL url, ResourceBundle rb) {
    	//this.timer = new Timer(this.tfTimer);
		//new Thread(this.timer).start();
		abortSemaphore = new Semaphore(1);
		this.exitFlag = false;
		ExeOptions[0] = "Local";
		ExeOptions[1] = "Server";
		cbExecuteOn.setItems(FXCollections.observableArrayList(ExeOptions));
		

		// Event Listener of Fault Target Combo Box
		cbExecuteOn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				
				switch (cbExecuteOn.getValue()) {
				case "Local":
					//Run Mission Locally
					break;
				case "Server":
					//Run Mission On the server
					break;
				default:
					break;
				}
				
			}
		});
		
	}
    
    public void defineSettings(String filePath) {
		// Create Injector Tool
		//this.injector = new ExecutionTool(filePath);
		this.injector = new LocalExecutionTool("");
		this.filePath = filePath;
    }
    
    
    @FXML
    void onClickBtnAbort(MouseEvent event) {
    	//TODO
    	//not implemented yet
    }

    @FXML
    void onClickBtnPause(MouseEvent event) {
    	//injector.setPausedFlag(true);
    	//timer.setPause(true);
    }

    @FXML
    void onClickBtnPlay(MouseEvent event) {
    	//injector.setPausedFlag(false);
    	//timer.setPause(false);
    	//injector.injectionLoop();
    	injector.RunLocalTest(filePath);
    }
    
    
    
    /**
	 * Disable Buttons available in the interface
	 */
	public void disableButtons() {
		//btnAbort.setDisable(true);
		//btnPause.setDisable(true);
		btnPlay.setDisable(true);
	}
	
	
	
	

}
