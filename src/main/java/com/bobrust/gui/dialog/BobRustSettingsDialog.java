package com.bobrust.gui.dialog;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.io.File;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.bobrust.gui.comp.JDimensionField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.gui.BobRustEditor;
import com.bobrust.gui.comp.JIntegerField;
import com.bobrust.lang.RustUI;
import com.bobrust.lang.RustUI.Type;
import com.bobrust.util.RustConstants;
import com.bobrust.util.UrlUtils;

public class BobRustSettingsDialog extends AbstractBobRustSettingsDialog {
	private static final Logger LOGGER = LogManager.getLogger(BobRustSettingsDialog.class);
	private final BobRustEditor gui;
	private final BobRustSignPicker signPicker;
	private final BobRustColorPicker colorPicker;
	private final JDialog dialog;
	
	// TabbedPane
	final JTabbedPane tabbedPane;
	
	// Generator
	final JButton btnBackgroundColor;
	final JButton btnSignType;
	final JDimensionField customSignDimension;
	final JComboBox<Integer> alphaCombobox;
	final JComboBox<String> scalingCombobox;
	final JIntegerField maxShapesField;
	final JIntegerField clickIntervalField;
	final JIntegerField autosaveIntervalField;
	final JComboBox<String> useIccConversionCombobox;
	
	// Editor
	final JButton btnBorderColor;
	final JButton btnToolbarColor;
	final JButton btnLabelColor;
	final JIntegerField callbackIntervalField;
	final JButton btnResetEditor;
	// final JButton btnResetSettings;
	
