package nl.bakkerv.p1.device;

import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Singleton;

import nl.bakkerv.p1.configuration.SmartMeterParserConfiguration;
import nl.bakkerv.p1.domain.event.SmartMeterDiscoveredAnnotationLiteral;
import nl.bakkerv.p1.domain.event.SmartMeterNewMeasurementAnnotationLiteral;
import nl.bakkerv.p1.domain.measurement.Measurement;
import nl.bakkerv.p1.domain.measurement.Measurements;
import nl.bakkerv.p1.parser.DatagramParser;
import nl.bakkerv.p1.parser.DatagramParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class P1DatagramListener {

	private static final Logger logger = LoggerFactory.getLogger(P1DatagramListener.class);

	@Inject
	private BeanManager beanManager;

	@Inject
	private SmartMeterParserConfiguration smartMeterParserConfiguration;

	private DatagramParser datagramParser = null;

	private Set<Measurement<?>> currentMeasurement;

	public void put(final String datagram) {

		if (logger.isTraceEnabled()) {
			logger.trace(datagram);
		}

		if (this.datagramParser == null) {
			logger.info("No parser configured yet, create datagram parser based on seen datagram");
			Optional<DatagramParser> datagramParserOpt = new DatagramParserFactory().create(datagram, smartMeterParserConfiguration);
			if (datagramParserOpt.isPresent()) {
				datagramParser = datagramParserOpt.get();
				logger.info("Created {}", datagramParser);

				beanManager.fireEvent(datagramParser, new SmartMeterDiscoveredAnnotationLiteral());
			} else {
				logger.warn("Could not create parser for datagram, ignoring datagram");
				return;
			}
		}

		Set<Measurement<?>> measurement = this.datagramParser.parse(datagram);

		currentMeasurement = measurement;

		logger.debug("New measurement: " + measurement);
		beanManager.fireEvent(new Measurements(measurement), new SmartMeterNewMeasurementAnnotationLiteral());
	}

	public Set<Measurement<?>> getCurrentMeasurements() {
		return this.currentMeasurement;
	}
}
