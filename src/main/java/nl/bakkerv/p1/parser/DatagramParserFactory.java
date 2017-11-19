package nl.bakkerv.p1.parser;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import nl.bakkerv.p1.configuration.SmartMeterParserConfiguration;
import nl.bakkerv.p1.domain.meter.Direction;
import nl.bakkerv.p1.domain.meter.Kind;
import nl.bakkerv.p1.domain.meter.Meter;
import nl.bakkerv.p1.domain.meter.MeterType;
import nl.bakkerv.p1.domain.meter.Unit;
import nl.bakkerv.p1.parser.DatagramParser.Builder;
import nl.bakkerv.p1.parser.text.DSMRVersionParser;
import nl.bakkerv.p1.parser.text.KwhValueParser;
import nl.bakkerv.p1.parser.text.MeterIdentifierParser;
import nl.bakkerv.p1.parser.text.TimestampedValue;
import nl.bakkerv.p1.parser.text.V4TimestampAndCubicMeterParser;
import nl.bakkerv.p1.parser.text.ValueParser;
import nl.bakkerv.p1.parser.text.WattValueParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatagramParserFactory {

	private static final Logger logger = LoggerFactory.getLogger(DatagramParserFactory.class);

	private static final Pattern VALUE_EXPRESSION = Pattern.compile("\\((.+)\\)");

	private DatagramCleaner datagramCleaner = new DatagramCleaner();
	private SmartMeterParserConfiguration smartMeterParserConfiguration;

	public Optional<DatagramParser> create(final String dataGram, final SmartMeterParserConfiguration smartMeterParserConfiguration) {
		this.smartMeterParserConfiguration = smartMeterParserConfiguration;

		Builder newParser = new DatagramParser.Builder();
		newParser.withTimeZone(smartMeterParserConfiguration.getTimeZone());
		Map<String, String> cleanedUp = datagramCleaner.splitDiagram(dataGram);
		Optional<String> dsmrVersion = extractField(cleanedUp, DatagramCodes.DSMR_VERSION, DSMRVersionParser::new);
		Optional<String> meterID = extractField(cleanedUp, DatagramCodes.SMART_METER_ID, MeterIdentifierParser::new);
		if (!meterID.isPresent()) {
			logger.error("No meter identifier present.");
			return Optional.empty();
		}
		if (smartMeterParserConfiguration.getDsmrVersionOverride().isPresent()) {
			logger.info("Smart Meter version override present, using {}", smartMeterParserConfiguration.getDsmrVersionOverride().get());
			dsmrVersion = this.smartMeterParserConfiguration.getDsmrVersionOverride();
		}
		if (!dsmrVersion.isPresent()) {
			logger.error("No DSMR version present");
			return Optional.empty();
		}
		newParser.withVersion("DSMR-" + dsmrVersion.get());
		newParser.withMeterIdentifier(meterID.get());
		newParser.withVendorInformation(datagramCleaner.asArray(dataGram)[0].substring(1));

		if (cleanedUp.containsKey(DatagramCodes.ELECTRICITY_CURRENT_POWER_CONSUMPTION)) {
			addCurrentPowerMeter(DatagramCodes.ELECTRICITY_CURRENT_POWER_CONSUMPTION, newParser, Direction.TO_CLIENT, meterID.orElse(null));
		}
		if (cleanedUp.containsKey(DatagramCodes.ELECTRICITY_CURRENT_POWER_PRODUCTION)) {
			addCurrentPowerMeter(DatagramCodes.ELECTRICITY_CURRENT_POWER_PRODUCTION, newParser, Direction.FROM_CLIENT, meterID.orElse(null));
		}
		if (cleanedUp.containsKey(DatagramCodes.ELECTRICITY_CONSUMPTION_RATE_1)) {
			generatekWhMeter(DatagramCodes.ELECTRICITY_CONSUMPTION_RATE_1, newParser, Direction.TO_CLIENT, 1, meterID.orElse(null));
		}
		if (cleanedUp.containsKey(DatagramCodes.ELECTRICITY_CONSUMPTION_RATE_2)) {
			generatekWhMeter(DatagramCodes.ELECTRICITY_CONSUMPTION_RATE_2, newParser, Direction.TO_CLIENT, 2, meterID.orElse(null));
		}
		if (cleanedUp.containsKey(DatagramCodes.ELECTRICITY_PRODUCTION_RATE_1)) {
			generatekWhMeter(DatagramCodes.ELECTRICITY_PRODUCTION_RATE_1, newParser, Direction.FROM_CLIENT, 1, meterID.orElse(null));
		}
		if (cleanedUp.containsKey(DatagramCodes.ELECTRICITY_PRODUCTION_RATE_2)) {
			generatekWhMeter(DatagramCodes.ELECTRICITY_PRODUCTION_RATE_2, newParser, Direction.FROM_CLIENT, 2, meterID.orElse(null));
		}

		Set<String> keysToIgnore = Stream.of("1-3", "0-0", "1-0").collect(toSet());
		Map<String, Set<Entry<String, String>>> perBusID = cleanedUp.entrySet().stream()
				.collect(groupingBy(
						entry -> entry.getKey().split(":")[0],
						toSet()
				));

		perBusID.entrySet().stream()
				.filter(s -> !keysToIgnore.contains(s.getKey()))
				.peek(s -> logger.info("Extracting for {}", s))
				.forEach(s -> extractMeter(newParser, s.getValue()));

		return Optional.of(newParser.build());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void extractMeter(final Builder newParser, final Collection<Entry<String, String>> values) {
		Kind kind = null;
		String identifier = null;
		ValueParser parser = null;
		String obisCode = null;
		for (Entry<String, String> e : values) {
			String code = e.getKey().split(":")[1];
			if ("24.1.0".equals(code)) {
				Matcher matcher = VALUE_EXPRESSION.matcher(e.getValue());
				if (!matcher.matches()) {
					logger.warn("{} does not match expected pattern", e.getKey());
					// no meter type known, abort
					return;
				}
				int type = Integer.parseInt(matcher.group(1));
				if (type != 3) {
					logger.warn("Unknown meter type {}", type);
					return;
				}
				kind = Kind.GAS;
			}
			if ("96.1.0".equals(code)) {
				Matcher matcher = VALUE_EXPRESSION.matcher(e.getValue());
				if (!matcher.matches()) {
					logger.warn("{} does not match expected pattern", e.getKey());
					// no meter type known, abort
					return;
				}
				identifier = matcher.group(1);
			}
			if ("24.2.1".equals(code)) {
				obisCode = e.getKey();
				parser = new V4TimestampAndCubicMeterParser(smartMeterParserConfiguration.getTimeZone());
			}
		}
		if (parser != null && kind != null && identifier != null) {
			newParser.addPropertyParser(obisCode, Meter.builder()
					.withKind(kind)
					.withIdentifier(identifier)
					.withMeterType(MeterType.INTEGRAL)
					.withUnit(Unit.CUBIC_METER)
					.withDirection(Direction.TO_CLIENT)
					.withParser(parser)
					.build());
		}
	}

	private void generatekWhMeter(final String id, final Builder newParser, final Direction direction, final int tariff,
			final String identifier) {
		newParser.addPropertyParser(id,
				Meter.<BigDecimal> builder()
						.withUnit(Unit.KILOWATTHOUR)
						.withKind(Kind.ELECTRICITY)
						.withMeterType(MeterType.INTEGRAL)
						.withDirection(direction)
						.withParser(new KwhValueParser())
						.withIdentifier(identifier)
						.withTariff(tariff)
						.build());
	}

	private void addCurrentPowerMeter(final String id, final Builder newParser, final Direction d, final String meterID) {
		newParser.addPropertyParser(id,
				Meter.<BigDecimal> builder()
						.withChannel("Total")
						.withParser(new WattValueParser())
						.withUnit(Unit.WATT)
						.withKind(Kind.ELECTRICITY)
						.withMeterType(MeterType.INSTANTANEOUS)
						.withIdentifier(meterID)
						.withDirection(d)
						.build());
	}

	private <T> Optional<T> extractField(final Map<String, String> cleanedUp, final String field, final Supplier<ValueParser<T>> parserSupplier) {
		if (cleanedUp.containsKey(field)) {
			final Optional<TimestampedValue<T>> value = parserSupplier.get().parse(cleanedUp.get(field));
			if (!value.isPresent()) {
				return Optional.empty();
			}
			return Optional.of(value.get().getValue());
		} else {
			return Optional.empty();
		}
	}

}
