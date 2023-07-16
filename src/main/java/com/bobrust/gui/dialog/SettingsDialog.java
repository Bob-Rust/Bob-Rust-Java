package com.bobrust.gui.dialog;

import com.bobrust.lang.RustUI;
import com.bobrust.lang.RustUI.Type;
import com.bobrust.settings.Settings;
import com.bobrust.settings.type.*;
import com.bobrust.settings.type.parent.GuiElement;
import com.bobrust.settings.type.parent.SettingsType;
import com.bobrust.util.UrlUtils;
import com.bobrust.util.data.AppConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.io.File;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class SettingsDialog extends AbstractSettingsDialog {
	private static final Logger LOGGER = LogManager.getLogger(SettingsDialog.class);
	private final SignPickerDialog signPicker;
	private final RustColorPicker colorPicker;
	private final JDialog dialog;
	
	// TabbedPane
	final JTabbedPane tabbedPane;
	
	// Editor
	final JButton btnResetEditor;
	
	private final java.util.List<IUpdateValue> applyEdits;
	
	public SettingsDialog(JDialog parent) {
		this.applyEdits = new ArrayList<>();
		this.dialog = new JDialog(parent, RustUI.getString(Type.EDITOR_SETTINGSDIALOG_TITLE), ModalityType.APPLICATION_MODAL);
		this.dialog.setIconImage(AppConstants.DIALOG_ICON);
		this.dialog.setSize(300, 360);
		this.dialog.setResizable(false);
		this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.colorPicker = new RustColorPicker(dialog);
		this.signPicker = new SignPickerDialog(dialog);
		
		dialog.getContentPane().setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.X_AXIS));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.setFocusable(false);
		dialog.getContentPane().add(tabbedPane);
		
		TabbedPane generatorPane = createPane(tabbedPane, Type.EDITOR_TAB_GENERATOR);
		TabbedPane editorPane = createPane(tabbedPane, Type.EDITOR_TAB_EDITOR);
		TabbedPane debugPane = createPane(tabbedPane, Type.EDITOR_TAB_DEBUGGING);
		
		var settings = Settings.InternalSettings.getSettings();
		
		// Automatically generate settings
		for (var key : settings.keySet()) {
			GuiElement annotation = settings.get(key);
			SettingsType<?> setting = Settings.InternalSettings.getSetting(key);
			
			TabbedPane pane = switch (annotation.tab()) {
				case Generator -> generatorPane;
				case Editor -> editorPane;
				case Debugging -> debugPane;
			};
			
			IUpdateValue runnable = addSetting(annotation, setting, pane);
			if (runnable != null) {
				applyEdits.add(runnable);
			}
		}
		
		// Custom buttons
		btnResetEditor = addButtonField(
			editorPane,
			Type.EDITOR_RESETEDITOR_LABEL,
			Type.EDITOR_RESETEDITOR_TOOLTIP,
			Type.EDITOR_RESETEDITOR_BUTTON,
			e -> {
				int dialogResult = JOptionPane.showConfirmDialog(dialog,
					RustUI.getString(Type.EDITOR_RESETEDITORDIALOG_MESSAGE),
					RustUI.getString(Type.EDITOR_RESETEDITORDIALOG_TITLE),
					JOptionPane.YES_NO_OPTION
				);
				if (dialogResult == JOptionPane.YES_OPTION) {
					Settings.EditorBorderColor.set(null);
					Settings.EditorToolbarColor.set(null);
					Settings.EditorLabelColor.set(null);
					
					// Update fields
					updateComponentValues();
				}
			}
		);
		
		addButtonField(debugPane, Type.DEBUG_OPENCONFIGDIRECTORY_LABEL, null, Type.DEBUG_OPENCONFIGDIRECTORY_BUTTON, e -> {
			UrlUtils.openDirectory(new File("").getAbsoluteFile());
		});
	}
	
	private IUpdateValue addSetting(GuiElement annotation, SettingsType<?> settingType, TabbedPane pane) {
		// Custom options are always buttons
		if (annotation.type() == GuiElement.Type.Custom) {
			if (annotation.button() == Type.NONE) {
				throw new RuntimeException("Setting '" + settingType.getId() + "' with 'button' NONE locale");
			}
			
			addButtonField(
				pane,
				annotation.label(),
				annotation.tooltip(),
				annotation.button(),
				e -> customAction(settingType)
			);
			return null;
		}
		
		// Generated options
		if (settingType instanceof ColorType setting) {
			if (annotation.button() == Type.NONE) {
				throw new RuntimeException("Setting '" + settingType.getId() + "' with 'button' NONE locale");
			}
			
			addButtonField(
				pane,
				annotation.label(),
				annotation.tooltip(),
				annotation.button(),
				e -> {
					Color color = JColorChooser.showDialog(dialog, RustUI.getString(Type.EDITOR_COLORDIALOG_TITLE), setting.get(), false);
					if (color != null) {
						setting.set(color);
					}
				}
			);
		} else if (settingType instanceof EnumType<?> setting) {
			Enum<?>[] values = setting.getEnumValues();
			var element = addComboBoxField(
				pane,
				setting.getIndex(),
				values,
				annotation.label(),
				annotation.tooltip()
			);
			
			return save -> {
				if (save) {
					setting.setAbstract(element.getSelectedItem());
				} else {
					element.setSelectedIndex(setting.getIndex());
				}
			};
		} else if (settingType instanceof IntType setting) {
			if (annotation.type() == GuiElement.Type.Combo) {
				if (!setting.isRange()) {
					throw new RuntimeException("Cannot create a combo box from an Int field without range");
				}
				
				Integer[] array = IntStream.rangeClosed(setting.getMin(), setting.getMax()).boxed().toArray(Integer[]::new);
				var element = addComboBoxField(
					pane,
					setting.get() - setting.getMin(),
					array,
					annotation.label(),
					annotation.tooltip()
				);
				
				return save -> {
					if (save) {
						setting.set(element.getSelectedIndex() + setting.getMin());
					} else {
						element.setSelectedIndex(setting.get() - setting.getMin());
					}
				};
			}
			
			var element = addIntegerField(
				pane,
				setting.get(),
				setting.getMin(),
				setting.getMax(),
				annotation.label(),
				annotation.tooltip()
			);
			
			return save -> {
				if (save) {
					try {
						setting.set(element.getNumberValue());
					} catch (NumberFormatException e) {
						setting.setDefault();
					}
				} else {
					element.setValue(setting.get());
				}
			};
		} else if (settingType instanceof BoolType setting) {
			var element = addComboBoxField(
				pane,
				setting.get() ? 1 : 0,
				new String[] { "Off", "On" },
				annotation.label(),
				annotation.tooltip()
			);
			
			return save -> {
				if (save) {
					setting.set(element.getSelectedIndex() == 1);
				} else {
					element.setSelectedIndex(setting.get() ? 1 : 0);
				}
			};
		} else if (settingType instanceof SizeType setting) {
			if (!setting.isRange()) {
				throw new RuntimeException("Cannot create a dimension box without range");
			}
			
			var element = addDimensionField(
				pane,
				setting.get(),
				setting.getMin(),
				setting.getMax(),
				annotation.label(),
				annotation.tooltip()
			);
			
			return save -> {
				if (save) {
					setting.set(element.getDimensionValue());
				} else {
					element.setValue(element.getDimensionValue());
				}
			};
		} else {
			throw new RuntimeException("Failed to generate for setting " + settingType.getId());
		}
		
		return null;
	}
	
	/**
	 * Custom actions will always be buttons
	 */
	private void customAction(SettingsType<?> setting) {
		if (setting == Settings.SettingsBackground) {
			Point dialogLocation = new Point(dialog.getLocationOnScreen());
			dialogLocation.x += 130;
			Settings.SettingsBackground.set(colorPicker.openColorDialog(dialogLocation));
		} else if (setting == Settings.SettingsSign) {
			Point dialogLocation = new Point(dialog.getLocationOnScreen());
			dialogLocation.x += 130;
			signPicker.openSignDialog(dialogLocation);
			Settings.SettingsSign.set(signPicker.getSelectedSign());
		} else {
			LOGGER.warn("Custom action for setting '{}' is not defined", setting.getId());
		}
	}
	
	/**
	 * This method will update the language of all elements in this component.
	 */
	@Override
	public void updateLanguage() {
		LOGGER.warn("Runtime Language changes for the settings dialog is only partially supported!");
		dialog.setTitle(RustUI.getString(Type.EDITOR_SETTINGSDIALOG_TITLE));
		
		super.updateLanguage();
	}
	
	private void updateComponentValues() {
		// Update all component values
		for (IUpdateValue updaters : applyEdits) {
			updaters.action(false);
		}
	}
	
	public void openDialog(Point point) {
		updateComponentValues();
		
		// Show the dialog.
		dialog.setLocation(point);
		dialog.setVisible(true);
		
		// Save all component values
		for (IUpdateValue updaters : applyEdits) {
			updaters.action(true);
		}
	}
	
	private interface IUpdateValue {
		/**
		 * If {@code true} save the current value to the setting,
		 * otherwise load the value from the setting
		 */
		void action(boolean save);
	}
}
