package com.bobrust.logging;

public class LogUtils {
	private static final String WARNING = "Warn",
								INFO = "Info",
								ERROR = "Error";
	
	public static void warn(String format, Object... args) {
		LogUtils.log(WARNING, format.formatted(args));
	}
	
	public static void info(String format, Object... args) {
		LogUtils.log(INFO, format.formatted(args));
	}
	
	public static void error(String format, Object... args) {
		LogUtils.log(ERROR, format.formatted(args));
	}
	
	private static void log(String level, String message) {
		System.out.printf("[%s] %s\n", level, message);
	}
}
