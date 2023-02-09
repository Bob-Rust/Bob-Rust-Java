package com.bobrust.gui.dialog;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.io.File;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.gui.BobRustEditor;
import com.bobrust.gui.comp.JIntegerField;
import com.bobrust.lang.RustUI;
import com.bobrust.lang.RustUI.Type;
import com.bobrust.util.RustConstants;
import com.bobrust.util.UrlUtils;

public class BobRustSettingsDialog {
	private static final Logger LOGGER = LogManager.getLogger(BobRustSettingsDialog.class);
	private final BobRustEditor gui;
	private final BobRustSignPicker signPicker;
	private final BobRustColorPicker colorPicker;
	private final JDialog dialog;
	
	// TabbedPane
	final JTabbedPane tabbedPane;
	
	// Generator
	final JLabel lblBackgroundColor;
	final JButton btnBackgroundColor;
	final JLabel lblSignType;
	final JButton btnSignType;
	final JLabel lblAlphaIndex;
	final JComboBox<Integer> alphaCombobox;
	final JLabel lblScalingLabel;
	final JComboBox<String> scalingCombobox;
	final JLabel lblShapesLabel;
	final JIntegerField maxShapesField;
	final JLabel lblClickIntervalLabel;
	final JIntegerField clickIntervalField;
	final JLabel lblAutosaveIntervalLabel;
	final JIntegerField autosaveIntervalField;
	final JLabel lblUseIccConversionLabel;
	final JComboBox<String> useIccConversionCombobox;
	
	// Editor
	final JLabel lblBorderColor;
	final JButton btnBorderColor;
	final JLabel lblToolbarColor;
	final JButton btnToolbarColor;
	final JLabel lblLabelColor;
	final JButton btnLabelColor;
	final JLabel lblCallbackLabel;
	final JIntegerField callbackIntervalField;
	final JLabel lblResetEditor;
	final JButton btnResetEditor;
	
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
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.setFocusable(false);
		dialog.getContentPane().add(tabbedPane);
		
