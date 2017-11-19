package nl.bakkerv.p1.domain.meter;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import nl.bakkerv.p1.domain.measurement.Measurement;
import nl.bakkerv.p1.parser.text.TimestampedValue;
import nl.bakkerv.p1.parser.text.ValueParser;

public class Meter<T> {

	private String channel;
	private ValueParser<T> parser;
	private Direction direction;
	private Unit unit;
	private Kind kind;
	private MeterType meterType;
	private int tariff;
	private String identifier;

	private Meter() {
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.channel, this.direction, this.unit, this.kind,
				this.meterType, this.tariff, this.identifier);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		Meter<?> other = (Meter<?>) obj;
		return Objects.equals(this.channel, other.channel) &&
				Objects.equals(this.direction, other.direction) &&
				Objects.equals(this.unit, other.unit) &&
				Objects.equals(this.kind, other.kind) &&
				Objects.equals(this.tariff, other.tariff) &&
				Objects.equals(this.identifier, other.identifier) &&
				Objects.equals(this.meterType, other.meterType);
	}

	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	public String getChannel() {
		return this.channel;
	}

	public ValueParser<T> getParser() {
		return this.parser;
	}

	public Unit getUnit() {
		return this.unit;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public Kind getEnergy() {
		return this.kind;
	}

	public MeterType getMeterType() {
		return this.meterType;
	}

	public int getTariff() {
		return this.tariff;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
				.add("identifier: " + this.identifier)
				.add("kind: " + this.kind)
				.add("meterType: " + this.meterType)
				.add("channel: " + this.channel)
				.add("tariff: " + this.tariff)
				.add("unit: " + this.unit)
				.add("direction: " + this.direction)
				.toString();
	}

	public Optional<Measurement<T>> extractMeasurement(final Instant timestampDatagram, final String value) {
		Optional<TimestampedValue<T>> parsedValue = this.parser.parse(value);
		if (!parsedValue.isPresent()) {
			return Optional.empty();
		}
		TimestampedValue<T> timestampedValue = parsedValue.get();
		return Optional.of(new Measurement<>(timestampedValue.getTimestamp().orElse(timestampDatagram), this, timestampedValue.getValue()));
	}

	public static class Builder<T> {
		private Meter<T> instance = new Meter<>();

		public Builder<T> withChannel(final String channel) {
			this.instance.channel = channel;
			return this;
		}

		public Builder<T> withParser(final ValueParser<T> parser) {
			this.instance.parser = parser;
			return this;
		}

		public Builder<T> withUnit(final Unit unit) {
			this.instance.unit = unit;
			return this;
		}

		public Builder<T> withDirection(final Direction direction) {
			this.instance.direction = direction;
			return this;
		}

		public Builder<T> withKind(final Kind kind) {
			this.instance.kind = kind;
			return this;
		}

		public Builder<T> withMeterType(final MeterType mt) {
			this.instance.meterType = mt;
			return this;
		}

		public Meter<T> build() {
			Meter<T> returnValue = this.instance;
			this.instance = null;
			return returnValue;
		}

		public Builder<T> withTariff(final int t) {
			this.instance.tariff = t;
			return this;
		}

		public Builder<T> withIdentifier(final String id) {
			this.instance.identifier = id;
			return this;
		}

	}

}
