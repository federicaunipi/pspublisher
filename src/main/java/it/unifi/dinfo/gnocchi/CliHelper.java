package it.unifi.dinfo.gnocchi;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

public class CliHelper {
	private static CliHelper args;

	@Parameter(names = {"--interval", "-i"}, description = "Interval in milliseconds")
	public Long interval = 60 * 1000L;
	@Parameter(names = {"--ip"}, description = "Openstack host address")
	public String ip = "192.168.9.131";
	@Parameter(names = {"--username"}, description = "Openstack username")
	public String username = "admin";
	@Parameter(names = {"--password"}, description = "Openstack password")
	public String password = "password";
	@Parameter(names = {"--domain"}, description = "Domain name")
	public String domain = "default";
	@Parameter(names = {"--project"}, required = true, description = "Project id (not the name)")
	public String projectId;
	@Parameter(names = {"--instance"}, required = true, description = "Instance id (not the name)")
	public String instance;
	@Parameter(names = {"--iface"}, required = true, description = "Interface name")
	public String iface;
	@Parameter(names = {"--proc_capacity"}, description = "Processing capacity in MB/s", validateWith = PositiveDoubleValidator.class)
	public Double proccessing_capacity = 1.0;
	@Parameter(names = {"--metric"}, description = "The name of the metric")
	public String metric = "processing_time";
	@Parameter(names = "--help", help = true, description = "Print help :)")
	private boolean help;

	public CliHelper() {
	}

	public static CliHelper parse(String[] argv) {
		args = new CliHelper();
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

	public static CliHelper getCli() {
		if (args == null) {
			throw new RuntimeException("Arguments not initialized");
		}
		return args;
	}
}