		{
			JPanel generatorPanel = new JPanel();
			generatorPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			generatorPanel.setOpaque(false);
			generatorPanel.setAlignmentY(Component.TOP_ALIGNMENT);
			tabbedPane.addTab(RustUI.getString(Type.EDITOR_TAB_GENERATOR), generatorPanel);
			generatorPanel.setFocusable(false);
			GridBagLayout gbl_generatorPanel = new GridBagLayout();
			gbl_generatorPanel.columnWidths = new int[]{140, 0, 0};
			gbl_generatorPanel.rowHeights = new int[] {20, 20, 20, 20, 20, 20, 20, 20, 20, };
			gbl_generatorPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_generatorPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
			generatorPanel.setLayout(gbl_generatorPanel);
			
			lblBackgroundColor = new JLabel(RustUI.getString(Type.SETTINGS_BACKGROUNDCOLOR_LABEL));
			GridBagConstraints gbc_lblBackgroundColor = new GridBagConstraints();
			gbc_lblBackgroundColor.anchor = GridBagConstraints.WEST;
			gbc_lblBackgroundColor.insets = new Insets(0, 0, 5, 5);
			gbc_lblBackgroundColor.gridx = 0;
			gbc_lblBackgroundColor.gridy = 0;
			generatorPanel.add(lblBackgroundColor, gbc_lblBackgroundColor);
			lblBackgroundColor.setToolTipText(RustUI.getString(Type.SETTINGS_BACKGROUNDCOLOR_TOOLTIP));
			
			btnBackgroundColor = new JButton(RustUI.getString(Type.SETTINGS_BACKGROUNDCOLOR_BUTTON));
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
			
			lblSignType = new JLabel(RustUI.getString(Type.SETTINGS_SIGNTYPE_LABEL));
			GridBagConstraints gbc_lblSignType = new GridBagConstraints();
			gbc_lblSignType.anchor = GridBagConstraints.WEST;
			gbc_lblSignType.insets = new Insets(0, 0, 5, 5);
			gbc_lblSignType.gridx = 0;
			gbc_lblSignType.gridy = 1;
			generatorPanel.add(lblSignType, gbc_lblSignType);
			lblSignType.setToolTipText(RustUI.getString(Type.SETTINGS_SIGNTYPE_TOOLTIP));
			lblSignType.setHorizontalTextPosition(SwingConstants.CENTER);
			lblSignType.setHorizontalAlignment(SwingConstants.CENTER);
			
			btnSignType = new JButton(RustUI.getString(Type.SETTINGS_SIGNTYPE_BUTTON));
			GridBagConstraints gbc_btnSign = new GridBagConstraints();
			gbc_btnSign.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnSign.insets = new Insets(0, 0, 5, 0);
			gbc_btnSign.gridx = 1;
			gbc_btnSign.gridy = 1;
			generatorPanel.add(btnSignType, gbc_btnSign);
			btnSignType.setPreferredSize(buttonSize);
			btnSignType.setMinimumSize(buttonSize);
			btnSignType.setMaximumSize(buttonSize);
			btnSignType.setFocusable(false);
			btnSignType.addActionListener((event) -> {
				Point dialogLocation = new Point(dialog.getLocationOnScreen());
				dialogLocation.x += 130;
				signPicker.openSignDialog(dialogLocation);
				gui.setSettingsSign(signPicker.getSelectedSign());;
			});
			
			lblAlphaIndex = new JLabel(RustUI.getString(Type.SETTINGS_ALPHAINDEX_LABEL));
			GridBagConstraints gbc_lblAlphaIndex = new GridBagConstraints();
			gbc_lblAlphaIndex.anchor = GridBagConstraints.WEST;
			gbc_lblAlphaIndex.insets = new Insets(0, 0, 5, 5);
			gbc_lblAlphaIndex.gridx = 0;
			gbc_lblAlphaIndex.gridy = 2;
			generatorPanel.add(lblAlphaIndex, gbc_lblAlphaIndex);
			lblAlphaIndex.setToolTipText(RustUI.getString(Type.SETTINGS_ALPHAINDEX_TOOLTIP));
			lblAlphaIndex.setHorizontalTextPosition(SwingConstants.CENTER);
			lblAlphaIndex.setHorizontalAlignment(SwingConstants.CENTER);
			
			alphaCombobox = new JComboBox<>();
			lblAlphaIndex.setLabelFor(alphaCombobox);
			GridBagConstraints gbc_alphaCombobox = new GridBagConstraints();
			gbc_alphaCombobox.fill = GridBagConstraints.HORIZONTAL;
			gbc_alphaCombobox.insets = new Insets(0, 0, 5, 0);
			gbc_alphaCombobox.gridx = 1;
			gbc_alphaCombobox.gridy = 2;
			generatorPanel.add(alphaCombobox, gbc_alphaCombobox);
			alphaCombobox.setAlignmentX(Component.LEFT_ALIGNMENT);
			alphaCombobox.setFocusable(false);
			alphaCombobox.setMaximumSize(new Dimension(116, 20));
			alphaCombobox.setModel(new DefaultComboBoxModel<>(new Integer[] { 0, 1, 2, 3, 4, 5 }));
			alphaCombobox.setSelectedIndex(gui.getSettingsAlpha());
			
			lblScalingLabel = new JLabel(RustUI.getString(Type.SETTINGS_SCALINGTYPE_LABEL));
			GridBagConstraints gbc_lblScalingLabel = new GridBagConstraints();
			gbc_lblScalingLabel.anchor = GridBagConstraints.WEST;
			gbc_lblScalingLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblScalingLabel.gridx = 0;
			gbc_lblScalingLabel.gridy = 3;
			generatorPanel.add(lblScalingLabel, gbc_lblScalingLabel);
			lblScalingLabel.setToolTipText(RustUI.getString(Type.SETTINGS_SCALINGTYPE_TOOLTIP));
			lblScalingLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			lblScalingLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			scalingCombobox = new JComboBox<>();
			lblScalingLabel.setLabelFor(scalingCombobox);
			GridBagConstraints gbc_scalingCombobox = new GridBagConstraints();
			gbc_scalingCombobox.fill = GridBagConstraints.HORIZONTAL;
			gbc_scalingCombobox.insets = new Insets(0, 0, 5, 0);
			gbc_scalingCombobox.gridx = 1;
			gbc_scalingCombobox.gridy = 3;
			generatorPanel.add(scalingCombobox, gbc_scalingCombobox);
			scalingCombobox.setAlignmentX(Component.LEFT_ALIGNMENT);
			scalingCombobox.setFocusable(false);
			scalingCombobox.setMaximumSize(new Dimension(116, 20));
			scalingCombobox.setModel(new DefaultComboBoxModel<>(new String[] { "Nearest", "Bilinear", "Bicubic" }));
			scalingCombobox.setSelectedIndex(gui.getSettingsScaling());
			
			lblShapesLabel = new JLabel(RustUI.getString(Type.SETTINGS_MAXSHAPES_LABEL));
			GridBagConstraints gbc_lblShapesLabel = new GridBagConstraints();
			gbc_lblShapesLabel.anchor = GridBagConstraints.WEST;
			gbc_lblShapesLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblShapesLabel.gridx = 0;
			gbc_lblShapesLabel.gridy = 4;
			generatorPanel.add(lblShapesLabel, gbc_lblShapesLabel);
			lblShapesLabel.setToolTipText(RustUI.getString(Type.SETTINGS_MAXSHAPES_TOOLTIP));
			lblShapesLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			lblShapesLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			maxShapesField = new JIntegerField(gui.getSettingsMaxShapes());
			lblShapesLabel.setLabelFor(maxShapesField);
			GridBagConstraints gbc_maxShapesField = new GridBagConstraints();
			gbc_maxShapesField.fill = GridBagConstraints.HORIZONTAL;
			gbc_maxShapesField.insets = new Insets(0, 0, 5, 0);
			gbc_maxShapesField.gridx = 1;
			gbc_maxShapesField.gridy = 4;
			generatorPanel.add(maxShapesField, gbc_maxShapesField);
			maxShapesField.setAlignmentX(Component.LEFT_ALIGNMENT);
			maxShapesField.setFocusable(true);
			maxShapesField.setMaximumSize(new Dimension(116, 20));
			
			lblClickIntervalLabel = new JLabel(RustUI.getString(Type.SETTINGS_CLICKINTERVAL_LABEL));
			GridBagConstraints gbc_lblClickIntervalLabel = new GridBagConstraints();
			gbc_lblClickIntervalLabel.anchor = GridBagConstraints.WEST;
			gbc_lblClickIntervalLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblClickIntervalLabel.gridx = 0;
			gbc_lblClickIntervalLabel.gridy = 5;
			generatorPanel.add(lblClickIntervalLabel, gbc_lblClickIntervalLabel);
			lblClickIntervalLabel.setToolTipText(RustUI.getString(Type.SETTINGS_CLICKINTERVAL_TOOLTIP));
			lblClickIntervalLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			lblClickIntervalLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			clickIntervalField = new JIntegerField(gui.getSettingsClickInterval());
			lblClickIntervalLabel.setLabelFor(clickIntervalField);
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
			
			lblAutosaveIntervalLabel = new JLabel(RustUI.getString(Type.SETTINGS_AUTOSAVEINTERVAL_LABEL));
			GridBagConstraints gbc_autosaveIntervalLabel = new GridBagConstraints();
			gbc_autosaveIntervalLabel.anchor = GridBagConstraints.WEST;
			gbc_autosaveIntervalLabel.insets = new Insets(0, 0, 5, 5);
			gbc_autosaveIntervalLabel.gridx = 0;
			gbc_autosaveIntervalLabel.gridy = 6;
			generatorPanel.add(lblAutosaveIntervalLabel, gbc_autosaveIntervalLabel);
			lblAutosaveIntervalLabel.setToolTipText(RustUI.getString(Type.SETTINGS_AUTOSAVEINTERVAL_TOOLTIP));
			lblAutosaveIntervalLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			lblAutosaveIntervalLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			autosaveIntervalField = new JIntegerField(gui.getSettingsAutosaveInterval());
			lblAutosaveIntervalLabel.setLabelFor(autosaveIntervalField);
			GridBagConstraints gbc_autosaveIntervalField = new GridBagConstraints();
			gbc_autosaveIntervalField.fill = GridBagConstraints.HORIZONTAL;
			gbc_autosaveIntervalField.insets = new Insets(0, 0, 5, 0);
			gbc_autosaveIntervalField.gridx = 1;
			gbc_autosaveIntervalField.gridy = 6;
			generatorPanel.add(autosaveIntervalField, gbc_autosaveIntervalField);
			autosaveIntervalField.setAlignmentX(Component.LEFT_ALIGNMENT);
			autosaveIntervalField.setFocusable(true);
			autosaveIntervalField.setMinimum(1);
			autosaveIntervalField.setMaximum(4000);
			autosaveIntervalField.setMaximumSize(new Dimension(116, 20));
			
			
			lblUseIccConversionLabel = new JLabel(RustUI.getString(Type.SETTINGS_USEICCCONVERSION_LABEL));
			GridBagConstraints gbc_useIccConversionLabel = new GridBagConstraints();
			gbc_useIccConversionLabel.anchor = GridBagConstraints.WEST;
			gbc_useIccConversionLabel.insets = new Insets(0, 0, 0, 5);
			gbc_useIccConversionLabel.gridx = 0;
			gbc_useIccConversionLabel.gridy = 7;
			generatorPanel.add(lblUseIccConversionLabel, gbc_useIccConversionLabel);
			lblUseIccConversionLabel.setToolTipText(RustUI.getString(Type.SETTINGS_USEICCCONVERSION_TOOLTIP));
			lblUseIccConversionLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			lblUseIccConversionLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			useIccConversionCombobox = new JComboBox<>();
			lblUseIccConversionLabel.setLabelFor(useIccConversionCombobox);
			GridBagConstraints gbc_useIccConversionField = new GridBagConstraints();
			gbc_useIccConversionField.fill = GridBagConstraints.HORIZONTAL;
			gbc_useIccConversionField.gridx = 1;
			gbc_useIccConversionField.gridy = 7;
			generatorPanel.add(useIccConversionCombobox, gbc_useIccConversionField);
			useIccConversionCombobox.setAlignmentX(Component.LEFT_ALIGNMENT);
			useIccConversionCombobox.setFocusable(true);
			useIccConversionCombobox.setMaximumSize(new Dimension(116, 20));
			useIccConversionCombobox.setModel(new DefaultComboBoxModel<>(new String[] { "Off", "On" }));
			useIccConversionCombobox.setSelectedIndex(gui.getSettingsUseICCConversion());
		}
		
