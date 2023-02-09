package com.bobrust.settings;

import java.awt.Color;

import com.bobrust.util.Sign;

public interface RustSettings {
	void setEditorBorderColor(Color color);
	void setEditorToolbarColor(Color color);
	void setEditorLabelColor(Color color);
	void setEditorImageDirectory(String pathname);
	void setEditorPresetDirectory(String pathname);
	void setEditorCallbackInterval(Integer callbackInterval);
	void setSettingsAlpha(Integer alpha);
	void setSettingsScaling(Integer index);
	void setSettingsMaxShapes(Integer maxShapes);
	void setSettingsSign(Sign sign);
	void setSettingsBackground(Color color);
	void setSettingsClickInterval(Integer interval);
	void setSettingsAutosaveInterval(Integer interval);
	void setSettingsUseICCConversion(Integer enabled);
	
	Color getEditorBorderColor();
	Color getEditorToolbarColor();
	Color getEditorLabelColor();
	String getEditorImageDirectory();
	String getEditorPresetDirectory();
	int getEditorCallbackInterval();
	int getSettingsAlpha();
	
	/**
	 * Returns:
	 * <pre>
	 * 0: Nearest neighbour
	 * 1: Bilinear
	 * 2: Bicubic
	 * </pre>
	 */
	int getSettingsScaling();
	int getSettingsMaxShapes();
	int getSettingsAutosaveInterval();
	int getSettingsUseICCConversion();
	Color getSettingsBackground();
	Sign getSettingsSign();
	int getSettingsClickInterval();
}
