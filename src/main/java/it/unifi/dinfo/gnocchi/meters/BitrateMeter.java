package it.unifi.dinfo.gnocchi.meters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.hardware.NetworkIF;

public class BitrateMeter {
	private long interval;
	private NetworkIF iface;
	Logger logger = LoggerFactory.getLogger(getClass());

	public BitrateMeter(long interval, NetworkIF iface) {
		this.interval = interval;
		this.iface = iface;
	}

	public double getBitrate() {
		long bytesRecv;
		long bytesRecv2;
		iface.updateNetworkStats();
		bytesRecv = iface.getBytesRecv();
		long t1 = System.nanoTime();
		try {
			Thread.sleep(interval);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		iface.updateNetworkStats();
		bytesRecv2 = iface.getBytesRecv();
		long t2 = System.nanoTime();
		double delta = (t2 - t1) / Math.pow(10, 9);
		double bitrate = (bytesRecv2 - bytesRecv) / delta;
		logger.debug(String.valueOf(delta));
		logger.info(scale(bitrate));
		return bitrate;
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

}
