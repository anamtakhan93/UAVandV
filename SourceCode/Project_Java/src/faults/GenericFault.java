package faults;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenericFault extends Fault{
	
	private int v1;
	private int v2;
	private int v3;
	private int v4;
	private int v5;
	private int v6;
	
	
	public GenericFault(int timeStartInjWindow, int timeEndInjWindow,
			String faultType, String faultSubtype, String target, int v1, int v2, int v3,
			int v4, int v5, int v6) {
		super(timeStartInjWindow, timeEndInjWindow, 0, 1, faultType, faultSubtype, target);
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.v4 = v4;
		this.v5 = v5;
		this.v6 = v6;
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
		String [] fault = new String[14];
		fault[0] = getFaultType();
		fault[1] = getTarget();
		fault[2] = getFaultSubtype();
		fault[3] = getTimeStartInjWindow()+"";
		fault[4] = getTimeInjection()+"";
		fault[5] = getTimeEndInjWindow()+"";
		fault[6] = getTimeEndInjRun()+"";
		fault[7] = v1+"";
		fault[8] = v2+"";
		fault[9] = v3+"";
		fault[10] = v4+"";
		fault[11] = v5+"";
		fault[12] = v6+"";
		fault[13] = this.getUavModelsToAffectString();
		return fault;
	}

	@Override
	public int getTimeInjection() {
		return timeInjection;
	}
	
	@Override
	public List<Integer> getFaultValues() {
		List<Integer> values = new ArrayList<>();
		values.add(v1);
		values.add(v2);
		values.add(v3);
		values.add(v4);
		values.add(v5);
		values.add(v6);
		return values;
	}

}
