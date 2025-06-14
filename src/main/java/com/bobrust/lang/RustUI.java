package com.bobrust.lang;

// TODO: Remove this class. No need to make this app so much more complicated by language?
//       Or at least change how this works 
public class RustUI {
	public enum Type {
		NONE(""),
		
		WARNING_CONFIGPERMISSION_TITLE("Important"),
		WARNING_CONFIGPERMISSION_MESSAGE(
			"This tool does not have the permission to update the config file\n" +
			"Try run the application as an Administrator if you want to fix this"
		),
		
		SETTINGS_CLICKINTERVAL_LABEL("Clicks per second"),
		SETTINGS_CLICKINTERVAL_TOOLTIP("The amount of clicks per second"),
		SETTINGS_AUTOSAVEINTERVAL_LABEL("Autosave Interval"),
		SETTINGS_AUTOSAVEINTERVAL_TOOLTIP("How many presses done between saves"),
		SETTINGS_USEICCCONVERSION_LABEL("Use ICC colors"),
		SETTINGS_USEICCCONVERSION_TOOLTIP("If ICC color conversion should be used"),
		SETTINGS_ALPHAINDEX_LABEL("Alpha Index"),
		SETTINGS_ALPHAINDEX_TOOLTIP("The alpha value used when drawing the image"),
		SETTINGS_SCALINGTYPE_LABEL("Scaling Type"),
		SETTINGS_SCALINGTYPE_TOOLTIP("The type of scaling performed when resizing the image"),
		SETTINGS_MAXSHAPES_LABEL("Max Shapes"),
		SETTINGS_MAXSHAPES_TOOLTIP("The max amount of shapes used when drawing the image"),
		SETTINGS_SIGNTYPE_LABEL("Sign Type"),
		SETTINGS_SIGNTYPE_BUTTON("Select Sign Type"),
		SETTINGS_SIGNTYPE_TOOLTIP("The type of sign used when drawing"),
		SETTINGS_CUSTOMSIGNDIMENSION_LABEL("Custom Sign Size"),
		SETTINGS_CUSTOMSIGNDIMENSION_TOOLTIP("Custom size of the sign"),
		SETTINGS_BACKGROUNDCOLOR_LABEL("Background Color"),
		SETTINGS_BACKGROUNDCOLOR_BUTTON("Background Color"),
		SETTINGS_BACKGROUNDCOLOR_TOOLTIP("The background color of the canvas"),
		
		EDITOR_SETTINGSDIALOG_TITLE("Settings"),
		EDITOR_TAB_GENERATOR("Generator"),
		EDITOR_TAB_EDITOR("Editor"),
		EDITOR_TAB_DEBUGGING("Debugging"),
		EDITOR_COLORDIALOG_TITLE("Color picker"),
		
		DEBUG_OPENCONFIGDIRECTORY_LABEL("Open config directory"),
		DEBUG_OPENCONFIGDIRECTORY_BUTTON("Config directory"),
		DEBUG_CLEARBUTTONCONFIG_LABEL("Clear button configuration"),
		DEBUG_CLEARBUTTONCONFIG_BUTTON("Clear buttons"),
		
		EDITOR_CALLBACKINTERVAL_LABEL("Callback Interval"),
		EDITOR_CALLBACKINTERVAL_TOOLTIP("The amount of shapes to generate before updating the screen"),
		EDITOR_RESETSETTINGS_LABEL("Reset Settings"),
		EDITOR_RESETSETTINGS_BUTTON("Reset Settings"),
		EDITOR_RESETSETTINGS_TOOLTIP("Reset all the settings to default"),
		EDITOR_RESETSETTINGSDIALOG_TITLE("Warning"),
		EDITOR_RESETSETTINGSDIALOG_MESSAGE("Are you sure you want to reset all settings to default?"),
		
		;
		
		private final String id;
		private final String defaultValue;
		
		Type(String defaultValue) {
			this.id = name().replace('_', '.').toLowerCase();
			this.defaultValue = defaultValue;
		}
		
		public String getId() {
			return id;
		}
	}
	
	public static String getString(Type type) {
		return type.defaultValue;
	}
}
