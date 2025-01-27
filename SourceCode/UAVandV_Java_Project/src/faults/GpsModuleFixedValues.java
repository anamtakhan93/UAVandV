package faults;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GpsModuleFixedValues extends Fault{

	private int latitude;
	private int longitude;
	private int altitude;
	
	
	public GpsModuleFixedValues(int timeStartInjWindow, int timeEndInjWindow, int timeEndInjRun, int numberOfFaults,
			String faultType, String faultSubtype, String target, int latitude, int longitude, int altitude) {
		super(timeStartInjWindow, timeEndInjWindow, timeEndInjRun, numberOfFaults, faultType, faultSubtype, target);
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}
	
	public GpsModuleFixedValues(int timeStartInjWindow, int timeEndInjWindow, int timeEndInjRun, int numberOfFaults,
			String faultType, String faultSubtype, String target, int latitude,
			int longitude, int altitude, int timeInjection, List<String> uavs) {
		super(timeStartInjWindow, timeEndInjWindow, timeEndInjRun, numberOfFaults, faultType, faultSubtype, target, timeInjection, uavs);
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}


	public int getLatitude() {
		return latitude;
	}


	public void setLatitude(int latitude) {
		this.latitude = latitude;
	}


	public int getLongitude() {
		return longitude;
	}


	public void setLongitude(int longitude) {
		this.longitude = longitude;
	}


	public int getAltitude() {
		return altitude;
	}


	public void setAltitude(int altitude) {
		this.altitude = altitude;
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
		String [] fault = new String[11];
		fault[0] = getFaultType();
		fault[1] = getTarget();
		fault[2] = getFaultSubtype();
		fault[3] = getTimeStartInjWindow()+"";
		fault[4] = getTimeInjection()+"";
		fault[5] = getTimeEndInjWindow()+"";
		fault[6] = getTimeEndInjRun()+"";
		fault[7] = latitude+"";
		fault[8] = longitude+"";
		fault[9] = altitude+"";
		fault[10] = this.getUavModelsToAffectString();
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
		values.add(longitude);
		values.add(altitude);
		return values;
	}
	
	
	
}
