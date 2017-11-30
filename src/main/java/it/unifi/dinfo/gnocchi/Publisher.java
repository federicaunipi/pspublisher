package it.unifi.dinfo.gnocchi;

import it.unifi.dinfo.gnocchi.meters.BitrateMeter;
import it.unifi.dinfo.gnocchi.meters.ProcessingTimeMeter;
import it.unifi.dinfo.gnocchi.meters.ProcessingTimeUsingBitrateMeter;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.hardware.NetworkIF;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Publisher {

	public static void main(String args[]) {
		//Logger configuration

		PropertyConfigurator.configure("log4j.properties");
		Logger logger = LoggerFactory.getLogger(Publisher.class);

		//Parse input arguments
		CliHelper cli = CliHelper.parse(args);

		//Scheduler with one thread
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		//Get interface by name
		NetworkIF iface = NetIFHelper.getInterfaceByName(cli.iface);

		//Instantiate the Bitrate Poller setting the interval and queried interface
		BitrateMeter bitrateMeter = new BitrateMeter(1000L, iface);
		ProcessingTimeMeter psMeter = new ProcessingTimeUsingBitrateMeter(bitrateMeter);

		GnocchiAPI gnocchi = new GnocchiAPI(cli.ip, cli.username, cli.password, cli.projectId, cli.domain);

		//Add a task to the scheduler: poll and print the bitrate
		scheduler.scheduleAtFixedRate(() -> gnocchi.pushMeasurement(new Measurement(psMeter.getProcessingTime())), 0L, cli.interval, TimeUnit.MILLISECONDS);
		//scheduler.scheduleAtFixedRate(() -> System.out.println(new Measurement(psMeter.getProcessingTime())), 0L, cli.interval, TimeUnit.MILLISECONDS);

	}

}
