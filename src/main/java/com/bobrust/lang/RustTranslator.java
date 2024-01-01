package com.bobrust.lang;

public class RustTranslator {
	public static String getTimeMinutesMessage(long milliseconds) {
		long seconds = milliseconds / 1000L;
		long minutes = seconds / 60L;
		
		int minuteIndex = (int) (minutes);
		int secondIndex = (int) (seconds % 60L);
		
		StringBuilder sb = new StringBuilder();
		if (minuteIndex > 0) {
			sb.append(minuteIndex).append(" minute").append(minuteIndex == 1 ? " " : "s ");
		}
		if (secondIndex > 0 || seconds == 0) {
			sb.append(secondIndex).append(" second").append(secondIndex == 1 ? " " : "s ");
		}
		return sb.toString().trim();
	}
}
