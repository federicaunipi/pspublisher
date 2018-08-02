package it.unifi.dinfo.gnocchi;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class CliHelperLola {
	private static CliHelperLola args;

	@Parameter(names = {"--interval", "-i"}, description = "Interval in milliseconds")
	public Long interval = 60 * 1000L;
	@Parameter(names = {"--ip"}, required = true, description = "Gnocchi host")
	public String ip;
	@Parameter(names = {"--name"}, required = true, description = "Virtual function name")
	public String name;
	@Parameter(names = {"--datacenter"}, required = true, description = "Name of the datacenter")
	public String datacenter;
	@Parameter(names = {"--policy"}, description = "Default archive policy for metrics")
	public String policy = "high";

	@Parameter(names = {"--delay"}, description = "Add delay corresponding to the computed processing time")
	public Boolean delay = false;




	@Parameter(names = {"--username"}, description = "Openstack username")
	public String username = "admin";
	@Parameter(names = {"--password"}, description = "Openstack password")
	public String password = "password";

	@Parameter(names = {"--iface"}, required = true, description = "Interface name")
	public String iface;
	@Parameter(names = {"--capacity"}, description = "Processing capacity in Mbit/s", validateWith = PositiveDoubleValidator.class)
	public Double capacity = 1.0;

	@Parameter(names = "--help", help = true, description = "Print help :)")
	private boolean help;

	public CliHelperLola() {
	}

	public static CliHelperLola parse(String[] argv) {
		args = new CliHelperLola();
		JCommander cli = JCommander.newBuilder().addObject(args).build();
		try {
			cli.parse(argv);
		} catch (ParameterException e) {
			System.out.println(e.getMessage());
			e.usage();
			System.exit(-1);
		}
		return args;
	}

	public static CliHelperLola getCli() {
		if (args == null) {
			throw new RuntimeException("Arguments not initialized");
		}
		return args;
	}
}
