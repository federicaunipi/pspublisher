package it.unifi.dinfo.gnocchi.meters;


import it.unifi.dinfo.gnocchi.CliHelperLola;
import it.unifi.dinfo.gnocchi.Measurement;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.LinkedList;
import java.util.List;

public class ProcessingTimeUsingBitrateMeter implements ProcessingTimeMeter {

	BitrateMeter bitrateMeter;
	SystemInfo si = new SystemInfo();
	HardwareAbstractionLayer hal = si.getHardware();

	public ProcessingTimeUsingBitrateMeter(BitrateMeter bitrateMeter){
		this.bitrateMeter=bitrateMeter;
	}

	@Override
	public double getProcessingTime(){
		double proccessing_capacity = CliHelperLola.getCli().capacity;	//In Mbit/s
		return bitrateMeter.getBitrate()/(proccessing_capacity*1000*1000);
	}

	public List<Measurement> getMeasurements(){
		List<Measurement> measurements = new LinkedList<>();
		double cpu = hal.getProcessor().getSystemCpuLoadBetweenTicks();
		double bitrate = bitrateMeter.getBitrate();
		double packetrate = bitrateMeter.getPacketRate();
		double proccessing_capacity = CliHelperLola.getCli().capacity;	//In Mbit/s
		double p_t =  bitrate /(proccessing_capacity*1000*1000);
		measurements.add(new Measurement("processing_time", p_t));
		measurements.add(new Measurement("incoming_rate_bytes", bitrate));
		measurements.add(new Measurement("incoming_rate_pkts", packetrate));
		measurements.add(new Measurement("cpu", cpu));
		return measurements;
	}

}
