package faults;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GpsModuleDelayedValues extends Fault {

	private int delayValue;

	public GpsModuleDelayedValues(int timeStartInjWindow, int timeEndInjWindow, int timeEndInjRun, int numberOfFaults,
			String faultType, String faultSubtype, String target, int delayValue) {
		super(timeStartInjWindow, timeEndInjWindow, timeEndInjRun, numberOfFaults, faultType, faultSubtype, target);
		this.delayValue = delayValue;
	}
	
	public GpsModuleDelayedValues(int timeStartInjWindow, int timeEndInjWindow, int timeEndInjRun, int numberOfFaults,
			String faultType, String faultSubtype, String target, int timeInjection, List<String> uavs) {
		super(timeStartInjWindow, timeEndInjWindow, timeEndInjRun, numberOfFaults, faultType, faultSubtype, target, timeInjection, uavs);
	}

	public int getDelayValue() {
		return delayValue;
	}

	public void setDelayValue(int delayValue) {
		this.delayValue = delayValue;
	}
	
	public String [] getFaultRecord(Random rnd) {
		String [] fault = new String[10];
		fault[0] = getFaultType();
		fault[1] = getTarget();
		fault[2] = getFaultSubtype();
		fault[3] = getTimeStartInjWindow()+"";
		fault[4] = getTimeInjection()+"";
		fault[5] = getTimeEndInjWindow()+"";
		fault[6] = getTimeEndInjRun()+"";
		fault[7] = getDelayValue()+"";
		fault[9] = this.getUavModelsToAffectString();
		return fault;
	}

	@Override
	public int getTimeStartInjWindow() {
		return timeStartInjWindow;
	}

	@Override
	public int getTimeEndInjWindow() {
		return timeEndInjWindow;
	}

	@Override
	public int getTimeEndInjRun() {
		return timeEndInjWindow;
	}

	@Override
	public int getNumberOfFaults() {
		return numberOfFaults;
	}

	@Override
	public String getFaultType() {
		return faultType;
	}

	@Override
	public String getFaultSubtype() {
		return faultSubtype;
	}

	@Override
	public String getTarget() {
		return target;
	}

	@Override
	public int getTimeInjection() {
		return timeInjection;
	}
	
	@Override
	public List<Integer> getFaultValues() {
		List<Integer> values = new ArrayList<>();
		values.add(delayValue);
		return values;
	}
	
	
}
