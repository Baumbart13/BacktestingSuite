package at.htlAnich.backTestingSuite;

public enum ProgramArguments {
	DEBUG,
	file,
	inProduction;

	public static final String PREFIX = "--";

	@Override
	public String toString() {
		return PREFIX.concat(this.name());
	}
}
