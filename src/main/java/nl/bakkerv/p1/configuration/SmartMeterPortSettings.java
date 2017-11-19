package nl.bakkerv.p1.configuration;

public enum SmartMeterPortSettings {

	V3(9600, DataBits.SEVEN, Parity.EVEN, StopBits.ONE),
	V4(115200, DataBits.EIGHT, Parity.NONE, StopBits.ONE);

	SmartMeterPortSettings(final int baudRate, final DataBits db, final Parity p, final StopBits b) {
		this.baudRate = baudRate;
		this.dataBits = db;
		this.parity = p;
		this.stopBits = b;
	}

	private int baudRate;
	private DataBits dataBits;
	private Parity parity;
	private StopBits stopBits;

	public int getBaudRate() {
		return this.baudRate;
	}

	public DataBits getDataBits() {
		return this.dataBits;
	}

	public Parity getParity() {
		return this.parity;
	}

	public StopBits getStopBits() {
		return this.stopBits;
	}
}
