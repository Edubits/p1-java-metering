package nl.bakkerv.p1.device;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.TooManyListenersException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import nl.bakkerv.p1.configuration.SmartMeterDeviceConfiguration;
import nl.bakkerv.p1.configuration.SmartMeterParserConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@Singleton
public class SmartMeterDevice implements SerialPortEventListener, SmartMeterDeviceInterface {

	private static final Logger logger = LoggerFactory.getLogger(SmartMeterDevice.class);

	private static final int START_CHARACTER = '/';
	private static final int FINISH_CHARACTER = '!';

	public enum ReaderState {
		Disabled,
		Waiting,
		Reading,
		Checksum
	}

	@Inject
	private SmartMeterParserConfiguration smartMeterParserConfiguration;

	@Inject
	private P1DatagramListener smartMeterListener;

	private SerialPort serialPort;
	private int crc;
	protected int maxBufferSize = 4096;
	protected ByteBuffer buffer = ByteBuffer.allocate(maxBufferSize);
	protected ByteBuffer checksum = ByteBuffer.allocate(10);
	protected ReaderState readerState = ReaderState.Disabled;

	@PostConstruct
	public void init() {
		SmartMeterDeviceConfiguration smartMeterConfig = smartMeterParserConfiguration.getSmartMeter();

		logger.info("Initializing SmartMeterDevice at {}", smartMeterConfig.getPortName());
		logger.info("Port settings: {}", smartMeterConfig);

		readerState = ReaderState.Disabled;
		buffer = ByteBuffer.allocate(maxBufferSize);
		try {
			CommPortIdentifier commPortIdentifier = CommPortIdentifier.getPortIdentifier(smartMeterConfig.getPortName());
			serialPort = commPortIdentifier.open("p1meter", smartMeterConfig.getPortTimeOut());
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			serialPort.setSerialPortParams(smartMeterConfig.getSmartMeterPortSettings().getBaudRate(),
					smartMeterConfig.getSmartMeterPortSettings().getDataBits().getBits(),
					smartMeterConfig.getSmartMeterPortSettings().getStopBits().getStopBits(),
					smartMeterConfig.getSmartMeterPortSettings().getParity().getParity());

			logger.info("Finished initializing SmartMeterDevice.");
			readerState = ReaderState.Waiting;

		} catch (TooManyListenersException | PortInUseException | UnsupportedCommOperationException | NoSuchPortException e) {
			logger.error(e.toString(), e);
		}

	}

	@PreDestroy
	public void destroy() {
		serialPort.close();
	}

	@Override
	public void serialEvent(final SerialPortEvent serialPortEvent) {
		if (serialPortEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				for (int i = 0; i < serialPort.getInputStream().available(); i++) {
					int read = serialPort.getInputStream().read();
					if (read == -1) {
						logger.debug("No bytes available");
						continue;
					}
					byte c = (byte) read;
					handleCharacter(c);
				}
			} catch (IOException e) {
				logger.error(e.toString(), e);
			}
		}
	}

	protected void handleCharacter(final byte c) {
		switch (readerState) {
			case Disabled:
				break;
			case Reading:
				crc = crc16_update(crc, c);
				if (c == FINISH_CHARACTER) {
					readerState = ReaderState.Checksum;
					checksum.clear();
					logger.debug("Saw ! -> Checksum");
				}
				this.buffer.put(c);
				break;
			case Waiting:
				if (c == START_CHARACTER) {
					logger.debug("Saw /, go to Reading");
					readerState = ReaderState.Reading;
					buffer.clear();
					buffer.put(c);
					crc = crc16_update(0, c);
				}
				break;
			case Checksum:
				// we are reading the checksum (optionally, not present in V3)
				this.checksum.put(c);
				if (this.checksum.position() == 4 || c == '\r' || c == '\n') {
					logger.debug("Read {} checksum chars, verify checksum", checksum.position());
					boolean checksumCorrect = false;
					if (checksum.position() == 4) {
						// done reading checksum
						try {
							String checksumText = new String(checksum.array(), 0, checksum.position());
							logger.debug("Checksum text, {}", checksumText);
							int receivedCrc16 = Integer.parseInt(checksumText, 16);
							logger.debug("Done reading checksum: {} vs {}", receivedCrc16, crc);
							byte[] data = new byte[buffer.position()];
							for (int i = 0; i < data.length; i++) {
								data[i] = buffer.get(i);
							}
							if (crc == receivedCrc16) {
								logger.debug("Checkum is correct");
								checksumCorrect = true;
							}
						} catch (Exception e) {
							logger.error("Could not verify checksum {}", e.getMessage(), e);
						}
					}
					if (checksumCorrect || checksum.position() < 4 && (c == '\r' || c == '\n')) {
						String datagram = new String(buffer.array(), 0, buffer.position());
						logger.debug("read datagram: {}", datagram);
						smartMeterListener.put(datagram);
					}

					buffer.clear();
					checksum.clear();
					readerState = ReaderState.Waiting;
					break;
				}
		}
	}

	private int crc16_update(int crc, final byte a) {
		crc ^= a;
		for (int i = 0; i < 8; ++i) {
			if ((crc & 1) != 0) {
				crc = crc >> 1 & 0xFFFF ^ 0xA001;
			} else {
				crc = crc >> 1;
			}
		}
		return crc;
	}

}
