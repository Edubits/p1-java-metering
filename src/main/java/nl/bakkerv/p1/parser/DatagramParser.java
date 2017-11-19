package nl.bakkerv.p1.parser;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import nl.bakkerv.p1.device.SmartMeterDescription;
import nl.bakkerv.p1.domain.measurement.Measurement;
import nl.bakkerv.p1.domain.meter.Meter;
import nl.bakkerv.p1.parser.text.InvalidP1TimestampException;
import nl.bakkerv.p1.parser.text.P1Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatagramParser implements SmartMeterDescription {

	private static final Logger logger = LoggerFactory.getLogger(DatagramParser.class);

	DatagramCleaner datagramCleaner = new DatagramCleaner();

	private Map<String, Meter<?>> mapping;
	private String vendorInformation;
	private String version;
	private String meterIdentifier;
	private TimeZone timeZone;

	public DatagramParser() {
		this.mapping = new HashMap<>();
	}

	public Set<Measurement<?>> parse(final String datagram) {
		Set<Measurement<?>> returnValue = new HashSet<>();
		if (datagram == null) {
			return returnValue;
		}
		Map<String, String> datagramLines = cleanupAndSplitDatagram(datagram);
		// Instant timestamp = datagramLines.containsKey(DatagramCodes.TIMESTAMP) ? this.extract : Instant.now(); // FIXME: extract timestamp
		Instant timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		if (datagramLines.containsKey(DatagramCodes.TIMESTAMP)) {
			String p1Value = datagramLines.get(DatagramCodes.TIMESTAMP);
			String p1String = p1Value.substring(1, p1Value.length() - 1);
			try {
				timestamp = P1Timestamp.parse(p1String, this.timeZone).getZonedDateTime().toInstant();
			} catch (InvalidP1TimestampException e) {
				logger.debug("Could not parse {} as P1 timestamp {}", p1String, e.getMessage());
			}
		}
		for (Map.Entry<String, String> e : datagramLines.entrySet()) {
			Meter<?> meter = this.mapping.get(e.getKey());
			if (meter == null) {
				continue;
			}
			Optional<?> extractedMeasurement = meter.extractMeasurement(timestamp, e.getValue());
			if (extractedMeasurement.isPresent()) {
				returnValue.add((Measurement<?>) extractedMeasurement.get());
			}
		}
		return returnValue;
	}

	private Map<String, String> cleanupAndSplitDatagram(final String datagram) {
		return this.datagramCleaner.splitDiagram(datagram).entrySet().stream()
				.filter(s -> this.mapping.containsKey(s.getKey()) || DatagramCodes.TIMESTAMP.equals(s.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public String getVersion() {
		return this.version;
	}

	@Override
	public String getVendorInformation() {
		return this.vendorInformation;
	}

	@Override
	public String getMeterIdentifier() {
		return this.meterIdentifier;
	}

	@Override
	public Map<String, Meter<?>> getMapping() {
		return this.mapping;
	}

	public static class Builder {
		DatagramParser instance;

		protected Builder() {
			this.instance = new DatagramParser();
		}

		public Builder withMeterIdentifier(final String meterID) {
			instance.meterIdentifier = meterID;
			return this;
		}

		public Builder withVendorInformation(final String vendorInfo) {
			instance.vendorInformation = vendorInfo;
			return this;
		}

		public Builder withVersion(final String version) {
			instance.version = version;
			return this;
		}

		public Builder withTimeZone(TimeZone timeZone) {
			instance.timeZone = timeZone;
			return this;
		}

		public DatagramParser build() {
			DatagramParser returnValue = instance;
			instance = null;
			return returnValue;
		}

		public Builder addPropertyParser(final String id, final Meter<?> meter) {
			instance.mapping.put(id, meter);
			return this;
		}
	}

}
