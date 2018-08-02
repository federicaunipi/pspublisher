package it.unifi.dinfo.gnocchi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TrafficController {
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	int delay = 0;

	private String getCommand(int delay) {
		String command = "sudo tc qdisc add dev %s root netem delay %dms";
		this.delay = delay;
		return String.format(command, CliHelperLola.getCli().iface, delay);
	}

	private String delCommand() {
		String command = "sudo tc qdisc del dev %s root netem delay %dms";
		this.delay = 0;
		return String.format(command, CliHelperLola.getCli().iface, delay);
	}

	public void addDelay(Double delay) throws IOException {
		addDelay(delay.intValue());
	}

	public void addDelay(int delay) throws IOException {
		logger.info("Delay: {}ms", delay);
		stopDelay();
		if (delay > 0) {
			String command = getCommand(Math.min(delay, 1000));
			Runtime.getRuntime().exec(command);
		}
	}

	public void stopDelay() throws IOException {
		String command = delCommand();
		Runtime.getRuntime().exec(command);
	}
}
