package com.bobrust.settings;

public enum Settings {
	BORDER_COLOR("editor.bordercolor"),
	TOOLBAR_COLOR("editor.toolbarcolor"),
	LABEL_COLOR("editor.labelcolor"),
	IMAGE_PATH("editor.imagepath"),
	PRESET_PATH("editor.presetpath"),
	CALLBACK_INTERVAL("editor.callbackinterval"),
	
	SETTINGS_ALPHA("settings.alpha"),
	SETTINGS_SCALING("settings.scaling"),
	SETTINGS_MAX_SHAPES("settings.maxshapes"),
	SETTINGS_SIGN_TYPE("settings.signtype"),
	SETTINGS_BACKGROUND("settings.background"),
	SETTINGS_CLICK_INTERVAL("settings.clickinterval"),
	SETTINGS_AUTOSAVE_INTERVAL("settings.autosaveinterval"),
	SETTINGS_USE_ICC_CONVERSION("settings.iccconversion"),
	;
	
	
	public final String id;
	
	Settings(String id) {
		this.id = id;
	}
}
