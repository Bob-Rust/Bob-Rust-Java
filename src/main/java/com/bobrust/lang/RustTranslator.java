package com.bobrust.lang;

public class RustTranslator {
	public static final String SIGNPICKER_DIALOG_TITLE = "dialog.signpicker.title";
	
	public static final String COLORPICKER_DIALOG_TITLE = "dialog.colorpicker.title";
	public static final String COLORPICKER_CURRENT_COLOR_LABEL = "dialog.colorpicker.label.currentcolor";
	public static final String COLORPICKER_CUSTOM_BUTTON = "dialog.colorpicker.button.custom";
	public static final String COLORPICKER_DEFAULT_BUTTON = "dialog.colorpicker.button.default";
	
	public static final String MAIN_DIALOG_TITLE = "dialog.main.title";
	
	public static final String TOOLBAR_COLOR_REGION = "toolbar.button.colorregion";
	public static final String TOOLBAR_IMAGE_REGION = "toolbar.button.imageregion";
	public static final String TOOLBAR_CANVAS_REGION = "toolbar.button.canvasregion";
	
	public static final String TOPBAR_GENERATION_LABEL_MESSAGE = "topbar.label.generation.message";
	public static final String TOPBAR_GENERATION_LABEL_UNSET = "topbar.label.generation.unset";
	
	public static final String TOOLBAR_MONITOR = "toolbar.button.monitor";
	public static final String TOOLBAR_FULLSCREEN_MAXIMIZE = "toolbar.button.fullscreen.minimize";
	public static final String TOOLBAR_FULLSCREEN_MINIMIZE = "toolbar.button.fullscreen.maximize";
	public static final String TOOLBAR_LOAD_IMAGE = "toolbar.button.loadimage";
	public static final String TOOLBAR_OPTION = "toolbar.button.option";
	public static final String TOOLBAR_CLOSE = "toolbar.button.close";
	public static final String TOOLBAR_ABOUT = "toolbar.button.about";
	public static final String TOOLBAR_REPORT_ISSUE = "toolbar.button.report";
	public static final String TOOLBAR_START_GENERATE = "toolbar.button.startgenerate";
	public static final String TOOLBAR_STOP_GENERATE = "toolbar.button.stopgenerate";
	public static final String TOOLBAR_PAUSE_PAUSE = "toolbar.button.pause.pause";
	public static final String TOOLBAR_PAUSE_RESUME = "toolbar.button.pause.resume";
	public static final String TOOLBAR_HELP_LABEL = "toolbar.label.help";
	public static final String TOOLBAR_REGION_LABEL = "toolbar.label.region";
	public static final String TOOLBAR_PREVIEW_LABEL = "toolbar.label.preview";
	
	public static final String TOOLBAR_ABOUT_TITLE = "toolbar.dialog.about.title";
	public static final String TOOLBAR_ABOUT_MESSAGE = "toolbar.dialog.about.message";
	
	public static String getText(String id) {
		return null;
	}
	
	public static String getTimeMessage(long milliseconds) {
		long seconds = milliseconds / 1000L;
		long minutes = seconds / 60L;
		long hours = minutes / 60L;
		
		int dayIndex = (int)(hours / 24L);
		int hourIndex = (int)(hours % 24L);
		int minuteIndex = (int)(minutes % 60L);
		int secondIndex = (int)(seconds % 60L);
		
		StringBuilder sb = new StringBuilder();
		if(dayIndex > 0) sb.append(dayIndex).append(" day").append(dayIndex == 1 ? " ":"s ");
		if(hourIndex > 0) sb.append(hourIndex).append(" hour").append(hourIndex == 1 ? " ":"s ");
		if(minuteIndex > 0) sb.append(minuteIndex).append(" minute").append(minuteIndex == 1 ? " ":"s ");
		if(secondIndex > 0 || seconds == 0) sb.append(secondIndex).append(" second").append(secondIndex == 1 ? " ":"s ");
		return sb.toString().trim();
	}
	
	public static String getTimeMinutesMessage(long milliseconds) {
		long seconds = milliseconds / 1000L;
		long minutes = seconds / 60L;
		
		int minuteIndex = (int)(minutes);
		int secondIndex = (int)(seconds % 60L);
		
		StringBuilder sb = new StringBuilder();
		if(minuteIndex > 0) sb.append(minuteIndex).append(" minute").append(minuteIndex == 1 ? " ":"s ");
		if(secondIndex > 0 || seconds == 0) sb.append(secondIndex).append(" second").append(secondIndex == 1 ? " ":"s ");
		return sb.toString().trim();
	}
}
