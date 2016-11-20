package nl.bakkerv.p1.parser;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.bakkerv.p1.domain.SmartMeterMeasurement;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class DatagramParser {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, PropertyAndPattern> mapping;

    public DatagramParser() {
        mapping = new HashMap<String, PropertyAndPattern>();
        mapping.put("1-0:1.8.1", new PropertyAndPattern(new KwhValueParser(), "electricityConsumptionLowRateKwh"));
        mapping.put("1-0:1.8.2", new PropertyAndPattern(new KwhValueParser(), "electricityConsumptionNormalRateKwh"));
        mapping.put("1-0:2.8.1", new PropertyAndPattern(new KwhValueParser(), "electricityProductionLowRateKwh"));
        mapping.put("1-0:2.8.2", new PropertyAndPattern(new KwhValueParser(), "electricityProductionNormalRateKwh"));
        mapping.put("1-0:1.7.0", new PropertyAndPattern(new WattValueParser(), "currentPowerConsumptionW"));
        mapping.put("1-0:2.7.0", new PropertyAndPattern(new WattValueParser(), "currentPowerProductionW"));
        mapping.put("0-1:24.3.0", new PropertyAndPattern(new CubicMetreValueParser(), "gasConsumptionM3"));
    }

    public SmartMeterMeasurement parse(String datagram) {

        SmartMeterMeasurement result = new SmartMeterMeasurement();

        String[] datagramLines = DatagramCleaner.asArray(datagram);

        for (String line : datagramLines) {

            for (Map.Entry<String, PropertyAndPattern> entry : mapping.entrySet()) {
                if (line.startsWith(entry.getKey())) {
                    entry.getValue().extract(line, result);
                    break;
                }
            }
        }

        return result;
    }

    private class PropertyAndPattern {

        private ValueParser valueParser;
        private String fieldName;

        public PropertyAndPattern(ValueParser valueParser, String fieldName) {
            this.valueParser = valueParser;
            this.fieldName = fieldName;
        }

        public void extract(String line, SmartMeterMeasurement measurement) {
            BigDecimal value = valueParser.parse(line);
            try {
                PropertyUtils.setProperty(measurement, fieldName, value);
            } catch (IllegalAccessException e) {
                logger.error(e.toString(), e);
            } catch (InvocationTargetException e) {
                logger.error(e.toString(), e);
            } catch (NoSuchMethodException e) {
                logger.error(e.toString(), e);
            }
        }
    }
}
