package com.bobrust.settings;

import java.awt.Color;

import com.bobrust.util.Sign;

public interface RustSettings {
	void setBorderColor(Color color);
	void setToolbarColor(Color color);
	void setLabelColor(Color color);
	void setEditorImageDirectory(String pathname);
	void setEditorPresetDirectory(String pathname);
	void setSettingsAlpha(int alpha);
	void setSettingsMaxShapes(int maxShapes);
	void setSettingsCallbackInterval(int callbackInterval);
	void setSettingsSign(Sign sign);
	void setSettingsBackground(Color color);
	void setSettingsClickInterval(int interval);
	void setSettingsAutosaveInterval(int interval);
	
	Color getBorderColor();
	Color getToolbarColor();
	Color getLabelColor();
	String getEditorImageDirectory();
	String getEditorPresetDirectory();
	int getSettingsAlpha();
	int getSettingsMaxShapes();
	int getSettingsCallbackInterval();
	int getSettingsAutosaveInterval();
	Color getSettingsBackground();
	Sign getSettingsSign();
	int getSettingsClickInterval();
}
