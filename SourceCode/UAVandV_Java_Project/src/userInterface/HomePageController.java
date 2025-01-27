package userInterface;

import java.net.URL;
import java.util.ResourceBundle;

import executionFaults.LocalExecutionTool;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

public class HomePageController implements Initializable{

	

    @FXML
    private AnchorPane apHomePage;
    @FXML
    private Text tlTitle, tlLastExp;
    @FXML
    private TableView<?> tableHomePage;
    @FXML
    private TableColumn<?, ?> columnName, columnPath, columnDate;
    @FXML
    private Button btnExecute, btnAddCampaign;
    @FXML
    private ImageView ivIconLeft, ivIconCenter, ivIconRight;
    
    
    private SideBarController sideBarController;
    
    private LocalExecutionTool injector;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		// Create Injector Tool
		this.injector = new LocalExecutionTool("");
		
	}
	
	public void setConfigurations(SideBarController sideBarController) {
		this.sideBarController = sideBarController;
	}
	

    @FXML
    void onClickExecuteBtn(MouseEvent event) {
		System.out.println("-------------------------------- Clicked Exec button");

    }

    @FXML
    void onClickSearchBtn(MouseEvent event) {

    }
    

    @FXML
    void onClickTestBtn(MouseEvent event) {
		System.out.println("-------------------------------- Clicked Test button");
    	injector.RunLocalTest("t1.csv");

    }
	
	

}
