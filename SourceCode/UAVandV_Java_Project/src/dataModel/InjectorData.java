package dataModel;

import java.util.List;

public class InjectorData {

	
	private String droneIP;
	private String missionPath;
	private int timeStartInjWindow;
	private int timeEndInjWindow;
	private int timeEndInjRun;
	private String faultType;
	private String faultSubtype;
	private String target;
	private int timesInjection;
	private List<Integer> values;
	private int machineID;
	
	
	public InjectorData(String droneIP, String missionPath, int timeStartInjWindow, int timeEndInjWindow,
			int timeEndInjRun, String faultType, String faultSubtype, String target, int timesInjection, List<Integer> values, int machineID) {
		super();
		this.droneIP = droneIP;
		this.missionPath = missionPath;
		this.timeStartInjWindow = timeStartInjWindow;
		this.timeEndInjWindow = timeEndInjWindow;
		this.timeEndInjRun = timeEndInjRun;
		this.faultType = faultType;
		this.faultSubtype = faultSubtype;
		this.target = target;
		this.timesInjection = timesInjection;
		this.machineID = machineID;
		this.values = values;
	}
	public String getDroneIP() {
		return droneIP;
	}
	public void setDroneIP(String droneIP) {
		this.droneIP = droneIP;
	}
	public String getMissionPath() {
		return missionPath;
	}
	public void setMissionPath(String missionPath) {
		this.missionPath = missionPath;
	}
	public int getTimeStartInjWindow() {
		return timeStartInjWindow;
	}
	public void setTimeStartInjWindow(int timeStartInjWindow) {
		this.timeStartInjWindow = timeStartInjWindow;
	}
	public int getTimeEndInjWindow() {
		return timeEndInjWindow;
	}
	public void setTimeEndInjWindow(int timeEndInjWindow) {
		this.timeEndInjWindow = timeEndInjWindow;
	}
	public int getTimeEndInjRun() {
		return timeEndInjRun;
	}
	public void setTimeEndInjRun(int timeEndInjRun) {
		this.timeEndInjRun = timeEndInjRun;
	}
	public String getFaultType() {
		return faultType;
	}
	public void setFaultType(String faultType) {
		this.faultType = faultType;
	}
	public String getFaultSubtype() {
		return faultSubtype;
	}
	public void setFaultSubtype(String faultSubtype) {
		this.faultSubtype = faultSubtype;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public int getTimesInjection() {
		return timesInjection;
	}
	public void setTimesInjection(int timesInjection) {
		this.timesInjection = timesInjection;
	}
	public List<Integer> getValues() {
		return values;
	}
	public void setValues(List<Integer> values) {
		this.values = values;
	}
	
	
}
