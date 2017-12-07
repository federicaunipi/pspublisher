package it.unifi.dinfo.gnocchi.meters;

import it.unifi.dinfo.gnocchi.CliHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.hardware.NetworkIF;

public class SlackBitrateMeter implements BitrateMeter {
	Long t1;
	Long latestBytesReceived;
	Logger logger = LoggerFactory.getLogger(getClass());
	private NetworkIF iface;


	public SlackBitrateMeter(NetworkIF iface) {
		this.iface = iface;
		iface.updateNetworkStats();
		latestBytesReceived = iface.getBytesRecv();
		t1 = System.currentTimeMillis();
		logger.info("Initialization");
		logger.debug("First value will be sent in "+CliHelper.getCli().interval+"ms");
		try {
			Thread.sleep(CliHelper.getCli().interval);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String scale(double value) {
		String[] suffix = {"B/s", "KB/s", "MB/s", "GB/s", "TB/s", "PB/s"};
		int index;
		if (value < 1)
			index = 0;
		else
			index = (int) Math.floor(Math.log10(value) / 3);
		return String.format("%.2f %s", value / Math.pow(1000, index), suffix[index]);
	}

	public Double getBitrate() {
		Double bitrate;
		iface.updateNetworkStats();
		Long bytesRecv = iface.getBytesRecv();
		long t2 = System.currentTimeMillis();
		double delta = (t2 - t1) / Math.pow(10, 3);
		bitrate = (bytesRecv - latestBytesReceived) / delta;
		latestBytesReceived = bytesRecv;
		t1 = t2;
		logger.info(scale(bitrate));
		return bitrate;
	}

}
