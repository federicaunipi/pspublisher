package it.unifi.dinfo.gnocchi.meters;

import it.unifi.dinfo.gnocchi.Measurement;

import java.util.List;

public interface ProcessingTimeMeter {
	double getProcessingTime();
	List<Measurement> getMeasurements();
}
