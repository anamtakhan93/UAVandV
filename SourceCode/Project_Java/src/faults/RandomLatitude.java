package faults;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomLatitude extends Fault{
	
	private int latitudeMin;
	private int latitudeMax;
	
	private int latitude;

	
	public RandomLatitude(int timeStartInjWindow, int timeEndInjWindow, int timeEndInjRun, int numberOfFaults,
			String faultType, String faultSubtype, String target, int latitudeMin, int latitudeMax) {
		super(timeStartInjWindow, timeEndInjWindow, timeEndInjRun, numberOfFaults, faultType, faultSubtype, target);
		this.latitudeMin = latitudeMin;
		this.latitudeMax = latitudeMax;
	}
	
	public RandomLatitude(int timeStartInjWindow, int timeEndInjWindow, int timeEndInjRun, int numberOfFaults,
			String faultType, String faultSubtype, String target, int latitude, int timeInjection, List<String> uavs) {
		super(timeStartInjWindow, timeEndInjWindow, timeEndInjRun, numberOfFaults, faultType, faultSubtype, target, timeInjection, uavs);
		this.latitude = latitude;
	}

	public int getLatitudeMin() {
		return latitudeMin;
	}

	public int getLatitudeMax() {
		return latitudeMax;
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
		latitude = this.getRandomNumber(rnd, latitudeMin, latitudeMax);
		fault[7] = latitude+"";
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
		values.add(latitude);
		return values;
	}
	

}
