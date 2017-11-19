package nl.bakkerv.p1.configuration;

import java.util.Optional;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SmartMeterParserConfiguration {

	@JsonProperty
	SmartMeterDeviceConfiguration smartMeter = new SmartMeterDeviceConfiguration();

	@JsonProperty
	TimeZone timeZone = TimeZone.getDefault();

	@JsonProperty
	Optional<String> dsmrVersionOverride = Optional.empty();

	public SmartMeterDeviceConfiguration getSmartMeter() {
		return this.smartMeter;
	}

	public TimeZone getTimeZone() {
		return this.timeZone;
	}

	public Optional<String> getDsmrVersionOverride() {
		return this.dsmrVersionOverride;
	}

}