	public BobRustSettingsDialog(BobRustEditor gui, JDialog parent) {
		this.gui = gui;
		this.dialog = new JDialog(parent, RustUI.getString(Type.EDITOR_SETTINGSDIALOG_TITLE), ModalityType.APPLICATION_MODAL);
		this.dialog.setIconImage(RustConstants.DIALOG_ICON);
		this.dialog.setSize(300, 360);
		this.dialog.setResizable(false);
		this.signPicker = new BobRustSignPicker(gui, dialog);
		this.colorPicker = new BobRustColorPicker(gui, dialog);
		
		dialog.getContentPane().setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.X_AXIS));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.setFocusable(false);
		dialog.getContentPane().add(tabbedPane);
		
		{
			TabbedPane generatorPane = createPane(tabbedPane, Type.EDITOR_TAB_GENERATOR);
			btnBackgroundColor = addButtonField(
				generatorPane,
				Type.SETTINGS_BACKGROUNDCOLOR_LABEL,
				Type.SETTINGS_BACKGROUNDCOLOR_TOOLTIP,
				Type.SETTINGS_BACKGROUNDCOLOR_BUTTON,
				e -> {
					Point dialogLocation = new Point(dialog.getLocationOnScreen());
					dialogLocation.x += 130;
					colorPicker.openColorDialog(dialogLocation);
				}
			);
			
			btnSignType = addButtonField(
				generatorPane,
				Type.SETTINGS_SIGNTYPE_LABEL,
				Type.SETTINGS_SIGNTYPE_TOOLTIP,
				Type.SETTINGS_SIGNTYPE_BUTTON,
				e -> {
					Point dialogLocation = new Point(dialog.getLocationOnScreen());
					dialogLocation.x += 130;
					signPicker.openSignDialog(dialogLocation);
					gui.SettingsSign.set(signPicker.getSelectedSign());
				}
			);
			
			customSignDimension = addDimensionField(
				generatorPane,
				gui.SettingsSignDimension.get(),
				gui.SettingsSignDimension.getMin(),
				gui.SettingsSignDimension.getMax(),
				Type.SETTINGS_CUSTOMSIGNDIMENSION_LABEL,
				Type.SETTINGS_CUSTOMSIGNDIMENSION_TOOLTIP
			);
			
			alphaCombobox = addComboBoxField(
				generatorPane,
				gui.SettingsAlpha.get(),
				new Integer[] { 0, 1, 2, 3, 4, 5 },
				Type.SETTINGS_ALPHAINDEX_LABEL,
				Type.SETTINGS_ALPHAINDEX_TOOLTIP
			);
			
			scalingCombobox = addComboBoxField(
				generatorPane,
				gui.SettingsScaling.get(),
				new String[] { "Nearest", "Bilinear", "Bicubic" },
				Type.SETTINGS_SCALINGTYPE_LABEL,
				Type.SETTINGS_SCALINGTYPE_TOOLTIP
			);
			
			maxShapesField = addIntegerField(
				generatorPane,
				gui.SettingsMaxShapes.get(),
				0,
				99999,
				Type.SETTINGS_MAXSHAPES_LABEL,
				Type.SETTINGS_MAXSHAPES_TOOLTIP
			);
			
			clickIntervalField = addIntegerField(
				generatorPane,
				gui.SettingsClickInterval.get(),
				1,
				60,
				Type.SETTINGS_CLICKINTERVAL_LABEL,
				Type.SETTINGS_CLICKINTERVAL_TOOLTIP
			);
			
			autosaveIntervalField = addIntegerField(
				generatorPane,
				gui.SettingsAutosaveInterval.get(),
				1,
				4000,
				Type.SETTINGS_AUTOSAVEINTERVAL_LABEL,
				Type.SETTINGS_AUTOSAVEINTERVAL_TOOLTIP
			);
			
			useIccConversionCombobox = addComboBoxField(
				generatorPane,
				gui.SettingsUseICCConversion.get() ? 1 : 0,
				new String[] { "Off", "On" },
				Type.SETTINGS_USEICCCONVERSION_LABEL,
				Type.SETTINGS_USEICCCONVERSION_TOOLTIP
			);
		}
		
		{
			TabbedPane editorPane = createPane(tabbedPane, Type.EDITOR_TAB_EDITOR);
			btnBorderColor = addButtonField(
				editorPane,
				Type.EDITOR_BORDERCOLOR_LABEL,
				Type.EDITOR_BORDERCOLOR_TOOLTIP,
				Type.EDITOR_BORDERCOLOR_BUTTON,
				e -> {
					Color color = JColorChooser.showDialog(dialog, RustUI.getString(Type.EDITOR_COLORDIALOG_TITLE), gui.EditorBorderColor.get(), false);
					if (color != null) {
						gui.EditorBorderColor.set(color);
					}
				}
			);
			
			btnToolbarColor = addButtonField(
				editorPane,
				Type.EDITOR_TOOLBARCOLOR_LABEL,
				Type.EDITOR_TOOLBARCOLOR_TOOLTIP,
				Type.EDITOR_TOOLBARCOLOR_BUTTON,
				e -> {
					Color color = JColorChooser.showDialog(dialog, RustUI.getString(Type.EDITOR_COLORDIALOG_TITLE), gui.EditorToolbarColor.get(), false);
					if (color != null) {
						gui.EditorToolbarColor.set(color);
					}
				}
			);
			
			btnLabelColor = addButtonField(
				editorPane,
				Type.EDITOR_LABELCOLOR_LABEL,
				Type.EDITOR_LABELCOLOR_TOOLTIP,
				Type.EDITOR_LABELCOLOR_BUTTON,
				e -> {
					Color color = JColorChooser.showDialog(dialog, RustUI.getString(Type.EDITOR_COLORDIALOG_TITLE), gui.EditorLabelColor.get(), false);
					if (color != null) {
						gui.EditorLabelColor.set(color);
					}
				}
			);
			
			callbackIntervalField = addIntegerField(
				editorPane,
				gui.EditorCallbackInterval.get(),
				0,
				99999,
				Type.EDITOR_CALLBACKINTERVAL_LABEL,
				Type.EDITOR_CALLBACKINTERVAL_TOOLTIP
			);
			
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
						gui.EditorBorderColor.set(null);
						gui.EditorToolbarColor.set(null);
						gui.EditorLabelColor.set(null);
						gui.EditorCallbackInterval.set(null);
						
						// Update fields
						callbackIntervalField.setText(Integer.toString(gui.EditorCallbackInterval.get()));
					}
				}
			);
			
			/*
			btnResetSettings = addButtonField(
				editorPane,
				Type.EDITOR_RESETSETTINGS_LABEL,
				Type.EDITOR_RESETSETTINGS_TOOLTIP,
				Type.EDITOR_RESETSETTINGS_BUTTON,
				e -> {
					int dialogResult = JOptionPane.showConfirmDialog(dialog,
						RustUI.getString(Type.EDITOR_RESETSETTINGSDIALOG_MESSAGE),
						RustUI.getString(Type.EDITOR_RESETSETTINGSDIALOG_TITLE),
						JOptionPane.YES_NO_OPTION
					);
					if (dialogResult == JOptionPane.YES_OPTION) {
						gui.InternalSettings.reset();
					}
				}
			);
			*/
		}
		
		{
			TabbedPane debugPane = createPane(tabbedPane, Type.EDITOR_TAB_DEBUGGING);
			addButtonField(debugPane, Type.DEBUG_OPENCONFIGDIRECTORY_LABEL, null, Type.DEBUG_OPENCONFIGDIRECTORY_BUTTON, e -> {
				UrlUtils.openDirectory(new File("").getAbsoluteFile());
			});
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
	
	public void openDialog(Point point) {
		// Update the fields to correctly show the settings.
		clickIntervalField.setText(Integer.toString(gui.SettingsClickInterval.get()));
		callbackIntervalField.setText(Integer.toString(gui.EditorCallbackInterval.get()));
		maxShapesField.setText(Integer.toString(gui.SettingsMaxShapes.get()));
		autosaveIntervalField.setText(Integer.toString(gui.SettingsAutosaveInterval.get()));
		alphaCombobox.setSelectedIndex(gui.SettingsAlpha.get());
		scalingCombobox.setSelectedIndex(gui.SettingsScaling.get());
		useIccConversionCombobox.setSelectedIndex(gui.SettingsUseICCConversion.get() ? 1 : 0);
		customSignDimension.setValue(gui.SettingsSignDimension.get());
		
		// Show the dialog.
		dialog.setLocation(point);
		dialog.setVisible(true);
		
		gui.SettingsAlpha.set(alphaCombobox.getSelectedIndex());
		gui.SettingsScaling.set(scalingCombobox.getSelectedIndex());
		
		try {
			gui.SettingsMaxShapes.set(maxShapesField.getNumberValue());
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid max shapes count '{}'", maxShapesField.getText());
			maxShapesField.setText(Integer.toString(gui.SettingsMaxShapes.get()));
		}
		
		try {
			gui.EditorCallbackInterval.set(callbackIntervalField.getNumberValue());
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid callback interval '{}'", callbackIntervalField.getText());
			callbackIntervalField.setText(Integer.toString(gui.EditorCallbackInterval.get()));
		}
		
		try {
			gui.SettingsClickInterval.set(clickIntervalField.getNumberValue());
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid click interval '{}'", clickIntervalField.getText());
			clickIntervalField.setText(Integer.toString(gui.SettingsClickInterval.get()));
		}
		
		try {
			gui.SettingsAutosaveInterval.set(autosaveIntervalField.getNumberValue());
		} catch (NumberFormatException e) {
			LOGGER.warn("Invalid autosave interval '{}'", autosaveIntervalField.getText());
			autosaveIntervalField.setText(Integer.toString(gui.SettingsAutosaveInterval.get()));
		}
		
		try {
			gui.SettingsUseICCConversion.set(useIccConversionCombobox.getSelectedIndex() == 1);
		} catch(NumberFormatException e) {
			LOGGER.warn("Invalid icc conversion interval '{}'", useIccConversionCombobox.getSelectedIndex());
			useIccConversionCombobox.setSelectedIndex(gui.SettingsUseICCConversion.get() ? 1 : 0);
		}
		
		try {
			gui.SettingsSignDimension.set(customSignDimension.getDimensionValue());
		} catch(NumberFormatException e) {
			LOGGER.warn("Invalid sign dimension '{}'", customSignDimension.getDimensionValue());
			customSignDimension.setValue(gui.SettingsSignDimension.get());
		}
		
		gui.SettingsSign.set(signPicker.getSelectedSign());
		gui.SettingsBackground.set(colorPicker.getSelectedColor());
	}
}
