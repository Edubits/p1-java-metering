package nl.bakkerv.p1.device;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import nl.bakkerv.p1.device.SmartMeterDevice.ReaderState;
import nl.bakkerv.p1.testutil.TestObjectFactory;

public class SmartMeterDeviceTest implements P1DatagramListener {

	private String readDatagram = null;

	public class SmartMeterDeviceTestModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(P1DatagramListener.class).toInstance(SmartMeterDeviceTest.this);
		}

	}

	@Test
	public void testReadingFromPort() {
		Injector j = Guice.createInjector(new SmartMeterDeviceTestModule());
		SmartMeterDevice smd = j.getInstance(SmartMeterDevice.class);
		String dataGram = TestObjectFactory.getTestV3Datagram();
		smd.buffer = ByteBuffer.allocate(smd.maxBufferSize);
		smd.readerState = ReaderState.Waiting;
		dataGram.chars().forEachOrdered(e -> smd.handleCharacter((byte) e));
		String trimmed = this.readDatagram.trim();
		assertThat(trimmed).isEqualTo(dataGram.trim());
	}

	@Override
	public void put(final String datagram) {
		this.readDatagram = datagram;
	}

}
