package it.unifi.dinfo.gnocchi;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Measurement {

	private LocalDateTime timestamp;
	private Double value;

	public Measurement(Double value) {
		this.value = value;
		this.timestamp = LocalDateTime.now(ZoneId.of("UTC"));
	}

	public Measurement() {
	}

	private LocalDateTime getTimestampAsZonedDateTime() {
		return timestamp;
	}

	public String getTimestamp() {
		return timestamp.toString();
	}

	public Measurement setTimestampAsLocalDateTime(LocalDateTime timestamp) {
		this.timestamp = timestamp;
		return this;
	}

	public Measurement setTimestamp(String timestamp) {
		this.timestamp = LocalDateTime.parse(timestamp);
		return this;
	}

	public Double getValue() {
		return value;
	}

	public Measurement setValue(Double value) {
		this.value = value;
		return this;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Measurement{");
		sb.append("timestamp=").append(timestamp);
		sb.append(", value=").append(value);
		sb.append('}');
		return sb.toString();
	}
}
