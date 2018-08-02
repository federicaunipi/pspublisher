package it.unifi.dinfo.gnocchi;

import java.util.List;

public interface GnocchiAPI {
	void pushMeasurement(Measurement measurement);
	void pushMeasurement(List<Measurement> measurement);

	void pushProcessingCapacity();
}