		{
			JPanel editorPanel = new JPanel();
			editorPanel.setFocusable(false);
			editorPanel.setAlignmentY(Component.TOP_ALIGNMENT);
			editorPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			editorPanel.setOpaque(false);
			tabbedPane.addTab(RustUI.getString(Type.EDITOR_TAB_EDITOR), editorPanel);
			GridBagLayout gbl_editorPanel = new GridBagLayout();
			gbl_editorPanel.columnWidths = new int[] {140, 0, 0};
			gbl_editorPanel.rowHeights = new int[] {20, 20, 20, 20, 0, 0, 0};
			gbl_editorPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_editorPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0};
			editorPanel.setLayout(gbl_editorPanel);
			
			lblBorderColor = new JLabel(RustUI.getString(Type.EDITOR_BORDERCOLOR_LABEL));
			lblBorderColor.setToolTipText(RustUI.getString(Type.EDITOR_BORDERCOLOR_TOOLTIP));
			GridBagConstraints gbc_lblBorderColor = new GridBagConstraints();
			gbc_lblBorderColor.anchor = GridBagConstraints.WEST;
			gbc_lblBorderColor.insets = new Insets(0, 0, 5, 5);
			gbc_lblBorderColor.gridx = 0;
			gbc_lblBorderColor.gridy = 0;
			editorPanel.add(lblBorderColor, gbc_lblBorderColor);
			
