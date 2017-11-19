//package nl.bakkerv.p1;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.Map;
//import java.util.Optional;
//import java.util.Set;
//
//import org.junit.Test;
//
//import com.google.inject.Guice;
//import com.google.inject.Injector;
//
//import nl.bakkerv.p1.domain.measurement.Measurement;
//import nl.bakkerv.p1.domain.meter.Direction;
//import nl.bakkerv.p1.domain.meter.Kind;
//import nl.bakkerv.p1.domain.meter.Meter;
//import nl.bakkerv.p1.domain.meter.MeterType;
//import nl.bakkerv.p1.domain.meter.Unit;
//import nl.bakkerv.p1.parser.DatagramParser;
//import nl.bakkerv.p1.parser.DatagramParserFactory;
//import nl.bakkerv.p1.testutil.TestObjectFactory;
//
//public class ParserIntegrationTest {
//
//	private static final Meter<Object> GAS_METER = Meter.builder()
//			.withDirection(Direction.TO_CLIENT)
//			.withKind(Kind.GAS)
//			.withMeterType(MeterType.INTEGRAL)
//			.withUnit(Unit.CUBIC_METER)
//			.withIdentifier("3232323241424344313233343536373839")
//			.build();
//	private static final Meter<Object> W_FROM_CLIENT = Meter.builder()
//			.withDirection(Direction.FROM_CLIENT)
//			.withKind(Kind.ELECTRICITY)
//			.withMeterType(MeterType.INSTANTANEOUS)
//			.withUnit(Unit.WATT)
//			.withChannel("Total")
//			.withIdentifier("4B384547303034303436333935353037")
//			.build();
//	private static final Meter<Object> W_TO_CLIENT = Meter.builder()
//			.withDirection(Direction.TO_CLIENT)
//			.withKind(Kind.ELECTRICITY)
//			.withMeterType(MeterType.INSTANTANEOUS)
//			.withUnit(Unit.WATT)
//			.withIdentifier("4B384547303034303436333935353037")
//			.withChannel("Total")
//			.build();
//	private static final Meter<Object> KWH_FROM_CLIENT_2 = Meter.builder()
//			.withDirection(Direction.FROM_CLIENT)
//			.withKind(Kind.ELECTRICITY)
//			.withMeterType(MeterType.INTEGRAL)
//			.withTariff(2)
//			.withUnit(Unit.KILOWATTHOUR)
//			.withIdentifier("4B384547303034303436333935353037")
//			.build();
//	private static final Meter<Object> KWH_FROM_CLIENT_1 = Meter.builder()
//			.withDirection(Direction.FROM_CLIENT)
//			.withKind(Kind.ELECTRICITY)
//			.withMeterType(MeterType.INTEGRAL)
//			.withTariff(1)
//			.withUnit(Unit.KILOWATTHOUR)
//			.withIdentifier("4B384547303034303436333935353037")
//			.build();
//	private static final Meter<Object> KWH_TO_CLIENT_2 = Meter.builder()
//			.withDirection(Direction.TO_CLIENT)
//			.withKind(Kind.ELECTRICITY)
//			.withMeterType(MeterType.INTEGRAL)
//			.withTariff(2)
//			.withIdentifier("4B384547303034303436333935353037")
//			.withUnit(Unit.KILOWATTHOUR)
//			.build();
//	private static final Meter<Object> KWH_TO_CLIENT_1 = Meter.builder()
//			.withDirection(Direction.TO_CLIENT)
//			.withKind(Kind.ELECTRICITY)
//			.withMeterType(MeterType.INTEGRAL)
//			.withTariff(1)
//			.withUnit(Unit.KILOWATTHOUR)
//			.withIdentifier("4B384547303034303436333935353037")
//			.build();
//
//	@Test
//	public void testV5() {
//		Injector j = Guice.createInjector(SmartMeterParserModule.create("src/test/resources/config.yaml"));
//		DatagramParserFactory factory = j.getInstance(DatagramParserFactory.class);
//		String testV5Datagram = TestObjectFactory.getTestV5Datagram();
//		Optional<DatagramParser> parser = factory.create(testV5Datagram);
//		assertThat(parser).isPresent();
//		assertThat(parser.get().getMeterIdentifier()).isEqualTo("4B384547303034303436333935353037");
//		assertThat(parser.get().getVersion()).isEqualTo("DSMR-50");
//		assertThat(parser.get().getVendorInformation()).isEqualTo("ISk5\\2MT382-1000");
//		Map<String, Meter<?>> meters = parser.get().getMapping();
//		hasCorrectMeters(meters);
//		Set<Measurement<?>> parsedValues = parser.get().parse(testV5Datagram);
//		hasCorrectValues(parsedValues);
//	}
//
//	@Test
//	public void testV3_fallbackOfVersion() {
//		Injector j = Guice.createInjector(SmartMeterParserModule.create("src/test/resources/config-3.yaml"));
//		DatagramParserFactory factory = j.getInstance(DatagramParserFactory.class);
//		String testV3Datagram = TestObjectFactory.getTestV3Datagram();
//		Optional<DatagramParser> parser = factory.create(testV3Datagram);
//		assertThat(parser).isPresent();
//		assertThat(parser.get().getMeterIdentifier()).isEqualTo("31333631363433322020202020202020");
//		assertThat(parser.get().getVersion()).isEqualTo("DSMR-30");
//		assertThat(parser.get().getVendorInformation()).isEqualTo("XMX5XMXABCE000062529");
//		Set<Measurement<?>> parsedValues = parser.get().parse(testV3Datagram);
//		final Instant timestamp = parsedValues.stream().filter(m -> m.getMeter().getMeterType() == MeterType.INSTANTANEOUS).findAny().get().getTimestamp();
//		// test for truncated timestamps
//		assertThat(timestamp.getNano()).isEqualTo(0);
//	}
//
//	private void hasCorrectValues(final Set<Measurement<?>> parsedValues) {
//
//		// Check gas
//		Optional<Measurement<?>> anyGasValue = parsedValues.stream().filter(f -> f.getMeter().equals(GAS_METER)).findAny();
//		assertThat(anyGasValue).isPresent();
//		Measurement<?> theGasValue = anyGasValue.get();
//		assertThat(theGasValue.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1291890300));
//		assertThat(theGasValue.getValue()).isEqualTo(new BigDecimal("12785.123"));
//
//		// kwh <- 1
//		Optional<Measurement<?>> kwh11 = parsedValues.stream().filter(f -> f.getMeter().equals(KWH_FROM_CLIENT_1)).findAny();
//		assertThat(kwh11).isPresent();
//		Measurement<?> the_kwh11 = kwh11.get();
//		assertThat(the_kwh11.getValue()).isEqualTo(new BigDecimal("123453.789"));
//		assertThat(the_kwh11.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1291890620));
//
//		// kwh <- 2
//		Optional<Measurement<?>> kwh12 = parsedValues.stream().filter(f -> f.getMeter().equals(KWH_FROM_CLIENT_2)).findAny();
//		assertThat(kwh12).isPresent();
//		Measurement<?> the_kwh12 = kwh12.get();
//		assertThat(the_kwh12.getValue()).isEqualTo(new BigDecimal("123454.789"));
//		assertThat(the_kwh12.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1291890620));
//
//		// kwh -> 1
//		Optional<Measurement<?>> kwh13 = parsedValues.stream().filter(f -> f.getMeter().equals(KWH_TO_CLIENT_1)).findAny();
//		assertThat(kwh13).isPresent();
//		Measurement<?> the_kwh13 = kwh13.get();
//		assertThat(the_kwh13.getValue()).isEqualTo(new BigDecimal("123451.789"));
//		assertThat(the_kwh13.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1291890620));
//
//		// kwh -> 2
//		Optional<Measurement<?>> kwh14 = parsedValues.stream().filter(f -> f.getMeter().equals(KWH_TO_CLIENT_2)).findAny();
//		assertThat(kwh14).isPresent();
//		Measurement<?> the_kwh14 = kwh14.get();
//		assertThat(the_kwh14.getValue()).isEqualTo(new BigDecimal("123452.789"));
//		assertThat(the_kwh14.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1291890620));
//
//		// w ->
//		Optional<Measurement<?>> w_to = parsedValues.stream().filter(f -> f.getMeter().equals(W_TO_CLIENT)).findAny();
//		assertThat(w_to).isPresent();
//		Measurement<?> the_w_to = w_to.get();
//		assertThat(the_w_to.getValue()).isEqualTo(BigDecimal.valueOf(1193));
//		assertThat(the_w_to.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1291890620));
//
//		// w ->
//		Optional<Measurement<?>> w_from = parsedValues.stream().filter(f -> f.getMeter().equals(W_FROM_CLIENT)).findAny();
//		assertThat(w_from).isPresent();
//		Measurement<?> the_w_from = w_from.get();
//		assertThat(the_w_from.getValue()).isEqualTo(BigDecimal.valueOf(0));
//		assertThat(the_w_from.getTimestamp()).isEqualTo(Instant.ofEpochSecond(1291890620));
//	}
//
//	@SuppressWarnings("rawtypes")
//	private void hasCorrectMeters(final Map<String, Meter<?>> meters) {
//		assertThat(meters).hasSize(7); // 4 kWh meters, 2 Power, 1 gas
//		assertThat((Meter) meters.get("1-0:1.8.1")).isEqualTo(KWH_TO_CLIENT_1);
//		assertThat((Meter) meters.get("1-0:1.8.2")).isEqualTo(KWH_TO_CLIENT_2);
//		assertThat((Meter) meters.get("1-0:2.8.1")).isEqualTo(KWH_FROM_CLIENT_1);
//		assertThat((Meter) meters.get("1-0:2.8.2")).isEqualTo(KWH_FROM_CLIENT_2);
//		assertThat((Meter) meters.get("1-0:1.7.0")).isEqualTo(W_TO_CLIENT);
//		assertThat((Meter) meters.get("1-0:2.7.0")).isEqualTo(W_FROM_CLIENT);
//		assertThat((Meter) meters.get("0-1:24.2.1")).isEqualTo(GAS_METER);
//	}
//
//}
