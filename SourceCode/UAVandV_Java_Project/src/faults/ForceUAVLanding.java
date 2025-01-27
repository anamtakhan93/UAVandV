package faults;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ForceUAVLanding extends Fault{
	
	private int altitudeMin;
	private int altitudeMax;
	
	private int altitude;
	

	public ForceUAVLanding(int timeStartInjWindow, int timeEndInjWindow, int timeEndInjRun, int numberOfFaults,
			String faultType, String faultSubtype, String target, int altitudeMin, int altitudeMax) {
		super(timeStartInjWindow, timeEndInjWindow, timeEndInjRun, numberOfFaults, faultType, faultSubtype, target);
		this.altitudeMin = altitudeMin;
		this.altitudeMax = altitudeMax;
	}
	
	public ForceUAVLanding(int timeStartInjWindow, int timeEndInjWindow, int timeEndInjRun, int numberOfFaults,
			String faultType, String faultSubtype, String target, int altitude, int timeInjection, List<String> uavs) {
		super(timeStartInjWindow, timeEndInjWindow, timeEndInjRun, numberOfFaults, faultType, faultSubtype, target, timeInjection, uavs);
		this.altitude = altitude;
	}


	public int getAltitudeMin() {
		return altitudeMin;
	}

	public int getAltitudeMax() {
		return altitudeMax;
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
		return timeEndInjRun;
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
	public String[] getFaultRecord(Random rnd) {
		String [] fault = new String[9];
		fault[0] = getFaultType();
		fault[1] = getTarget();
		fault[2] = getFaultSubtype();
		fault[3] = getTimeStartInjWindow()+"";
		fault[4] = getTimeInjection()+"";
		fault[5] = getTimeEndInjWindow()+"";
		fault[6] = getTimeEndInjRun()+"";
		altitude = this.getRandomNumber(rnd, altitudeMin, altitudeMax);
		fault[7] = altitude+"";
		fault[8] = this.getUavModelsToAffectString();
		return fault;
	}

	@Override
	public int getTimeInjection() {
		return timeInjection;
	}
	
	@Override
	public List<Integer> getFaultValues() {
		List<Integer> values = new ArrayList<>();
		values.add(altitude);
		return values;
	}
	

}
