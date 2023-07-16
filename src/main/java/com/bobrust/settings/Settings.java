package com.bobrust.settings;

import java.awt.*;

import com.bobrust.lang.RustUI;
import com.bobrust.settings.data.ScalingType;
import com.bobrust.settings.type.*;
import com.bobrust.settings.type.parent.InternalSettings;
import com.bobrust.settings.type.parent.GuiElement;
import com.bobrust.util.data.RustSigns;

/**
 * Settings interface of the app.
 * 
 * All variables are static and can be accessed
 * 
 * By using the {@code @GuiElement} annotation you can easily add
 * new settings to the app gui. These will be added automatically
 * 
 * @author HardCoded
 */
public interface Settings {
	// Editor
	@GuiElement(tab = GuiElement.Tab.Editor,
		label = RustUI.Type.EDITOR_CALLBACKINTERVAL_LABEL,
		tooltip = RustUI.Type.EDITOR_CALLBACKINTERVAL_TOOLTIP)
	IntType EditorCallbackInterval = new IntType(100, 1, 99999);
	
	StringType EditorImageDirectory = new StringType(System.getProperty("user.home"));
	
	// Generator
	@GuiElement(tab = GuiElement.Tab.Generator, type = GuiElement.Type.Custom,
		label = RustUI.Type.SETTINGS_BACKGROUNDCOLOR_LABEL,
		tooltip = RustUI.Type.SETTINGS_BACKGROUNDCOLOR_TOOLTIP,
		button = RustUI.Type.SETTINGS_BACKGROUNDCOLOR_BUTTON)
	ColorType SettingsBackground = new ColorType(null);
	
	@GuiElement(tab = GuiElement.Tab.Generator, type = GuiElement.Type.Custom,
		label = RustUI.Type.SETTINGS_SIGNTYPE_LABEL,
		tooltip = RustUI.Type.SETTINGS_SIGNTYPE_TOOLTIP,
		button = RustUI.Type.SETTINGS_SIGNTYPE_BUTTON)
	SignType SettingsSign = new SignType(RustSigns.FIRST);
	
	@GuiElement(tab = GuiElement.Tab.Generator,
		label = RustUI.Type.SETTINGS_CUSTOMSIGNDIMENSION_LABEL,
		tooltip = RustUI.Type.SETTINGS_CUSTOMSIGNDIMENSION_TOOLTIP)
	SizeType SettingsSignDimension = new SizeType(
		new Dimension(256, 256),
		new Dimension(1, 1),
		new Dimension(9999, 9999));
	
	@GuiElement(tab = GuiElement.Tab.Generator, type = GuiElement.Type.Combo,
		label = RustUI.Type.SETTINGS_ALPHAINDEX_LABEL,
		tooltip = RustUI.Type.SETTINGS_ALPHAINDEX_TOOLTIP)
	IntType SettingsAlpha = new IntType(2, 0, 5);
	
	/**
	 * Returns:
	 * <pre>
	 * 0: Nearest neighbour
	 * 1: Bilinear
	 * 2: Bicubic
	 * </pre>
	 */
	@GuiElement(tab = GuiElement.Tab.Generator,
		label = RustUI.Type.SETTINGS_SCALINGTYPE_LABEL,
		tooltip = RustUI.Type.SETTINGS_SCALINGTYPE_TOOLTIP)
	EnumType<ScalingType> SettingsScaling = new EnumType<>(ScalingType.Nearest);
	
	@GuiElement(tab = GuiElement.Tab.Generator,
		label = RustUI.Type.SETTINGS_MAXSHAPES_LABEL,
		tooltip = RustUI.Type.SETTINGS_MAXSHAPES_TOOLTIP)
	IntType SettingsMaxShapes = new IntType(99999, 0, 200000);
	
	@GuiElement(tab = GuiElement.Tab.Generator,
		label = RustUI.Type.SETTINGS_CLICKINTERVAL_LABEL,
		tooltip = RustUI.Type.SETTINGS_CLICKINTERVAL_TOOLTIP)
	IntType SettingsClickInterval = new IntType(30, 1, 99999);
	
	@GuiElement(tab = GuiElement.Tab.Generator,
		label = RustUI.Type.SETTINGS_AUTOSAVEINTERVAL_LABEL,
		tooltip = RustUI.Type.SETTINGS_AUTOSAVEINTERVAL_TOOLTIP)
	IntType SettingsAutosaveInterval = new IntType(1000, 1, 99999);
	
	@GuiElement(tab = GuiElement.Tab.Generator,
		label = RustUI.Type.SETTINGS_USEICCCONVERSION_LABEL,
		tooltip = RustUI.Type.SETTINGS_USEICCCONVERSION_TOOLTIP)
	BoolType SettingsUseICCConversion = new BoolType(false);
	
	// Used for internal save state
	InternalSettings InternalSettings = new InternalSettings();
	
	static Color getSettingsBackgroundCalculated() {
		Color color = SettingsBackground.get();
		if (color == null) {
			return SettingsSign.get().getAverageColor();
		}
		
		return color;
	}
}
