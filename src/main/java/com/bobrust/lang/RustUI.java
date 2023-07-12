package com.bobrust.lang;

public class RustUI {
	public enum Type {
		NONE(""),
		
		WARNING_CONFIGPERMISSION_TITLE("Important"),
		WARNING_CONFIGPERMISSION_MESSAGE(
			"This tool does not have the permission to update the config file\n" +
			"Try run the application as an Administrator if you want to fix this"
		),
		
		ACTION_ABOUTME_TITLE("About me"),
		ACTION_ABOUTME_MESSAGE(
			"Created by HardCoded & Sekwah41\n" +
			"\n" +
			"HardCoded\n" +
			"- Design\n" +
			"- Sorting algorithm\n" +
			"- Optimized generation\n" +
			"\n" +
			"Sekwah41\n" +
			"- Initial generation"
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
		
		EDITOR_SIGNPICKERDIALOG_TITLE("Sign Picker"),
		EDITOR_DRAWDIALOG_TITLE("Draw Settings"),
		EDITOR_SETTINGSDIALOG_TITLE("Settings"),
		EDITOR_TAB_GENERATOR("Generator"),
		EDITOR_TAB_EDITOR("Editor"),
		EDITOR_TAB_DEBUGGING("Debugging"),
		EDITOR_COLORDIALOG_TITLE("Color picker"),
		
		DEBUG_OPENCONFIGDIRECTORY_LABEL("Open config directory"),
		DEBUG_OPENCONFIGDIRECTORY_BUTTON("Config directory"),
		
		EDITOR_BORDERCOLOR_LABEL("Border Color"),
		EDITOR_BORDERCOLOR_BUTTON("Border Color"),
		EDITOR_BORDERCOLOR_TOOLTIP("The border color of the editor"),
		EDITOR_TOOLBARCOLOR_LABEL("Toolbar Color"),
		EDITOR_TOOLBARCOLOR_BUTTON("Toolbar Color"),
		EDITOR_TOOLBARCOLOR_TOOLTIP("The toolbar color of the editor"),
		EDITOR_LABELCOLOR_LABEL("Label Color"),
		EDITOR_LABELCOLOR_BUTTON("Label Color"),
		EDITOR_LABELCOLOR_TOOLTIP("The label color of the editor"),
		EDITOR_CALLBACKINTERVAL_LABEL("Callback Interval"),
		EDITOR_CALLBACKINTERVAL_TOOLTIP("The amount of shapes to generate before updating the screen"),
		EDITOR_RESETEDITOR_LABEL("Reset Editor"),
		EDITOR_RESETEDITOR_BUTTON("Reset Editor"),
		EDITOR_RESETEDITOR_TOOLTIP("Reset the editor colors"),
		EDITOR_RESETEDITORDIALOG_TITLE("Warning"),
		EDITOR_RESETEDITORDIALOG_MESSAGE("Are you sure you want to reset the editor?"),
		EDITOR_RESETSETTINGS_LABEL("Reset Settings"),
		EDITOR_RESETSETTINGS_BUTTON("Reset Settings"),
		EDITOR_RESETSETTINGS_TOOLTIP("Reset all the settings"),
		EDITOR_RESETSETTINGSDIALOG_TITLE("Warning"),
		EDITOR_RESETSETTINGSDIALOG_MESSAGE("Are you sure you want to reset all the settings?"),

		EDITOR_COLORPICKER_BUTTON_DEFAULT("Default"),
		EDITOR_COLORPICKER_BUTTON_CUSTOM("Custom"),
		EDITOR_COLORPICKER_LABEL_CURRENTCOLOR("Current Color"),
		
		ACTION_OPTIONS_LABEL("Options"),
		ACTION_MAKEFULLSCREEN_ON("Make Fullscreen"),
		ACTION_MAKEFULLSCREEN_OFF("Minimize"),
		ACTION_SELECTMONITOR_BUTTON("Select Monitor"),
		ACTION_LOADIMAGE_BUTTON("Load Image"),
		ACTION_OPTIONS_BUTTON("Options"),
		
		ACTION_REGIONS_LABEL("Regions"),
		ACTION_CANVASREGION_BUTTON("Canvas Region"),
		ACTION_IMAGEREGION_BUTTON("Image Region"),
		
		ACTION_PREVIEWACTIONS_LABEL("Preview Actions"),
		ACTION_STARTGENERATE_BUTTON("Start Generate"),
		ACTION_RESETGENERATE_BUTTON("Reset Generate"),
		ACTION_DRAWIMAGE_BUTTON("Draw Image"),
		ACTION_CLOSE_BUTTON("Close"),
		
		ACTION_CLOSEDIALOG_TITLE("Warning"),
		ACTION_CLOSEDIALOG_MESSAGE("Do you want to close the application?"),

		ACTION_HELP_LABEL("Help"),
		ACTION_REPORTISSUE_BUTTON("Report Issue"),
		ACTION_DONATE_BUTTON("Donate"),
		ACTION_ABOUT_BUTTON("About"),
		
		ACTION_SHAPECOUNT_LABEL("Shape Count"),
		ACTION_CALCULATEEXACTTIME_BUTTON("Calculate Exact Time"),
		ACTION_STARTDRAWING_BUTTON("Start Drawing"),
		ACTION_SELECTCOLORPALETTE_BUTTON("Select Color Palette"),
		
		ACTION_PALETTEWARNINGDIALOG_TITLE("Could not find the palette"),
		
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
		// TODO: Create language files
		return type.defaultValue;
	}
}