			btnBorderColor = new JButton(RustUI.getString(Type.EDITOR_BORDERCOLOR_BUTTON));
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
			
			lblToolbarColor = new JLabel(RustUI.getString(Type.EDITOR_TOOLBARCOLOR_LABEL));
			GridBagConstraints gbc_lblToolbarColor = new GridBagConstraints();
			gbc_lblToolbarColor.anchor = GridBagConstraints.WEST;
			gbc_lblToolbarColor.insets = new Insets(0, 0, 5, 5);
			gbc_lblToolbarColor.gridx = 0;
			gbc_lblToolbarColor.gridy = 1;
			editorPanel.add(lblToolbarColor, gbc_lblToolbarColor);
			lblToolbarColor.setToolTipText(RustUI.getString(Type.EDITOR_TOOLBARCOLOR_TOOLTIP));
			
			btnToolbarColor = new JButton(RustUI.getString(Type.EDITOR_TOOLBARCOLOR_BUTTON));
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
			
			lblLabelColor = new JLabel(RustUI.getString(Type.EDITOR_LABELCOLOR_LABEL));
			GridBagConstraints gbc_lblLabelColor = new GridBagConstraints();
			gbc_lblLabelColor.anchor = GridBagConstraints.WEST;
			gbc_lblLabelColor.insets = new Insets(0, 0, 5, 5);
			gbc_lblLabelColor.gridx = 0;
			gbc_lblLabelColor.gridy = 2;
			editorPanel.add(lblLabelColor, gbc_lblLabelColor);
			lblLabelColor.setToolTipText(RustUI.getString(Type.EDITOR_LABELCOLOR_TOOLTIP));
			
