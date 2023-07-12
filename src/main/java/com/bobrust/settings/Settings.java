package com.bobrust.settings;

import java.awt.*;

import com.bobrust.settings.type.*;
import com.bobrust.settings.type.parent.InternalSettings;
import com.bobrust.util.RustConstants;
import com.bobrust.util.RustSigns;

/**
 * Settings interface of the app.
 * 
 * All variables are static and can be accessed
 */
public interface Settings {
	ColorType  EditorBorderColor = new ColorType(new Color(0xff3333));
	ColorType  EditorToolbarColor = new ColorType(new Color(0x333333));
	ColorType  EditorLabelColor = new ColorType(Color.white);
	StringType EditorImageDirectory = new StringType(System.getProperty("user.home"));
	StringType EditorPresetDirectory = new StringType(System.getProperty("user.home"));
	IntType    EditorCallbackInterval = new IntType(20, 1, 99999);
	IntType    SettingsAlpha = new IntType(2, 0, 5);
	
	/**
	 * Returns:
	 * <pre>
	 * 0: Nearest neighbour
	 * 1: Bilinear
	 * 2: Bicubic
	 * </pre>
	 */
	IntType    SettingsScaling = new IntType(RustConstants.IMAGE_SCALING_NEAREST, 0, 2);
	IntType    SettingsMaxShapes = new IntType(99999, 0, 99999);
	SignType   SettingsSign = new SignType(RustSigns.FIRST);
	SizeType   SettingsSignDimension = new SizeType(
		new Dimension(1, 1),
		new Dimension(1, 1),
		new Dimension(9999, 9999));
	ColorType  SettingsBackground = new ColorType(null);
	IntType    SettingsClickInterval = new IntType(30, 1, 99999);
	IntType    SettingsAutosaveInterval = new IntType(1000, 1, 99999);
	BoolType   SettingsUseICCConversion = new BoolType(true);
	
	// Used for internal save state
	InternalSettings InternalSettings = new InternalSettings();
}
