package it.unifi.dinfo.gnocchi.meters;

import it.unifi.dinfo.gnocchi.CliHelper;

public class ProcessingTimeUsingBitrateMeter implements ProcessingTimeMeter {

	BitrateMeter bitrateMeter;
	public ProcessingTimeUsingBitrateMeter(BitrateMeter bitrateMeter){
		this.bitrateMeter=bitrateMeter;
	}

	@Override
	public double getProcessingTime(){
		double proccessing_capacity = CliHelper.getCli().proccessing_capacity;	//In MB/s
		return bitrateMeter.getBitrate()/(proccessing_capacity*1000*1000);
	}
}
