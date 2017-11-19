package nl.bakkerv.p1.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatagramCleaner {

	public static class DatagramLine {
		private final String obisCode;
		private final String valueString;

		public DatagramLine(final String obisCode, final String valueString) {
			this.obisCode = obisCode;
			this.valueString = valueString;
		}

		public String getObisCode() {
			return this.obisCode;
		}

		public String getValueString() {
			return this.valueString;
		}
	}

	private static final String OBIS_ID = "obisID";
	private static final String VALUE = "value";
	private static final Pattern P1_DATAGRAM_PATTERN = Pattern.compile("(?<" + OBIS_ID + ">\\d+-\\d+:\\d+\\.\\d+\\.\\d+)(?<" + VALUE + ">\\(.*\\))");

	public String[] asArray(String source) {

		// Remove windowsy line ends
		source = source.replaceAll("\\r", "");

		// Put gas measurements on one line
		source = source.replaceAll("\\(m3\\)\\n", "(m3)");

		return source.split("\\n");
	}

	public Map<String, String> splitDiagram(final String datagram) {
		String[] datagramLines = this.asArray(datagram);
		Map<String, String> lines = new HashMap<>();

		for (String line : datagramLines) {
			splitLine(line).ifPresent(s -> lines.put(s.getObisCode(), s.getValueString()));
		}
		return lines;
	}

	public Optional<DatagramLine> splitLine(final String line) {
		Matcher matcher = P1_DATAGRAM_PATTERN.matcher(line);
		if (!matcher.matches()) {
			return Optional.empty();
		}
		String obis = matcher.group(OBIS_ID);
		if (obis == null) {
			return Optional.empty();
		}
		String value = matcher.group(VALUE);
		if (value != null) {
			return Optional.of(new DatagramLine(obis, value));
		}
		return Optional.empty();
	}
}
