package it.unifi.dinfo.gnocchi.meters;

import it.unifi.dinfo.gnocchi.CliHelperLola;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.hardware.NetworkIF;

public class SlackBitrateMeter implements BitrateMeter {
	private long t1a, t1b;
	private long latestBytesReceived;
	private long latestPktsReceived;
	Logger logger = LoggerFactory.getLogger(getClass());
	private NetworkIF iface;


	public SlackBitrateMeter(NetworkIF iface) {
		this.iface = iface;
		iface.updateNetworkStats();
		//latestBytesReceived = iface.getBytesRecv();
		latestBytesReceived = iface.getBytesRecv() * 8;
		latestPktsReceived = iface.getPacketsRecv();
		t1a = System.currentTimeMillis();
		t1b = t1a;
		logger.info("Initialization");
		logger.debug("First value will be sent in "+ CliHelperLola.getCli().interval+"ms");
		try {
			Thread.sleep(CliHelperLola.getCli().interval);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String scale(double value) {
		String[] suffix = {"b/s", "Kb/s", "Mb/s", "Gb/s", "Tb/s", "Pb/s"};
		int index;
		if (value < 1)
			index = 0;
		else
			index = (int) Math.floor(Math.log10(value) / 3);
		return String.format("%.2f %s", value / Math.pow(1000, index), suffix[index]);
	}

	public static String scale(double value,String uom) {
		String[] suffix = {uom+"/s", "K"+uom+"/s", "M"+uom+"/s", "G"+uom+"b/s", "T"+uom+"b/s", "P"+uom+"b/s"};
		int index;
		if (value < 1)
			index = 0;
		else
			index = (int) Math.floor(Math.log10(value) / 3);
		return String.format("%.2f %s", value / Math.pow(1000, index), suffix[index]);
	}

	public Double getBitrate() {
		iface.updateNetworkStats();
		// Bits
		long bytesRecv = iface.getBytesRecv()*8;
		// Bytes
		//Long bytesRecv = iface.getBytesRecv();
		long t2 = System.currentTimeMillis();
		double delta = (t2 - t1a) / Math.pow(10, 3);
		double bitrate = (bytesRecv - latestBytesReceived) / delta;
		latestBytesReceived = bytesRecv;
		t1a = t2;
		logger.debug(scale(bitrate));
		return bitrate;
	}

	@Override
	public Double getPacketRate() {
		// Bits
		long pktsRecv = iface.getPacketsRecv();

		// Bytes
		//Long bytesRecv = iface.getBytesRecv();
		long t2 = System.currentTimeMillis();
		double delta = (t2 - t1b) / 1000;
		double pktRate = (pktsRecv - latestPktsReceived) / delta;
		latestPktsReceived = pktsRecv;
		t1b = t2;
		logger.debug(pktRate+" pkts/s");
		return pktRate;
	}


}
