package nl.bakkerv.p1.device;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import nl.bakkerv.p1.domain.measurement.Measurement;
import nl.bakkerv.p1.domain.meter.Meter;
import nl.bakkerv.p1.parser.DatagramParser;
import nl.bakkerv.p1.parser.DatagramParserFactory;

public class P1DatagramListenerImpl implements P1DatagramListener {

	private static final Logger logger = LoggerFactory.getLogger(P1DatagramListenerImpl.class);

	private DatagramParser datagramParser = null;

	@Inject
	private Set<SmartMeterMeasurementListener> listeners;
	@Inject
	private DatagramParserFactory parserFactory;

	private Set<Measurement<?>> currentMeasurement;

	@Override
	public void put(final String datagram) {

		if (logger.isTraceEnabled()) {
			logger.trace(datagram);
		}

		if (this.datagramParser == null) {
			logger.info("No parser configured yet, create datagram parser based on seen datagram");
			Optional<DatagramParser> datagramParserOpt = this.parserFactory.create(datagram);
			if (datagramParserOpt.isPresent()) {
				this.datagramParser = datagramParserOpt.get();
				logger.info("Created {}", this.datagramParser);
				Collection<Meter<?>> meters = this.datagramParser.getMapping().values();
				this.listeners.forEach(s -> s.metersDiscovered(meters));
			} else {
				logger.warn("Could not create parser for datagram, ignoring datagram");
				return;
			}
		}

		Set<Measurement<?>> measurement = this.datagramParser.parse(datagram);

		this.currentMeasurement = measurement;
		logger.debug("Listeners {}", this.listeners);
		this.listeners.forEach(l -> l.smartMeterMeasurementRead(measurement));
	}

	public Set<Measurement<?>> getCurrentMeasurements() {
		return this.currentMeasurement;
	}
}
