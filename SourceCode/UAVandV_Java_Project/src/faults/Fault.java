package faults;

import java.util.List;
import java.util.Random;

public abstract class Fault {

	protected int timeStartInjWindow;
	protected int timeEndInjWindow;
	protected int timeEndInjRun;
	protected int numberOfFaults;
	protected String faultType;
	protected String faultSubtype;
	protected String target;
	protected int timeInjection;
	protected List<String> uavModelsToAffect;	
	
	

	public Fault(int timeStartInjWindow, int timeEndInjWindow, int timeEndInjRun, int numberOfFaults, String faultType,
			String faultSubtype, String target) {
		super();
		this.timeStartInjWindow = timeStartInjWindow;
		this.timeEndInjWindow = timeEndInjWindow;
		this.timeEndInjRun = timeEndInjRun;
		this.numberOfFaults = numberOfFaults;
		this.faultType = faultType;
		this.faultSubtype = faultSubtype;
		this.target = target;
	}
	
	public Fault(int timeStartInjWindow, int timeEndInjWindow, int timeEndInjRun, int numberOfFaults, String faultType,
			String faultSubtype, String target, int timeInjection, List<String> uavs) {
		super();
		this.timeStartInjWindow = timeStartInjWindow;
		this.timeEndInjWindow = timeEndInjWindow;
		this.timeEndInjRun = timeEndInjRun;
		this.numberOfFaults = numberOfFaults;
		this.faultType = faultType;
		this.faultSubtype = faultSubtype;
		this.target = target;
		this.timeInjection = timeInjection;
		this.uavModelsToAffect = uavs;
	}


	public abstract int getTimeStartInjWindow();


	public void setTimeStartInjWindow(int timeStartInjWindow) {
		this.timeStartInjWindow = timeStartInjWindow;
	}


	public abstract int getTimeEndInjWindow();


	public void setTimeEndInjWindow(int timeEndInjWindow) {
		this.timeEndInjWindow = timeEndInjWindow;
	}


	public abstract int getTimeEndInjRun();


	public void setTimeEndInjRun(int timeEndInjRun) {
		this.timeEndInjRun = timeEndInjRun;
	}


	public abstract int getNumberOfFaults();


	public void setNumberOfFaults(int numberOfFaults) {
		this.numberOfFaults = numberOfFaults;
	}


	public abstract String getFaultType();


	public void setFaultType(String faultType) {
		this.faultType = faultType;
	}


	public abstract String getFaultSubtype();


	public void setFaultSubtype(String faultSubtype) {
		this.faultSubtype = faultSubtype;
	}


	public abstract String  getTarget();


	public void setTarget(String target) {
		this.target = target;
	}
	
	public abstract String [] getFaultRecord(Random rnd);	
	
	public abstract int getTimeInjection();
	
	public void setTimeInjection(int timeValue) {
		this.timeInjection = timeValue;
	}
	
	public String getResume() {
		String resume = "Fault Type: " + faultType  + " | "
				+ "Fault SubType: " + faultSubtype + " | "
				+ "Fault Target: " + target + " | "
				+ "Number of Faults " + numberOfFaults + "\n";
		return resume;
	}
	
	
	public int getRandomNumber(Random rnd, int min, int max) {
		return rnd.nextInt(max - min) + min;
	}
	
	public List<String> getUavModelsToAffect(){
		return uavModelsToAffect;
	}
	
	public void setUavModelsToAffect(List<String> uavModels) {
		this.uavModelsToAffect = uavModels;
	}
	
	public String getUavModelsToAffectString() {
		String models = "[";
		for(int i=0; i<uavModelsToAffect.size(); i++) {
			models = models + uavModelsToAffect.get(i);
			if(i != uavModelsToAffect.size()-1)
				models = models + ";";
		}

		models = models + "]";
		return models;
	}
	
	public abstract List<Integer> getFaultValues();
}