			btnLabelColor = new JButton(RustUI.getString(Type.EDITOR_LABELCOLOR_BUTTON));
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
			
			lblCallbackLabel = new JLabel(RustUI.getString(Type.EDITOR_CALLBACKINTERVAL_LABEL));
			GridBagConstraints gbc_lblCallbackLabel = new GridBagConstraints();
			gbc_lblCallbackLabel.anchor = GridBagConstraints.WEST;
			gbc_lblCallbackLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblCallbackLabel.gridx = 0;
			gbc_lblCallbackLabel.gridy = 3;
			editorPanel.add(lblCallbackLabel, gbc_lblCallbackLabel);
			lblCallbackLabel.setToolTipText(RustUI.getString(Type.EDITOR_CALLBACKINTERVAL_TOOLTIP));
			lblCallbackLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			lblCallbackLabel.setHorizontalAlignment(SwingConstants.CENTER);
			
			callbackIntervalField = new JIntegerField(gui.getEditorCallbackInterval());
			lblCallbackLabel.setLabelFor(callbackIntervalField);
			GridBagConstraints gbc_callbackIntervalField = new GridBagConstraints();
			gbc_callbackIntervalField.fill = GridBagConstraints.HORIZONTAL;
			gbc_callbackIntervalField.insets = new Insets(0, 0, 5, 0);
			gbc_callbackIntervalField.gridx = 1;
			gbc_callbackIntervalField.gridy = 3;
			editorPanel.add(callbackIntervalField, gbc_callbackIntervalField);
			callbackIntervalField.setAlignmentX(Component.LEFT_ALIGNMENT);
			callbackIntervalField.setFocusable(true);
			callbackIntervalField.setMaximumSize(new Dimension(116, 20));
			
			lblResetEditor = new JLabel(RustUI.getString(Type.EDITOR_RESETEDITOR_LABEL));
			GridBagConstraints gbc_lblResetEditor = new GridBagConstraints();
			gbc_lblResetEditor.anchor = GridBagConstraints.WEST;
			gbc_lblResetEditor.insets = new Insets(0, 0, 5, 5);
			gbc_lblResetEditor.gridx = 0;
			gbc_lblResetEditor.gridy = 4;
			editorPanel.add(lblResetEditor, gbc_lblResetEditor);
			lblResetEditor.setToolTipText(RustUI.getString(Type.EDITOR_RESETEDITOR_TOOLTIP));
			
