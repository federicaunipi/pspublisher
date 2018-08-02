package it.unifi.dinfo.gnocchi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class FakeGnocchiApi implements GnocchiAPI {
	private TrafficController trafficController = new TrafficController();
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void pushMeasurement(Measurement measurement) {

	}

	@Override
	public void pushMeasurement(List<Measurement> measurement) {
		for (Measurement m : measurement) {

			if (CliHelperLola.getCli().delay && m.getName().equals("processing_time")) {
				try {
					trafficController.addDelay(m.getValue() * 1000);
				} catch (IOException e) {
					logger.error("Error while setting delay: {}", e.getMessage());
				}
			}
		}
	}

	@Override
	public void pushProcessingCapacity() {

	}
}
