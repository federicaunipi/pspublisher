package it.unifi.dinfo.gnocchi;

import it.unifi.dinfo.gnocchi.meters.*;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.hardware.NetworkIF;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Publisher {

	public static void main(String args[]) throws InterruptedException {
		//Logger configuration

		PropertyConfigurator.configure("log4j.properties");
		Logger logger = LoggerFactory.getLogger(Publisher.class);

		//Parse input arguments
		CliHelper cli = CliHelper.parse(args);

		//Scheduler with one thread
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		//Get interface by name
		NetworkIF iface = NetIFHelper.getInterfaceByName(cli.iface);



		GnocchiAPI gnocchi = new GnocchiAPI(cli.ip, cli.username, cli.password, cli.projectId, cli.domain);

		while(!gnocchi.checkIfInstanceExists()){
			logger.error(String.format("Instance %s not found on dc %s. Infinite retry loop. Waiting for 10 seconds.", CliHelper.getCli().instance, CliHelper.getCli().ip));
			Thread.sleep(10_000);
		}

		//Instantiate the Bitrate Poller setting the interval and queried interface
		BitrateMeter bitrateMeter = new SlackBitrateMeter(iface);
		ProcessingTimeMeter psMeter = new ProcessingTimeUsingBitrateMeter(bitrateMeter);

		//Add a task to the scheduler: poll and print the bitrate
		try {
			scheduler.scheduleWithFixedDelay(() -> gnocchi.pushMeasurement(new Measurement(psMeter
					.getProcessingTime())), 0L, cli.interval, TimeUnit.MILLISECONDS);
		}
		catch (FirstIterationException e){
			logger.info("First iteration, waiting "+cli.interval+ "ms for gathering data");
		}

		//scheduler.scheduleAtFixedRate(() -> System.out.println(new Measurement(psMeter.getProcessingTime())), 0L, cli.interval, TimeUnit.MILLISECONDS);

	}

}
