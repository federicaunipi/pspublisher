package it.unifi.dinfo.gnocchi;

import oshi.SystemInfo;
import oshi.hardware.NetworkIF;

import java.util.Arrays;
import java.util.List;

public class NetIFHelper {
	public static NetworkIF getInterfaceByName(String ifaceName) {
		NetworkIF[] networkIFs = new SystemInfo().getHardware().getNetworkIFs();
		List<NetworkIF> networkIFS = Arrays.asList(networkIFs);
		NetworkIF iface = networkIFS.stream().filter(i -> i.getName().equals(ifaceName)).findFirst().orElse(null);
		if (iface == null) {
			StringBuffer str = new StringBuffer();
			str.append("Unable to find interface with name: " + ifaceName + "\nValid names are:\n");
			networkIFS.stream().forEach(n -> str.append(n.getName() + "\n"));
			throw new RuntimeException(str.toString());
		}
		return iface;
	}
}
