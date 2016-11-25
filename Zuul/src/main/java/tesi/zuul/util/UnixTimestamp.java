package tesi.zuul.util;

public class UnixTimestamp {

	public static long getCurrentUnixTimestamp() {
		return System.currentTimeMillis() / 1000L;
	}
}
