package dataModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import faults.Fault;

public class CampaignData {
	
	//TYPE of Flight Controller
	public static int PX4_CONTROLLER = 0;
	public static int ARDU_PILOT_CONTROLLER = 1;
	public static int ROTORS_CONTORLLER = 2;
	
	

	private int flightController;						// Flight Controller Selected
	private HashMap<String, UAVModel> UAVModel;			// UAV Model's Selected
	private String missionPlanPath;						// Mission Plan File Path
	private String gezeboWorldPath;						// Gezebo World File Path
	private List<Fault> faults;							// List with Faults
	

	public int getFlightController() {
		return flightController;
	}

	public void setFlightController(int flightController) {
		this.flightController = flightController;
	}

	public List<UAVModel> getUAVModels() {
		Collection<UAVModel> uavs = UAVModel.values();
		return new ArrayList<UAVModel>(uavs); 
	}
	
	public HashMap<String, UAVModel> GetUAVData(){
		return UAVModel;
	}

	public void addUAVModel(UAVModel uAVModel) throws Exception {
		if(UAVModel == null)
			UAVModel = new HashMap<String, UAVModel>();
		if(UAVModel.containsKey(uAVModel.getId()))
			throw new Exception("Model already added!");
		UAVModel.put(uAVModel.getId(), uAVModel);
	}

	public String getMissionPlanPath() {
		return missionPlanPath;
	}

	public void setMissionPlanPath(String missionPlanPath) {
		this.missionPlanPath = missionPlanPath;
	}

	public String getGezeboWorldPath() {
		return gezeboWorldPath;
	}

	public void setGezeboWorldPath(String gezeboWorldPath) {
		this.gezeboWorldPath = gezeboWorldPath;
	}

	public List<Fault> getFaults() {
		return faults;
	}

	public void setFaults(List<Fault> faults) {
		this.faults = faults;
	}

	public void addFault(Fault fault) {
		if(this.faults == null)
			this.faults = new ArrayList<>();
		this.faults.add(fault);
	}
	
	public boolean isFaultListEmpety() {
		return faults.isEmpty();
	}

	
	public String[] getNumberOfUAVModels() {
		String [] record = new String[2];
		record[0] = "Number of UAV Models";
		record[1] = UAVModel.size()+"";
		return record;
	}
	
	public String getResume() {
		String resume = "Gezebo World File: " + gezeboWorldPath + "\n\n";
		resume = resume + "UAV Models Selected:\n";
		for(UAVModel uavModel : UAVModel.values()) {
			resume = resume + "- " + uavModel.getModel() + " | " + uavModel.getIp() + "\n";
		}
		resume = resume + "\nFaults:\n";
		for (Fault fault : faults) {
			resume = "- " + resume + fault.getResume();
		}		
		return resume;
	}

	
	public String[] getFlightControllerRecord() {
		String[] record = new String[2];
		record[0] = "Flight Controller";
		record[1] = flightController + "";
		return record;
	}
	
	public String[] getWorldrRecord() {
		String[] record = new String[2];
		record[0] = "Gezebo World File Path";
		record[1] = gezeboWorldPath;
		return record;
	}
}
