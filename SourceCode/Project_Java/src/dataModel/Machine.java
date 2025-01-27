package dataModel;

public class Machine {
	
	private String InjectionFilesDir;
	private String MissionFilesDir;
	private String AutopilotPath;
	private String DroneKitScriptPath;

	private String IP;
	private String User;
	private String Password;
	
	private int NumOfDrones;
	private int UAVstartIndex;
	private int UAVendIndex;
	

	public Machine(String iP, String user, String password, String injectionFilesDir, String missionFilesDir, String autopilotPath, 
			String droneKitScriptPath, int numOfDrones) {
		super();
		IP = iP;
		User = user;
		Password = password;
		InjectionFilesDir = injectionFilesDir;
		MissionFilesDir = missionFilesDir;
		AutopilotPath = autopilotPath;
		DroneKitScriptPath = droneKitScriptPath;
		NumOfDrones = numOfDrones;
		UAVstartIndex = 0;
		UAVendIndex = numOfDrones;
		
	}
	
	public Machine() {
		//This uses the default values
		InjectionFilesDir = "/home/bubbles/Injections/";
		MissionFilesDir = "/home/bubbles/Missions/";
		AutopilotPath = "/home/bubbles/PX4/PX4-Autopilot";
		DroneKitScriptPath = "/home/bubbles/DronekitScript/dronekit_script_custom.py";

		IP = "10.254.0.118";
		User = "bubbles";
		Password = "Bubbles%21";
		
		NumOfDrones = 9;
	}
	
	
	
	public String getInjectionFilesDir() {
		return InjectionFilesDir;
	}


	public void setInjectionFilesDir(String injectionFilesDir) {
		InjectionFilesDir = injectionFilesDir;
	}


	public String getMissionFilesDir() {
		return MissionFilesDir;
	}


	public void setMissionFilesDir(String missionFilesDir) {
		MissionFilesDir = missionFilesDir;
	}


	public String getAutopilotPath() {
		return AutopilotPath;
	}


	public void setAutopilotPath(String autopilotPath) {
		AutopilotPath = autopilotPath;
	}


	public String getDroneKitScriptPath() {
		return DroneKitScriptPath;
	}


	public void setDroneKitScriptPath(String droneKitScriptPath) {
		DroneKitScriptPath = droneKitScriptPath;
	}


	public String getIP() {
		return IP;
	}


	public void setIP(String iP) {
		IP = iP;
	}


	public String getUser() {
		return User;
	}


	public void setUser(String user) {
		User = user;
	}


	public String getPassword() {
		return Password;
	}


	public void setPassword(String password) {
		Password = password;
	}

	public int getNumOfDrones() {
		return NumOfDrones;
	}

	public void setNumOfDrones(int numOfDrones) {
		NumOfDrones = numOfDrones;
	}

	public int getUAVstartIndex() {
		return UAVstartIndex;
	}

	public void setUAVstartIndex(int uAVstartIndex) {
		UAVstartIndex = uAVstartIndex;
	}

	public int getUAVendIndex() {
		return UAVendIndex;
	}

	public void setUAVendIndex(int uAVendIndex) {
		UAVendIndex = uAVendIndex;
	}


}
