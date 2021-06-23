package at.htlAnich.backTestingSuite;

public enum ProgramArguments {
	DEBUG,
	file,
	inProduction,
	notAdjusted;

	public static final String PREFIX = "--";

	@Override
	public String toString() {
		return PREFIX.concat(this.name());
	}
}
