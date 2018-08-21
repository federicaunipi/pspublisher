package it.unifi.dinfo.gnocchi;

import it.unifi.dinfo.gnocchi.meters.BitrateMeter;
import it.unifi.dinfo.gnocchi.meters.ProcessingTimeMeter;
import it.unifi.dinfo.gnocchi.meters.ProcessingTimeUsingBitrateMeter;
import it.unifi.dinfo.gnocchi.meters.SlackBitrateMeter;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.hardware.NetworkIF;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Publisher {
	private Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String args[]) throws InterruptedException, IOException {
		//Logger configuration

		PropertyConfigurator.configure("log4j.properties");
		Logger logger = LoggerFactory.getLogger(Publisher.class);

		//Parse input arguments
		CliHelperLola cli = CliHelperLola.parse(args);

		//Scheduler with one thread
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		//Get interface by name
		NetworkIF iface = NetIFHelper.getInterfaceByName(cli.iface);

		TrafficController trafficController = new TrafficController();


		GnocchiAPI gnocchi = new GnocchiApiSimple(cli.ip, cli.username, cli.password);
//		GnocchiAPI gnocchi = new FakeGnocchiApi();

		//Instantiate the Bitrate Poller setting the interval and queried interface
		BitrateMeter bitrateMeter = new SlackBitrateMeter(iface);
		ProcessingTimeMeter psMeter = new ProcessingTimeUsingBitrateMeter(bitrateMeter);

		//Add a task to the scheduler: poll and print the bitrate



		try {
			scheduler.scheduleWithFixedDelay(() -> gnocchi
					.pushMeasurement(psMeter.getMeasurements()), 0L, cli.interval, TimeUnit.MILLISECONDS);
		} catch (FirstIterationException e) {
			logger.info("First iteration, waiting " + cli.interval + "ms for gathering data");
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				trafficController.stopDelay();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));

		//scheduler.scheduleAtFixedRate(() -> System.out.println(new Measurement(psMeter.getProcessingTime())), 0L, cli.interval, TimeUnit.MILLISECONDS);

	}

}
