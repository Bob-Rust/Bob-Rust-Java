package com.bobrust.gui;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;

import com.bobrust.logging.LogUtils;
import javax.swing.border.TitledBorder;

public class BobRustSettings {
	private final BobRustEditor gui;
	private final BobRustSignPicker signPicker;
	private final BobRustColorPicker colorPicker;
	private final JDialog dialog;
	
	private final JComboBox<Integer> alphaCombobox;
	private final JFormattedTextField maxShapesTextField;
	private final JFormattedTextField callbackIntervalTextField;
	
	public BobRustSettings(BobRustEditor gui, JDialog parent) {
		this.gui = gui;
		this.dialog = new JDialog(parent, "Settings", ModalityType.APPLICATION_MODAL);
		this.signPicker = new BobRustSignPicker(gui, dialog);
		this.colorPicker = new BobRustColorPicker(gui, dialog);
		dialog.getContentPane().setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.X_AXIS));
		
		Dimension optionSize = new Dimension(140, 50);
		Dimension buttonSize = new Dimension(120, 23);
		
		JPanel panel = new JPanel();
		panel.setAlignmentY(Component.TOP_ALIGNMENT);
		panel.setBorder(new TitledBorder(null, "Generator", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		dialog.getContentPane().add(panel);
		panel.setFocusable(true);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JPanel backgroundPanel = new JPanel();
		backgroundPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		backgroundPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
		backgroundPanel.setPreferredSize(optionSize);
		backgroundPanel.setMinimumSize(optionSize);
		backgroundPanel.setMaximumSize(optionSize);
		panel.add(backgroundPanel);
		backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));
		
		JLabel lblBackgroundColor = new JLabel("Background Color");
		lblBackgroundColor.setToolTipText("The background color of the canvas");
		backgroundPanel.add(lblBackgroundColor);
		
		JButton btnBackgroundColor = new JButton("Background Color");
		btnBackgroundColor.setPreferredSize(buttonSize);
		btnBackgroundColor.setMinimumSize(buttonSize);
		btnBackgroundColor.setMaximumSize(buttonSize);
		btnBackgroundColor.setFocusable(false);
		lblBackgroundColor.setLabelFor(btnBackgroundColor);
		btnBackgroundColor.addActionListener((event) -> {
			Point dialogLocation = new Point(dialog.getLocationOnScreen());
			dialogLocation.x += 130;
			colorPicker.openColorDialog(dialogLocation);
		});
		backgroundPanel.add(btnBackgroundColor);
		
		JPanel signPanel = new JPanel();
		signPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		signPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
		signPanel.setPreferredSize(optionSize);
		signPanel.setMinimumSize(optionSize);
		signPanel.setMaximumSize(optionSize);
		panel.add(signPanel);
		signPanel.setLayout(new BoxLayout(signPanel, BoxLayout.Y_AXIS));
		
		JLabel signLabel = new JLabel("Sign Type");
		signLabel.setToolTipText("The type of sign to use when drawing");
		signLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		signLabel.setHorizontalAlignment(SwingConstants.CENTER);
		signPanel.add(signLabel);
		
		final JButton btnSign = new JButton("Select Sign Type");
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
		signPanel.add(btnSign);
		
		JPanel alphaPanel = new JPanel();
		alphaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		alphaPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
		alphaPanel.setPreferredSize(optionSize);
		alphaPanel.setMinimumSize(optionSize);
		alphaPanel.setMaximumSize(optionSize);
		panel.add(alphaPanel);
		alphaPanel.setLayout(new BoxLayout(alphaPanel, BoxLayout.Y_AXIS));
		
		JLabel alphaLabel = new JLabel("Alpha index");
		alphaLabel.setToolTipText("The alpha value used to draw the image");
		alphaLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		alphaLabel.setHorizontalAlignment(SwingConstants.CENTER);
		alphaPanel.add(alphaLabel);
		
		alphaCombobox = new JComboBox<Integer>();
		alphaCombobox.setAlignmentX(Component.LEFT_ALIGNMENT);
		alphaCombobox.setFocusable(false);
		alphaLabel.setLabelFor(alphaCombobox);
		alphaCombobox.setMaximumSize(new Dimension(116, 20));
		alphaCombobox.setModel(new DefaultComboBoxModel<Integer>(new Integer[] { 0, 1, 2, 3, 4, 5 }));
		alphaCombobox.setSelectedIndex(gui.getSettingsAlpha());
		alphaPanel.add(alphaCombobox);

		NumberFormat format = NumberFormat.getIntegerInstance(Locale.US);
		format.setGroupingUsed(false);
		
		JPanel shapesPanel = new JPanel();
		shapesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		shapesPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		shapesPanel.setPreferredSize(optionSize);
		shapesPanel.setMinimumSize(optionSize);
		shapesPanel.setMaximumSize(optionSize);
		panel.add(shapesPanel);
		shapesPanel.setLayout(new BoxLayout(shapesPanel, BoxLayout.Y_AXIS));
		
		JLabel shapesLabel = new JLabel("Max Shapes");
		shapesLabel.setToolTipText("The max amount of shapes used when drawing the image");
		shapesLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		shapesLabel.setHorizontalAlignment(SwingConstants.CENTER);
		shapesPanel.add(shapesLabel);
		
		NumberFormatter maxShapesFormatter = new NumberFormatter(format);
		maxShapesFormatter.setValueClass(Integer.class);
		maxShapesFormatter.setMinimum(0);
		maxShapesFormatter.setMaximum(99999);
		maxShapesFormatter.setAllowsInvalid(false);
		maxShapesFormatter.setCommitsOnValidEdit(true);
		
		maxShapesTextField = new JFormattedTextField(maxShapesFormatter);
		maxShapesTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
		maxShapesTextField.setFocusable(true);
		maxShapesTextField.setMaximumSize(new Dimension(116, 20));
		maxShapesTextField.setText(Integer.toString(gui.getSettingsMaxShapes()));
		shapesLabel.setLabelFor(maxShapesTextField);
		shapesPanel.add(maxShapesTextField);
		
		JPanel callbackPanel = new JPanel();
		callbackPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		callbackPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		callbackPanel.setPreferredSize(optionSize);
		callbackPanel.setMinimumSize(optionSize);
		callbackPanel.setMaximumSize(optionSize);
		panel.add(callbackPanel);
		callbackPanel.setLayout(new BoxLayout(callbackPanel, BoxLayout.Y_AXIS));
		
		JLabel callbackLabel = new JLabel("Callback Interval");
		callbackLabel.setToolTipText("The max amount of shapes generated before updating the screen");
		callbackLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		callbackLabel.setHorizontalAlignment(SwingConstants.CENTER);
		callbackPanel.add(callbackLabel);
		
		NumberFormatter calbackIntervalFormatter = new NumberFormatter(format);
		calbackIntervalFormatter.setValueClass(Integer.class);
		calbackIntervalFormatter.setMinimum(1);
		calbackIntervalFormatter.setMaximum(1000);
		calbackIntervalFormatter.setAllowsInvalid(false);
		calbackIntervalFormatter.setCommitsOnValidEdit(true);
		
		callbackIntervalTextField = new JFormattedTextField(calbackIntervalFormatter);
		callbackIntervalTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
		callbackIntervalTextField.setFocusable(true);
		callbackIntervalTextField.setMaximumSize(new Dimension(116, 20));
		callbackIntervalTextField.setText(Integer.toString(gui.getSettingsCallbackInterval()));
		callbackLabel.setLabelFor(callbackIntervalTextField);
		callbackPanel.add(callbackIntervalTextField);
		
		JPanel editorPanel = new JPanel();
		editorPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		editorPanel.setBorder(new TitledBorder(null, "Editor", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		dialog.getContentPane().add(editorPanel);
		editorPanel.setLayout(new BoxLayout(editorPanel, BoxLayout.Y_AXIS));
		
		{
			JPanel borderPanel = new JPanel();
			borderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			borderPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
			borderPanel.setPreferredSize(optionSize);
			borderPanel.setMinimumSize(optionSize);
			borderPanel.setMaximumSize(optionSize);
			editorPanel.add(borderPanel);
			borderPanel.setLayout(new BoxLayout(borderPanel, BoxLayout.Y_AXIS));
			
			JLabel lblBorderColor = new JLabel("Border Color");
			lblBorderColor.setToolTipText("The border color of the editor");
			borderPanel.add(lblBorderColor);
			
			JButton btnBorderColor = new JButton("Border Color");
			btnBorderColor.setPreferredSize(buttonSize);
			btnBorderColor.setMinimumSize(buttonSize);
			btnBorderColor.setMaximumSize(buttonSize);
			btnBorderColor.setFocusable(false);
			btnBorderColor.addActionListener((event) -> {
				Color color = JColorChooser.showDialog(dialog, "Color picker", gui.getBorderColor());
				if(color != null) {
					gui.setBorderColor(color);
				}
			});
			borderPanel.add(btnBorderColor);
			
			JPanel toolbarPanel = new JPanel();
			toolbarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			toolbarPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
			toolbarPanel.setPreferredSize(optionSize);
			toolbarPanel.setMinimumSize(optionSize);
			toolbarPanel.setMaximumSize(optionSize);
			editorPanel.add(toolbarPanel);
			toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.Y_AXIS));
			
			JLabel lblToolbarColor = new JLabel("Toolbar Color");
			lblToolbarColor.setToolTipText("The toolbar color of the editor");
			toolbarPanel.add(lblToolbarColor);
			
			JButton btnToolbarColor = new JButton("Toolbar Color");
			btnToolbarColor.setPreferredSize(buttonSize);
			btnToolbarColor.setMinimumSize(buttonSize);
			btnToolbarColor.setMaximumSize(buttonSize);
			btnToolbarColor.setFocusable(false);
			lblToolbarColor.setLabelFor(btnToolbarColor);
			btnToolbarColor.addActionListener((event) -> {
				Color color = JColorChooser.showDialog(dialog, "Color picker", gui.getToolbarColor());
				if(color != null) {
					gui.setToolbarColor(color);
				}
			});
			toolbarPanel.add(btnToolbarColor);
			
			JPanel labelPanel = new JPanel();
			labelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			labelPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
			labelPanel.setPreferredSize(optionSize);
			labelPanel.setMinimumSize(optionSize);
			labelPanel.setMaximumSize(optionSize);
			editorPanel.add(labelPanel);
			labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
			
			JLabel lblLabelColor = new JLabel("Label Color");
			lblLabelColor.setToolTipText("The label color of the editor");
			labelPanel.add(lblLabelColor);
			
			JButton btnLabelColor = new JButton("Label Color");
			btnLabelColor.setPreferredSize(buttonSize);
			btnLabelColor.setMinimumSize(buttonSize);
			btnLabelColor.setMaximumSize(buttonSize);
			btnLabelColor.setFocusable(false);
			btnLabelColor.addActionListener((event) -> {
				Color color = JColorChooser.showDialog(dialog, "Color picker", gui.getLabelColor());
				if(color != null) {
					gui.setLabelColor(color);
				}
			});
			labelPanel.add(btnLabelColor);
			
			JPanel resetPanel = new JPanel();
			resetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			resetPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
			resetPanel.setPreferredSize(optionSize);
			resetPanel.setMinimumSize(optionSize);
			resetPanel.setMaximumSize(optionSize);
			editorPanel.add(resetPanel);
			resetPanel.setLayout(new BoxLayout(resetPanel, BoxLayout.Y_AXIS));
			
			JLabel lblResetEditor = new JLabel("Reset Editor");
			lblResetEditor.setToolTipText("Reset the editor colors");
			resetPanel.add(lblResetEditor);
			
			JButton btnResetEditor = new JButton("Reset Editor");
			btnResetEditor.setPreferredSize(buttonSize);
			btnResetEditor.setMinimumSize(buttonSize);
			btnResetEditor.setMaximumSize(buttonSize);
			btnResetEditor.setFocusable(false);
			btnResetEditor.addActionListener((event) -> {
				int dialogResult = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to reset the editor?", "Warning", JOptionPane.YES_NO_OPTION);
				if(dialogResult == JOptionPane.YES_OPTION){
					gui.setBorderColor(null);
					gui.setToolbarColor(null);
					gui.setLabelColor(null);
				}
			});
			resetPanel.add(btnResetEditor);
		}
		
		dialog.setResizable(false);
		dialog.pack();
	}
	
	public void openDialog(Point point) {
		dialog.setLocation(point);
		dialog.setVisible(true);
		
		gui.setSettingsAlpha(alphaCombobox.getSelectedIndex());
		try {
			gui.setSettingsMaxShapes(Integer.parseInt(maxShapesTextField.getText()));
		} catch(NumberFormatException e) {
			maxShapesTextField.setText(Integer.toString(gui.getSettingsMaxShapes()));
			LogUtils.warn("Invalid max shapes count '%s'", maxShapesTextField.getText());
		}
		
		try {
			gui.setSettingsCallbackInterval(Integer.parseInt(callbackIntervalTextField.getText()));
		} catch(NumberFormatException e) {
			maxShapesTextField.setText(Integer.toString(gui.getSettingsCallbackInterval()));
			LogUtils.warn("Invalid callback interval '%s'", callbackIntervalTextField.getText());
		}
		
		gui.setSettingsSign(signPicker.getSelectedSign());
		gui.setSettingsBackground(colorPicker.getSelectedColor());
	}
}
