package nl.bakkerv.p1.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import nl.bakkerv.p1.testutil.TestObjectFactory;
import org.junit.Before;
import org.junit.Test;

public class DatagramCleanerTest {

	private DatagramCleaner cleanerUnderTest;

	@Before
	public void setUp() {
		this.cleanerUnderTest = new DatagramCleaner();
	}

	@Test
	public void asArray() throws Exception {
		assertThat(this.cleanerUnderTest.asArray(TestObjectFactory.getTestV3Datagram())).hasSize(19);
	}

	@Test
	public void splitDiagram() {
		/*
		0-0:96.1.1(31333631363433322020202020202020)
		1-0:1.8.1(00038.113*kWh)
		1-0:1.8.2(00026.006*kWh)
		1-0:2.8.1(00001.104*kWh)
		1-0:2.8.2(00006.696*kWh)
		0-0:96.14.0(0002)
		1-0:1.7.0(0000.00*kW)
		1-0:2.7.0(0000.55*kW)
		0-0:17.0.0(999*A)
		0-0:96.3.10(1)
		0-0:96.13.1()
		0-0:96.13.0()
		0-1:96.1.0(3238303131303038323437303539393132)
		0-1:24.1.0(03)
		0-1:24.3.0(130206140000)(00)(60)(1)(0-1:24.2.0)(m3)
		(00047.057)
		0-1:24.4.0(1)
		 */
		Map<String, String> actual = this.cleanerUnderTest.splitDiagram(TestObjectFactory.getTestV3Datagram());
		Map<String, String> expected = new HashMap<>();
		expected.put("0-0:96.1.1", "(31333631363433322020202020202020)");
		expected.put("1-0:1.8.1", "(00038.113*kWh)");
		expected.put("1-0:1.8.2", "(00026.006*kWh)");
		expected.put("1-0:2.8.1", "(00001.104*kWh)");
		expected.put("1-0:2.8.2", "(00006.696*kWh)");
		expected.put("0-0:96.14.0", "(0002)");
		expected.put("1-0:1.7.0", "(0000.00*kW)");
		expected.put("1-0:2.7.0", "(0000.55*kW)");
		expected.put("0-0:17.0.0", "(999*A)");
		expected.put("0-0:96.3.10", "(1)");
		expected.put("0-0:96.13.1", "()");
		expected.put("0-0:96.13.0", "()");
		expected.put("0-1:96.1.0", "(3238303131303038323437303539393132)");
		expected.put("0-1:24.1.0", "(03)");
		expected.put("0-1:24.3.0", "(130206140000)(00)(60)(1)(0-1:24.2.0)(m3)(00047.057)");
		expected.put("0-1:24.4.0", "(1)");
		assertThat(actual).isEqualTo(expected);
	}

}
