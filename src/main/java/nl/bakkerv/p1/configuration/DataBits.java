package nl.bakkerv.p1.configuration;

import gnu.io.SerialPort;

public enum DataBits {

	FIVE(SerialPort.DATABITS_5),
	SIX(SerialPort.DATABITS_6),
	SEVEN(SerialPort.DATABITS_7),
	EIGHT(SerialPort.DATABITS_8);

	int bits;

	DataBits(final int bits) {
		this.bits = bits;
	}

	public int getBits() {
		return this.bits;
	}
}