			btnResetEditor = new JButton(RustUI.getString(Type.EDITOR_RESETEDITOR_BUTTON));
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
					
					// Update fields.
					callbackIntervalField.setText(Integer.toString(gui.getEditorCallbackInterval()));
				}
			});
		}
		
		{
			JPanel debugPanel = new JPanel();
			debugPanel.setFocusable(false);
			debugPanel.setAlignmentY(Component.TOP_ALIGNMENT);
			debugPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			debugPanel.setOpaque(false);
			tabbedPane.addTab(RustUI.getString(Type.EDITOR_TAB_DEBUGGING), debugPanel);
			GridBagLayout gbl_editorPanel = new GridBagLayout();
			gbl_editorPanel.columnWidths = new int[] {140, 0, 0};
			gbl_editorPanel.rowHeights = new int[] {20, 20, 20, 20, 0, 0, 0};
			gbl_editorPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_editorPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0};
			debugPanel.setLayout(gbl_editorPanel);
			
			JLabel lblOpenConfigLabel = new JLabel(RustUI.getString(Type.DEBUG_OPENCONFIGDIRECTORY_LABEL));
			GridBagConstraints gbc_lblOpenConfigLabel = new GridBagConstraints();
			gbc_lblOpenConfigLabel.anchor = GridBagConstraints.WEST;
			gbc_lblOpenConfigLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblOpenConfigLabel.gridx = 0;
			gbc_lblOpenConfigLabel.gridy = 0;
			debugPanel.add(lblOpenConfigLabel, gbc_lblOpenConfigLabel);
			
			JButton btnOpenConfig = new JButton(RustUI.getString(Type.DEBUG_OPENCONFIGDIRECTORY_BUTTON));
			lblOpenConfigLabel.setLabelFor(btnOpenConfig);
			btnOpenConfig.addActionListener((event) -> {
				UrlUtils.openDirectory(new File("").getAbsoluteFile());
			});
			btnOpenConfig.setFocusable(false);
			GridBagConstraints gbc_btnOpenConfig = new GridBagConstraints();
			btnOpenConfig.setPreferredSize(buttonSize);
			btnOpenConfig.setMinimumSize(buttonSize);
			btnOpenConfig.setMaximumSize(buttonSize);
			gbc_btnOpenConfig.insets = new Insets(0, 0, 5, 0);
			gbc_btnOpenConfig.gridx = 1;
			gbc_btnOpenConfig.gridy = 0;
			debugPanel.add(btnOpenConfig, gbc_btnOpenConfig);
		}
	}
	
	/**
	 * This method will update the language of all elements in this component.
	 */
	public void updateLanguage() {
		LOGGER.warn("Runtime Language changes for the settings dialog is only partially supported!");
		dialog.setTitle(RustUI.getString(Type.EDITOR_SETTINGSDIALOG_TITLE));
		
		{
			tabbedPane.setTitleAt(0, RustUI.getString(Type.EDITOR_TAB_GENERATOR));
			lblBackgroundColor.setText(RustUI.getString(Type.SETTINGS_BACKGROUNDCOLOR_LABEL));
			lblBackgroundColor.setToolTipText(RustUI.getString(Type.SETTINGS_BACKGROUNDCOLOR_TOOLTIP));
			btnBackgroundColor.setText(RustUI.getString(Type.SETTINGS_BACKGROUNDCOLOR_BUTTON));
			lblSignType.setText(RustUI.getString(Type.SETTINGS_SIGNTYPE_LABEL));
			lblSignType.setToolTipText(RustUI.getString(Type.SETTINGS_SIGNTYPE_TOOLTIP));
			btnSignType.setText(RustUI.getString(Type.SETTINGS_SIGNTYPE_BUTTON));
			lblAlphaIndex.setText(RustUI.getString(Type.SETTINGS_ALPHAINDEX_LABEL));
			lblAlphaIndex.setToolTipText(RustUI.getString(Type.SETTINGS_ALPHAINDEX_TOOLTIP));
			lblScalingLabel.setText(RustUI.getString(Type.SETTINGS_SCALINGTYPE_LABEL));
			lblScalingLabel.setToolTipText(RustUI.getString(Type.SETTINGS_SCALINGTYPE_TOOLTIP));
			lblShapesLabel.setText(RustUI.getString(Type.SETTINGS_MAXSHAPES_LABEL));
			lblShapesLabel.setToolTipText(RustUI.getString(Type.SETTINGS_MAXSHAPES_TOOLTIP));
			lblClickIntervalLabel.setText(RustUI.getString(Type.SETTINGS_CLICKINTERVAL_LABEL));
			lblClickIntervalLabel.setToolTipText(RustUI.getString(Type.SETTINGS_CLICKINTERVAL_TOOLTIP));
			lblAutosaveIntervalLabel.setText(RustUI.getString(Type.SETTINGS_AUTOSAVEINTERVAL_LABEL));
			lblAutosaveIntervalLabel.setToolTipText(RustUI.getString(Type.SETTINGS_AUTOSAVEINTERVAL_TOOLTIP));
			lblUseIccConversionLabel.setText(RustUI.getString(Type.SETTINGS_AUTOSAVEINTERVAL_LABEL));
			lblUseIccConversionLabel.setToolTipText(RustUI.getString(Type.SETTINGS_AUTOSAVEINTERVAL_TOOLTIP));
		}
		
		{
			tabbedPane.setTitleAt(1, RustUI.getString(Type.EDITOR_TAB_EDITOR));
			lblBorderColor.setText(RustUI.getString(Type.EDITOR_BORDERCOLOR_LABEL));
			lblBorderColor.setToolTipText(RustUI.getString(Type.EDITOR_BORDERCOLOR_TOOLTIP));
			btnBorderColor.setText(RustUI.getString(Type.EDITOR_BORDERCOLOR_BUTTON));
			lblToolbarColor.setText(RustUI.getString(Type.EDITOR_TOOLBARCOLOR_LABEL));
			lblToolbarColor.setToolTipText(RustUI.getString(Type.EDITOR_TOOLBARCOLOR_TOOLTIP));
			btnToolbarColor.setText(RustUI.getString(Type.EDITOR_TOOLBARCOLOR_BUTTON));
			lblLabelColor.setText(RustUI.getString(Type.EDITOR_LABELCOLOR_LABEL));
			lblLabelColor.setToolTipText(RustUI.getString(Type.EDITOR_LABELCOLOR_TOOLTIP));
			btnLabelColor.setText(RustUI.getString(Type.EDITOR_LABELCOLOR_BUTTON));
			lblCallbackLabel.setText(RustUI.getString(Type.EDITOR_CALLBACKINTERVAL_LABEL));
			lblCallbackLabel.setToolTipText(RustUI.getString(Type.EDITOR_CALLBACKINTERVAL_TOOLTIP));
			lblResetEditor.setText(RustUI.getString(Type.EDITOR_RESETEDITOR_LABEL));
			lblResetEditor.setToolTipText(RustUI.getString(Type.EDITOR_RESETEDITOR_TOOLTIP));
			btnResetEditor.setText(RustUI.getString(Type.EDITOR_RESETEDITOR_BUTTON));
		}
		
		{
			tabbedPane.setTitleAt(2, RustUI.getString(Type.EDITOR_TAB_DEBUGGING));
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
		useIccConversionCombobox.setSelectedIndex(gui.getSettingsUseICCConversion());
		
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
		
		try {
			gui.setSettingsUseICCConversion(useIccConversionCombobox.getSelectedIndex());
		} catch(NumberFormatException e) {
			LOGGER.warn("Invalid icc conversion interval '{}'", useIccConversionCombobox.getSelectedIndex());
			useIccConversionCombobox.setSelectedIndex(gui.getSettingsUseICCConversion());
		}
		
		gui.setSettingsSign(signPicker.getSelectedSign());
		gui.setSettingsBackground(colorPicker.getSelectedColor());
	}
}
