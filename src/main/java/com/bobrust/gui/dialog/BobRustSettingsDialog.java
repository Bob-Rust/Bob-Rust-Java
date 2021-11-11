package com.bobrust.gui.dialog;

import java.awt.*;
import java.awt.Dialog.ModalityType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.gui.BobRustEditor;
import com.bobrust.gui.comp.JIntegerField;
import com.bobrust.lang.RustUI;
import com.bobrust.lang.RustUI.Type;
import com.bobrust.util.RustConstants;

public class BobRustSettingsDialog {
	private static final Logger LOGGER = LogManager.getLogger(BobRustSettingsDialog.class);
	private final BobRustEditor gui;
	private final BobRustSignPicker signPicker;
	private final BobRustColorPicker colorPicker;
	private final JDialog dialog;
	
	private final JComboBox<Integer> alphaCombobox;
	private final JComboBox<String> scalingCombobox;
	private final JIntegerField maxShapesField;
	private final JIntegerField callbackIntervalField;
	private final JIntegerField clickIntervalField;
	private final JIntegerField autosaveIntervalField;
	
	public BobRustSettingsDialog(BobRustEditor gui, JDialog parent) {
		this.gui = gui;
		this.dialog = new JDialog(parent, RustUI.getString(Type.EDITOR_SETTINGSDIALOG_TITLE), ModalityType.APPLICATION_MODAL);
		this.dialog.setIconImage(RustConstants.DIALOG_ICON);
		this.dialog.setSize(300, 360);
		this.dialog.setResizable(false);
		this.signPicker = new BobRustSignPicker(gui, dialog);
		this.colorPicker = new BobRustColorPicker(gui, dialog);
		dialog.getContentPane().setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.X_AXIS));
		
		Dimension buttonSize = new Dimension(120, 23);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.setFocusable(false);
		dialog.getContentPane().add(tabbedPane);
		
		{
			JPanel generatorPanel = new JPanel();
			generatorPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			generatorPanel.setOpaque(false);
			generatorPanel.setAlignmentY(Component.TOP_ALIGNMENT);
			tabbedPane.addTab("Generator", generatorPanel);
			generatorPanel.setFocusable(false);
			GridBagLayout gbl_generatorPanel = new GridBagLayout();
			gbl_generatorPanel.columnWidths = new int[]{140, 0, 0};
			gbl_generatorPanel.rowHeights = new int[] {20, 20, 20, 20, 20, 20, 20, 20, 0};
			gbl_generatorPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_generatorPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			generatorPanel.setLayout(gbl_generatorPanel);
			
			JLabel lblBackgroundColor = new JLabel(RustUI.getString(Type.SETTINGS_BACKGROUNDCOLOR_LABEL));
			GridBagConstraints gbc_lblBackgroundColor = new GridBagConstraints();
			gbc_lblBackgroundColor.anchor = GridBagConstraints.WEST;
			gbc_lblBackgroundColor.insets = new Insets(0, 0, 5, 5);
			gbc_lblBackgroundColor.gridx = 0;
			gbc_lblBackgroundColor.gridy = 0;
			generatorPanel.add(lblBackgroundColor, gbc_lblBackgroundColor);
			lblBackgroundColor.setToolTipText(RustUI.getString(Type.SETTINGS_BACKGROUNDCOLOR_TOOLTIP));
			
			JButton btnBackgroundColor = new JButton(RustUI.getString(Type.SETTINGS_BACKGROUNDCOLOR_BUTTON));
			lblBackgroundColor.setLabelFor(btnBackgroundColor);
			GridBagConstraints gbc_btnBackgroundColor = new GridBagConstraints();
			gbc_btnBackgroundColor.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnBackgroundColor.insets = new Insets(0, 0, 5, 0);
			gbc_btnBackgroundColor.gridx = 1;
			gbc_btnBackgroundColor.gridy = 0;
			generatorPanel.add(btnBackgroundColor, gbc_btnBackgroundColor);
			btnBackgroundColor.setPreferredSize(buttonSize);
			btnBackgroundColor.setMinimumSize(buttonSize);
			btnBackgroundColor.setMaximumSize(buttonSize);
			btnBackgroundColor.setFocusable(false);
			btnBackgroundColor.addActionListener((event) -> {
				Point dialogLocation = new Point(dialog.getLocationOnScreen());
				dialogLocation.x += 130;
				colorPicker.openColorDialog(dialogLocation);
			});
			
			JLabel signLabel = new JLabel(RustUI.getString(Type.SETTINGS_SIGNTYPE_LABEL));
			GridBagConstraints gbc_signLabel = new GridBagConstraints();
			gbc_signLabel.anchor = GridBagConstraints.WEST;
			gbc_signLabel.insets = new Insets(0, 0, 5, 5);
			gbc_signLabel.gridx = 0;
			gbc_signLabel.gridy = 1;
			generatorPanel.add(signLabel, gbc_signLabel);
			signLabel.setToolTipText(RustUI.getString(Type.SETTINGS_SIGNTYPE_TOOLTIP));
			signLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			signLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			final JButton btnSign = new JButton(RustUI.getString(Type.SETTINGS_SIGNTYPE_BUTTON));
			GridBagConstraints gbc_btnSign = new GridBagConstraints();
			gbc_btnSign.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnSign.insets = new Insets(0, 0, 5, 0);
			gbc_btnSign.gridx = 1;
			gbc_btnSign.gridy = 1;
			generatorPanel.add(btnSign, gbc_btnSign);
			btnSign.setPreferredSize(buttonSize);
			btnSign.setMinimumSize(buttonSize);
			btnSign.setMaximumSize(buttonSize);
			btnSign.setFocusable(false);
			btnSign.addActionListener((event) -> {
				Point dialogLocation = new Point(dialog.getLocationOnScreen());
				dialogLocation.x += 130;
				signPicker.openSignDialog(dialogLocation);
				gui.setSettingsSign(signPicker.getSelectedSign());;
			});
			
			JLabel alphaLabel = new JLabel(RustUI.getString(Type.SETTINGS_ALPHAINDEX_LABEL));
			GridBagConstraints gbc_alphaLabel = new GridBagConstraints();
			gbc_alphaLabel.anchor = GridBagConstraints.WEST;
			gbc_alphaLabel.insets = new Insets(0, 0, 5, 5);
			gbc_alphaLabel.gridx = 0;
			gbc_alphaLabel.gridy = 2;
			generatorPanel.add(alphaLabel, gbc_alphaLabel);
			alphaLabel.setToolTipText(RustUI.getString(Type.SETTINGS_ALPHAINDEX_TOOLTIP));
			alphaLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			alphaLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			alphaCombobox = new JComboBox<Integer>();
			alphaLabel.setLabelFor(alphaCombobox);
			GridBagConstraints gbc_alphaCombobox = new GridBagConstraints();
			gbc_alphaCombobox.fill = GridBagConstraints.HORIZONTAL;
			gbc_alphaCombobox.insets = new Insets(0, 0, 5, 0);
			gbc_alphaCombobox.gridx = 1;
			gbc_alphaCombobox.gridy = 2;
			generatorPanel.add(alphaCombobox, gbc_alphaCombobox);
			alphaCombobox.setAlignmentX(Component.LEFT_ALIGNMENT);
			alphaCombobox.setFocusable(false);
			alphaCombobox.setMaximumSize(new Dimension(116, 20));
			alphaCombobox.setModel(new DefaultComboBoxModel<Integer>(new Integer[] { 0, 1, 2, 3, 4, 5 }));
			alphaCombobox.setSelectedIndex(gui.getSettingsAlpha());
			
			JLabel scalingLabel = new JLabel(RustUI.getString(Type.SETTINGS_SCALINGTYPE_LABEL));
			GridBagConstraints gbc_scalingLabel = new GridBagConstraints();
			gbc_scalingLabel.anchor = GridBagConstraints.WEST;
			gbc_scalingLabel.insets = new Insets(0, 0, 5, 5);
			gbc_scalingLabel.gridx = 0;
			gbc_scalingLabel.gridy = 3;
			generatorPanel.add(scalingLabel, gbc_scalingLabel);
			scalingLabel.setToolTipText(RustUI.getString(Type.SETTINGS_SCALINGTYPE_TOOLTIP));
			scalingLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			scalingLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			scalingCombobox = new JComboBox<String>();
			scalingLabel.setLabelFor(scalingCombobox);
			GridBagConstraints gbc_scalingCombobox = new GridBagConstraints();
			gbc_scalingCombobox.fill = GridBagConstraints.HORIZONTAL;
			gbc_scalingCombobox.insets = new Insets(0, 0, 5, 0);
			gbc_scalingCombobox.gridx = 1;
			gbc_scalingCombobox.gridy = 3;
			generatorPanel.add(scalingCombobox, gbc_scalingCombobox);
			scalingCombobox.setAlignmentX(Component.LEFT_ALIGNMENT);
			scalingCombobox.setFocusable(false);
			scalingCombobox.setMaximumSize(new Dimension(116, 20));
			scalingCombobox.setModel(new DefaultComboBoxModel<String>(new String[] { "Nearest", "Bilinear", "Bicubic" }));
			scalingCombobox.setSelectedIndex(gui.getSettingsScaling());
			
			JLabel shapesLabel = new JLabel(RustUI.getString(Type.SETTINGS_MAXSHAPES_LABEL));
			GridBagConstraints gbc_shapesLabel = new GridBagConstraints();
			gbc_shapesLabel.anchor = GridBagConstraints.WEST;
			gbc_shapesLabel.insets = new Insets(0, 0, 5, 5);
			gbc_shapesLabel.gridx = 0;
			gbc_shapesLabel.gridy = 4;
			generatorPanel.add(shapesLabel, gbc_shapesLabel);
			shapesLabel.setToolTipText(RustUI.getString(Type.SETTINGS_MAXSHAPES_TOOLTIP));
			shapesLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			shapesLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			maxShapesField = new JIntegerField(gui.getSettingsMaxShapes());
			shapesLabel.setLabelFor(maxShapesField);
			GridBagConstraints gbc_maxShapesField = new GridBagConstraints();
			gbc_maxShapesField.fill = GridBagConstraints.HORIZONTAL;
			gbc_maxShapesField.insets = new Insets(0, 0, 5, 0);
			gbc_maxShapesField.gridx = 1;
			gbc_maxShapesField.gridy = 4;
			generatorPanel.add(maxShapesField, gbc_maxShapesField);
			maxShapesField.setAlignmentX(Component.LEFT_ALIGNMENT);
			maxShapesField.setFocusable(true);
			maxShapesField.setMaximumSize(new Dimension(116, 20));
			
			JLabel clickIntervalLabel = new JLabel(RustUI.getString(Type.SETTINGS_CLICKINTERVAL_LABEL));
			GridBagConstraints gbc_clickIntervalLabel = new GridBagConstraints();
			gbc_clickIntervalLabel.anchor = GridBagConstraints.WEST;
			gbc_clickIntervalLabel.insets = new Insets(0, 0, 5, 5);
			gbc_clickIntervalLabel.gridx = 0;
			gbc_clickIntervalLabel.gridy = 5;
			generatorPanel.add(clickIntervalLabel, gbc_clickIntervalLabel);
			clickIntervalLabel.setToolTipText(RustUI.getString(Type.SETTINGS_CLICKINTERVAL_TOOLTIP));
			clickIntervalLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			clickIntervalLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			clickIntervalField = new JIntegerField(gui.getSettingsClickInterval());
			clickIntervalLabel.setLabelFor(clickIntervalField);
			GridBagConstraints gbc_clickIntervalField = new GridBagConstraints();
			gbc_clickIntervalField.fill = GridBagConstraints.HORIZONTAL;
			gbc_clickIntervalField.insets = new Insets(0, 0, 5, 0);
			gbc_clickIntervalField.gridx = 1;
			gbc_clickIntervalField.gridy = 5;
			generatorPanel.add(clickIntervalField, gbc_clickIntervalField);
			clickIntervalField.setAlignmentX(Component.LEFT_ALIGNMENT);
			clickIntervalField.setFocusable(true);
			clickIntervalField.setMinimum(1);
			clickIntervalField.setMaximum(60);
			clickIntervalField.setMaximumSize(new Dimension(116, 20));
			
			JLabel autosaveIntervalLabel = new JLabel(RustUI.getString(Type.SETTINGS_AUTOSAVEINTERVAL_LABEL));
			GridBagConstraints gbc_autosaveIntervalLabel = new GridBagConstraints();
			gbc_autosaveIntervalLabel.anchor = GridBagConstraints.WEST;
			gbc_autosaveIntervalLabel.insets = new Insets(0, 0, 0, 5);
			gbc_autosaveIntervalLabel.gridx = 0;
			gbc_autosaveIntervalLabel.gridy = 6;
			generatorPanel.add(autosaveIntervalLabel, gbc_autosaveIntervalLabel);
			autosaveIntervalLabel.setToolTipText(RustUI.getString(Type.SETTINGS_AUTOSAVEINTERVAL_TOOLTIP));
			autosaveIntervalLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			autosaveIntervalLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			autosaveIntervalField = new JIntegerField(gui.getSettingsAutosaveInterval());
			autosaveIntervalLabel.setLabelFor(autosaveIntervalField);
			GridBagConstraints gbc_autosaveIntervalField = new GridBagConstraints();
			gbc_autosaveIntervalField.fill = GridBagConstraints.HORIZONTAL;
			gbc_autosaveIntervalField.gridx = 1;
			gbc_autosaveIntervalField.gridy = 6;
			generatorPanel.add(autosaveIntervalField, gbc_autosaveIntervalField);
			autosaveIntervalField.setAlignmentX(Component.LEFT_ALIGNMENT);
			autosaveIntervalField.setFocusable(true);
			autosaveIntervalField.setMinimum(1);
			autosaveIntervalField.setMaximum(4000);
			autosaveIntervalField.setMaximumSize(new Dimension(116, 20));
		}
		
		{
			JPanel editorPanel = new JPanel();
			editorPanel.setFocusable(false);
			editorPanel.setAlignmentY(Component.TOP_ALIGNMENT);
			editorPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			editorPanel.setOpaque(false);
			tabbedPane.addTab("Editor", editorPanel);
			GridBagLayout gbl_editorPanel = new GridBagLayout();
			gbl_editorPanel.columnWidths = new int[] {140, 0, 0};
			gbl_editorPanel.rowHeights = new int[] {20, 20, 20, 20, 0, 0, 0};
			gbl_editorPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_editorPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0};
			editorPanel.setLayout(gbl_editorPanel);
			
			JLabel lblBorderColor = new JLabel(RustUI.getString(Type.EDITOR_BORDERCOLOR_LABEL));
			lblBorderColor.setToolTipText(RustUI.getString(Type.EDITOR_BORDERCOLOR_TOOLTIP));
			GridBagConstraints gbc_lblBorderColor = new GridBagConstraints();
			gbc_lblBorderColor.anchor = GridBagConstraints.WEST;
			gbc_lblBorderColor.insets = new Insets(0, 0, 5, 5);
			gbc_lblBorderColor.gridx = 0;
			gbc_lblBorderColor.gridy = 0;
			editorPanel.add(lblBorderColor, gbc_lblBorderColor);
			
			JButton btnBorderColor = new JButton(RustUI.getString(Type.EDITOR_BORDERCOLOR_BUTTON));
			lblBorderColor.setLabelFor(btnBorderColor);
			btnBorderColor.setPreferredSize(buttonSize);
			btnBorderColor.setMinimumSize(buttonSize);
			btnBorderColor.setMaximumSize(buttonSize);
			btnBorderColor.setFocusable(false);
			btnBorderColor.addActionListener((event) -> {
				Color color = JColorChooser.showDialog(dialog, RustUI.getString(Type.EDITOR_COLORDIALOG_TITLE), gui.getEditorBorderColor(), false);
				if(color != null) {
					gui.setEditorBorderColor(color);
				}
			});
			GridBagConstraints gbc_btnBorderColor = new GridBagConstraints();
			gbc_btnBorderColor.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnBorderColor.insets = new Insets(0, 0, 5, 0);
			gbc_btnBorderColor.gridx = 1;
			gbc_btnBorderColor.gridy = 0;
			editorPanel.add(btnBorderColor, gbc_btnBorderColor);
			
			JLabel lblToolbarColor = new JLabel(RustUI.getString(Type.EDITOR_TOOLBARCOLOR_LABEL));
			GridBagConstraints gbc_lblToolbarColor = new GridBagConstraints();
			gbc_lblToolbarColor.anchor = GridBagConstraints.WEST;
			gbc_lblToolbarColor.insets = new Insets(0, 0, 5, 5);
			gbc_lblToolbarColor.gridx = 0;
			gbc_lblToolbarColor.gridy = 1;
			editorPanel.add(lblToolbarColor, gbc_lblToolbarColor);
			lblToolbarColor.setToolTipText(RustUI.getString(Type.EDITOR_TOOLBARCOLOR_TOOLTIP));
			
			JButton btnToolbarColor = new JButton(RustUI.getString(Type.EDITOR_TOOLBARCOLOR_BUTTON));
			lblToolbarColor.setLabelFor(btnToolbarColor);
			GridBagConstraints gbc_btnToolbarColor = new GridBagConstraints();
			gbc_btnToolbarColor.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnToolbarColor.insets = new Insets(0, 0, 5, 0);
			gbc_btnToolbarColor.gridx = 1;
			gbc_btnToolbarColor.gridy = 1;
			editorPanel.add(btnToolbarColor, gbc_btnToolbarColor);
			btnToolbarColor.setPreferredSize(buttonSize);
			btnToolbarColor.setMinimumSize(buttonSize);
			btnToolbarColor.setMaximumSize(buttonSize);
			btnToolbarColor.setFocusable(false);
			btnToolbarColor.addActionListener((event) -> {
				Color color = JColorChooser.showDialog(dialog, RustUI.getString(Type.EDITOR_COLORDIALOG_TITLE), gui.getEditorToolbarColor(), false);
				if(color != null) {
					gui.setEditorToolbarColor(color);
				}
			});
			
			JLabel lblLabelColor = new JLabel(RustUI.getString(Type.EDITOR_LABELCOLOR_LABEL));
			GridBagConstraints gbc_lblLabelColor = new GridBagConstraints();
			gbc_lblLabelColor.anchor = GridBagConstraints.WEST;
			gbc_lblLabelColor.insets = new Insets(0, 0, 5, 5);
			gbc_lblLabelColor.gridx = 0;
			gbc_lblLabelColor.gridy = 2;
			editorPanel.add(lblLabelColor, gbc_lblLabelColor);
			lblLabelColor.setToolTipText(RustUI.getString(Type.EDITOR_LABELCOLOR_TOOLTIP));
			
			JButton btnLabelColor = new JButton(RustUI.getString(Type.EDITOR_LABELCOLOR_BUTTON));
			GridBagConstraints gbc_btnLabelColor = new GridBagConstraints();
			gbc_btnLabelColor.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnLabelColor.insets = new Insets(0, 0, 5, 0);
			gbc_btnLabelColor.gridx = 1;
			gbc_btnLabelColor.gridy = 2;
			editorPanel.add(btnLabelColor, gbc_btnLabelColor);
			btnLabelColor.setPreferredSize(buttonSize);
			btnLabelColor.setMinimumSize(buttonSize);
			btnLabelColor.setMaximumSize(buttonSize);
			btnLabelColor.setFocusable(false);
			btnLabelColor.addActionListener((event) -> {
				Color color = JColorChooser.showDialog(dialog, RustUI.getString(Type.EDITOR_COLORDIALOG_TITLE), gui.getEditorLabelColor(), false);
				if(color != null) {
					gui.setEditorLabelColor(color);
				}
			});
			
			JLabel callbackLabel = new JLabel(RustUI.getString(Type.EDITOR_CALLBACKINTERVAL_LABEL));
			GridBagConstraints gbc_callbackLabel = new GridBagConstraints();
			gbc_callbackLabel.anchor = GridBagConstraints.WEST;
			gbc_callbackLabel.insets = new Insets(0, 0, 5, 5);
			gbc_callbackLabel.gridx = 0;
			gbc_callbackLabel.gridy = 3;
			editorPanel.add(callbackLabel, gbc_callbackLabel);
			callbackLabel.setToolTipText(RustUI.getString(Type.EDITOR_CALLBACKINTERVAL_TOOLTIP));
			callbackLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			callbackLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			callbackIntervalField = new JIntegerField(gui.getEditorCallbackInterval());
			callbackLabel.setLabelFor(callbackIntervalField);
			GridBagConstraints gbc_callbackIntervalField = new GridBagConstraints();
			gbc_callbackIntervalField.fill = GridBagConstraints.HORIZONTAL;
			gbc_callbackIntervalField.insets = new Insets(0, 0, 5, 0);
			gbc_callbackIntervalField.gridx = 1;
			gbc_callbackIntervalField.gridy = 3;
			editorPanel.add(callbackIntervalField, gbc_callbackIntervalField);
			callbackIntervalField.setAlignmentX(Component.LEFT_ALIGNMENT);
			callbackIntervalField.setFocusable(true);
			callbackIntervalField.setMaximumSize(new Dimension(116, 20));
			
			JLabel lblResetEditor = new JLabel(RustUI.getString(Type.EDITOR_RESETEDITOR_LABEL));
			GridBagConstraints gbc_lblResetEditor = new GridBagConstraints();
			gbc_lblResetEditor.anchor = GridBagConstraints.WEST;
			gbc_lblResetEditor.insets = new Insets(0, 0, 5, 5);
			gbc_lblResetEditor.gridx = 0;
			gbc_lblResetEditor.gridy = 4;
			editorPanel.add(lblResetEditor, gbc_lblResetEditor);
			lblResetEditor.setToolTipText(RustUI.getString(Type.EDITOR_RESETEDITOR_TOOLTIP));
			
			JButton btnResetEditor = new JButton(RustUI.getString(Type.EDITOR_RESETEDITOR_BUTTON));
			GridBagConstraints gbc_btnResetEditor = new GridBagConstraints();
			gbc_btnResetEditor.insets = new Insets(0, 0, 5, 0);
			gbc_btnResetEditor.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnResetEditor.gridx = 1;
			gbc_btnResetEditor.gridy = 4;
			editorPanel.add(btnResetEditor, gbc_btnResetEditor);
			btnResetEditor.setPreferredSize(buttonSize);
			btnResetEditor.setMinimumSize(buttonSize);
			btnResetEditor.setMaximumSize(buttonSize);
			btnResetEditor.setFocusable(false);
			btnResetEditor.addActionListener((event) -> {
				int dialogResult = JOptionPane.showConfirmDialog(dialog,
					RustUI.getString(Type.EDITOR_RESETEDITORDIALOG_MESSAGE),
					RustUI.getString(Type.EDITOR_RESETEDITORDIALOG_TITLE),
					JOptionPane.YES_NO_OPTION
				);
				if(dialogResult == JOptionPane.YES_OPTION) {
					gui.setEditorBorderColor(null);
					gui.setEditorToolbarColor(null);
					gui.setEditorLabelColor(null);
					gui.setEditorCallbackInterval(null);
				}
			});
		}
	}
	
	public void openDialog(Point point) {
		// Update the fields to correctly show the settings.
		clickIntervalField.setText(Integer.toString(gui.getSettingsClickInterval()));
		callbackIntervalField.setText(Integer.toString(gui.getEditorCallbackInterval()));
		maxShapesField.setText(Integer.toString(gui.getSettingsMaxShapes()));
		autosaveIntervalField.setText(Integer.toString(gui.getSettingsAutosaveInterval()));
		alphaCombobox.setSelectedIndex(gui.getSettingsAlpha());
		scalingCombobox.setSelectedIndex(gui.getSettingsScaling());
		
		// Show the dialog.
		dialog.setLocation(point);
		dialog.setVisible(true);
		
		gui.setSettingsAlpha(alphaCombobox.getSelectedIndex());
		gui.setSettingsScaling(scalingCombobox.getSelectedIndex());
		
		try {
			gui.setSettingsMaxShapes(maxShapesField.getNumberValue());
		} catch(NumberFormatException e) {
			LOGGER.warn("Invalid max shapes count '{}'", maxShapesField.getText());
			maxShapesField.setText(Integer.toString(gui.getSettingsMaxShapes()));
		}
		
		try {
			gui.setEditorCallbackInterval(callbackIntervalField.getNumberValue());
		} catch(NumberFormatException e) {
			LOGGER.warn("Invalid callback interval '{}'", callbackIntervalField.getText());
			callbackIntervalField.setText(Integer.toString(gui.getEditorCallbackInterval()));
		}
		
		try {
			gui.setSettingsClickInterval(clickIntervalField.getNumberValue());
		} catch(NumberFormatException e) {
			LOGGER.warn("Invalid click interval '{}'", clickIntervalField.getText());
			clickIntervalField.setText(Integer.toString(gui.getSettingsClickInterval()));
		}
		
		try {
			gui.setSettingsAutosaveInterval(autosaveIntervalField.getNumberValue());
		} catch(NumberFormatException e) {
			LOGGER.warn("Invalid autosave interval '{}'", autosaveIntervalField.getText());
			autosaveIntervalField.setText(Integer.toString(gui.getSettingsAutosaveInterval()));
		}
		
		gui.setSettingsSign(signPicker.getSelectedSign());
		gui.setSettingsBackground(colorPicker.getSelectedColor());
	}
}
