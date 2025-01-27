package dataModel;

import userInterface.IPValidationChangeListener;

public class UAVModel {
	
	private String ip;
	private String model;
	private String missionPath;
	private String id;
	private int order;
	
	//Used to read UAVModels from a file
	public UAVModel(String [] attributes) throws Exception { //ORDER: id, ip, model, missionpath, order
		super();
		this.id = attributes[0];
		if(!IPValidationChangeListener.validIP(attributes[1]))
			throw new Exception("Invalid IP received from CSV value.");
		this.ip = attributes[1];
		if(attributes[2] == null || attributes[2].isEmpty())
			throw new Exception("Invalid format of CSV file. Please, try again with a valid file. MODEL");
		this.model = attributes[2];
		if( attributes.length != 4 || attributes[3] == null)
			throw new Exception("Invalid format of CSV file. Please, try again with a valid file. MISSION PATH");
		this.missionPath = attributes[3];
	}
	
	
	public UAVModel(String id, String ip, String model, String missionPath, int order) {
		super();
		this.id = id;
		this.ip = ip;
		this.model = model;
		this.missionPath = missionPath;
		this.order = order;
	}

	public UAVModel(String id, String ip, String model, String missionPath) {
		super();
		this.id = id;
		this.ip = ip;
		this.model = model;
		this.missionPath = missionPath;
	}


	public String getIp() {
		return ip;
	}
	
	public String getId() {
		return id;
	}


	public String getModel() {
		return model;
	}

	public String getMissionPath() {
		return missionPath;
	}


	public void setMissionPath(String missionPath) {
		this.missionPath = missionPath;
	}


	public String[] getUAVModelRecord() {
		String[] record = new String[4];
		record[0] = id;
		record[1] = model;
		record[2] = ip;
		record[3] = missionPath;
		return record;
	}


	public int getOrder() {
		return order;
	}


	public void setOrder(int order) {
		this.order = order;
	}
	
	
	
}
