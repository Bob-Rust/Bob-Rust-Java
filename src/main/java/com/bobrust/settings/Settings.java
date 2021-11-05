package com.bobrust.settings;

public enum Settings {
	BORDER_COLOR("editor.bordercolor"),
	TOOLBAR_COLOR("editor.toolbarcolor"),
	LABEL_COLOR("editor.labelcolor"),
	IMAGE_PATH("editor.imagepath"),
	PRESET_PATH("editor.presetpath"),
	SETTINGS_ALPHA("settings.alpha"),
	SETTINGS_SCALING("settings.scaling"),
	SETTINGS_MAX_SHAPES("settings.maxshapes"),
	SETTINGS_CALLBACK_INTERVAL("settings.callbackinterval"),
	SETTINGS_SIGN_TYPE("settings.signtype"),
	SETTINGS_BACKGROUND("settings.background"),
	SETTINGS_CLICK_INTERVAL("settings.clickinterval"),
	SETTINGS_AUTOSAVE_INTERVAL("settings.autosaveinterval");
	
	public final String id;
	
	private Settings(String id) {
		this.id = id;
	}
}
