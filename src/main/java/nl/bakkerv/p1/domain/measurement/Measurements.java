package nl.bakkerv.p1.domain.measurement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import nl.bakkerv.p1.domain.meter.Direction;
import nl.bakkerv.p1.domain.meter.Kind;
import nl.bakkerv.p1.domain.meter.Meter;
import nl.bakkerv.p1.domain.meter.MeterType;

public class Measurements {
	private Instant timestamp;
	private BigDecimal electricityConsumptionLowRateKwh;
	private BigDecimal electricityConsumptionNormalRateKwh;
	private BigDecimal electricityProductionLowRateKwh;
	private BigDecimal electricityProductionNormalRateKwh;
	private BigDecimal currentPowerConsumptionW;
	private BigDecimal currentPowerProductionW;
	private BigDecimal gasConsumptionM3;

	public Measurements(Set<Measurement<?>> measurement) {
		measurement.forEach(m -> {
			Meter<?> meter = m.getMeter();
			if (meter.getDirection() == Direction.TO_CLIENT) {
				if (meter.getEnergy() == Kind.ELECTRICITY) {
					timestamp = m.getTimestamp();

					if (meter.getMeterType() == MeterType.INTEGRAL) {
						if (meter.getTariff() == 1) {
							electricityConsumptionLowRateKwh = (BigDecimal) m.getValue();
						} else if (meter.getTariff() == 2) {
							electricityConsumptionNormalRateKwh = (BigDecimal) m.getValue();
						}
					} else {
						currentPowerConsumptionW = (BigDecimal) m.getValue();
					}
				}
			}
		});
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public BigDecimal getElectricityConsumptionLowRateKwh() {
		return electricityConsumptionLowRateKwh;
	}

	public void setElectricityConsumptionLowRateKwh(BigDecimal electricityConsumptionLowRateKwh) {
		this.electricityConsumptionLowRateKwh = electricityConsumptionLowRateKwh;
	}

	public BigDecimal getElectricityConsumptionNormalRateKwh() {
		return electricityConsumptionNormalRateKwh;
	}

	public void setElectricityConsumptionNormalRateKwh(BigDecimal electricityConsumptionNormalRateKwh) {
		this.electricityConsumptionNormalRateKwh = electricityConsumptionNormalRateKwh;
	}

	public BigDecimal getElectricityProductionLowRateKwh() {
		return electricityProductionLowRateKwh;
	}

	public void setElectricityProductionLowRateKwh(BigDecimal electricityProductionLowRateKwh) {
		this.electricityProductionLowRateKwh = electricityProductionLowRateKwh;
	}

	public BigDecimal getElectricityProductionNormalRateKwh() {
		return electricityProductionNormalRateKwh;
	}

	public void setElectricityProductionNormalRateKwh(BigDecimal electricityProductionNormalRateKwh) {
		this.electricityProductionNormalRateKwh = electricityProductionNormalRateKwh;
	}

	public BigDecimal getCurrentPowerConsumptionW() {
		return currentPowerConsumptionW;
	}

	public void setCurrentPowerConsumptionW(BigDecimal currentPowerConsumptionW) {
		this.currentPowerConsumptionW = currentPowerConsumptionW;
	}

	public BigDecimal getCurrentPowerProductionW() {
		return currentPowerProductionW;
	}

	public void setCurrentPowerProductionW(BigDecimal currentPowerProductionW) {
		this.currentPowerProductionW = currentPowerProductionW;
	}

	public BigDecimal getGasConsumptionM3() {
		return gasConsumptionM3;
	}

	public void setGasConsumptionM3(BigDecimal gasConsumptionM3) {
		this.gasConsumptionM3 = gasConsumptionM3;
	}
}
