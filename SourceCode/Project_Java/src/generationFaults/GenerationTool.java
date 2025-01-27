package generationFaults;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import CSVModule.CSVDataWriter;
import dataModel.CampaignData;
import dataModel.UAVModel;
import faults.Fault;
import javafx.scene.layout.Pane;
import userInterface.CommunInterfaceFunctions;
import utils.OsValidator;

public class GenerationTool {

	
	private CampaignData campaignData;
	private Random rnd = null;
	private Pane p;
	private String fileName;
	
	
	public GenerationTool(CampaignData campaignData, Pane p, String fileName) {
		super();
		this.p = p;
		this.campaignData = campaignData;
		rnd = new Random();
		this.fileName = fileName;
	}
	
	
	
	public void saveFaults() throws Exception {
		//FileChooser chooser = 
		String path = null;
		try {
			path = CommunInterfaceFunctions.getDirectoryPath(p);
		} catch (Exception e1) {
			return;
		}
		CSVDataWriter writer = null;
		if(OsValidator.isWindows()) {
			try { writer = new CSVDataWriter(path + "\\" + fileName + ".csv"); } 
			catch (IOException e) {
				throw new Exception("An error occours during the file creation. Please, try again.");
			}
		}
		else {
			try { writer = new CSVDataWriter(path + "/" + fileName + ".csv"); } 
			catch (IOException e) {
				throw new Exception("An error occours during the file creation. Please, try again.");
			}
		}
		
		//Save Configuration
		writer.addNewLine(campaignData.getFlightControllerRecord());
		writer.addNewLine(campaignData.getWorldrRecord());
		//Save UAV Models
		List<UAVModel> models = campaignData.getUAVModels();
		writer.addNewLine(campaignData.getNumberOfUAVModels());
		for (UAVModel uavModel : models) {
			writer.addNewLine(uavModel.getUAVModelRecord());
		}
		//Save Type of Faults
		//System.out.println("Number of Faults : " + campaignData.getFaults().size());
		for (Fault fault : campaignData.getFaults()) {
			//Save i Number of faults
			for(int i=0; i < fault.getNumberOfFaults(); i++) {
				//System.out.println("Saving Faults : " + i);
				//Calculate the injection time randomly 
				fault.setTimeInjection(getRandomNumber(fault.getTimeStartInjWindow(), fault.getTimeEndInjWindow()));
				String [] record = fault.getFaultRecord(this.rnd);
				writer.addNewLine(record);
			}
		}
		
		writer.closeWriter();
	}
	
	/**
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public int getRandomNumber(int min, int max) {
		return rnd.nextInt(max - min) + min;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getRandomNumber() {
		return rnd.nextInt();
	}
	
}
