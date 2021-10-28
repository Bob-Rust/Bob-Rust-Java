package com.bobrust.gui;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.NumberFormatter;

import com.bobrust.logging.LogUtils;

public class BobRustSettings {
	private final BobRust gui;
	private final BobRustSignPicker signPicker;
	private final JDialog dialog;
	
	private final JComboBox<Integer> alphaCombobox;
	private final JFormattedTextField maxShapesTextField;
	
	public BobRustSettings(BobRust gui, JDialog parent) {
		this.gui = gui;
		this.dialog = new JDialog(parent, "Settings", ModalityType.APPLICATION_MODAL);
		this.signPicker = new BobRustSignPicker(dialog);
		dialog.getContentPane().setBackground(Color.DARK_GRAY);
		dialog.getContentPane().setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
		
		Dimension optionSize = new Dimension(160, 50);
		
		JPanel backgroundPanel = new JPanel();
		backgroundPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		backgroundPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
		dialog.getContentPane().add(backgroundPanel);
		backgroundPanel.setMaximumSize(optionSize);
		backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));
		
		JLabel lblBackgroundColor = new JLabel("Background Color");
		lblBackgroundColor.setToolTipText("The background color of the canvas");
		backgroundPanel.add(lblBackgroundColor);
		
		JButton btnBackgroundColor = new JButton("Background Color");
		btnBackgroundColor.setFocusable(false);
		lblBackgroundColor.setLabelFor(btnBackgroundColor);
		btnBackgroundColor.addActionListener((event) -> {
			Color initial = new Color(gui.borstSettings.Background, true);
			Color color = JColorChooser.showDialog(dialog, "Color pciker", initial, true);
			if(color != null) {
				gui.borstSettings.Background = color.getRGB();
			}
		});
		backgroundPanel.add(btnBackgroundColor);
		

		JPanel signPanel = new JPanel();
		signPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		signPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
		signPanel.setMaximumSize(optionSize);
		dialog.getContentPane().add(signPanel);
		signPanel.setLayout(new BoxLayout(signPanel, BoxLayout.Y_AXIS));
		
		JLabel signLabel = new JLabel("Sign Type");
		signLabel.setToolTipText("The type of sign to use when drawing");
		signLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		signLabel.setHorizontalAlignment(SwingConstants.CENTER);
		signPanel.add(signLabel);
		
		final JButton btnSign = new JButton("Select Sign Type");
		btnSign.setFocusable(false);
		btnSign.addActionListener((event) -> {
			Point dialogLocation = new Point(dialog.getLocationOnScreen());
			dialogLocation.x += 130;
			signPicker.openSignDialog(dialogLocation);
		});
		signPanel.add(btnSign);
		
		JPanel alphaPanel = new JPanel();
		alphaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		alphaPanel.setBorder(new EmptyBorder(5, 5, 0, 5));
		alphaPanel.setMaximumSize(optionSize);
		dialog.getContentPane().add(alphaPanel);
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
		alphaCombobox.setSelectedIndex(2);
		alphaPanel.add(alphaCombobox);
		
		JPanel shapesPanel = new JPanel();
		shapesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		shapesPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		shapesPanel.setMaximumSize(optionSize);
		dialog.getContentPane().add(shapesPanel);
		shapesPanel.setLayout(new BoxLayout(shapesPanel, BoxLayout.Y_AXIS));
		
		JLabel shapesLabel = new JLabel("Max Shapes");
		shapesLabel.setToolTipText("The max amount of shapes used when drawing the image");
		shapesLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		shapesLabel.setHorizontalAlignment(SwingConstants.CENTER);
		shapesPanel.add(shapesLabel);
		
		NumberFormat format = NumberFormat.getIntegerInstance(Locale.US);
		format.setGroupingUsed(false);
		NumberFormatter numberFormatter = new NumberFormatter(format);
		numberFormatter.setValueClass(Integer.class);
		numberFormatter.setMinimum(0);
		numberFormatter.setMaximum(30000);
		numberFormatter.setAllowsInvalid(false);
		numberFormatter.setCommitsOnValidEdit(true);
		
		maxShapesTextField = new JFormattedTextField(numberFormatter);
		maxShapesTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
		maxShapesTextField.setFocusable(true);
		maxShapesTextField.setMaximumSize(new Dimension(116, 20));
		maxShapesTextField.setText(Integer.toString(gui.borstSettings.MaxShapes));
		maxShapesTextField.addActionListener((event) -> {
			String text = maxShapesTextField.getText();
			try {
				int shapes = Integer.parseInt(text);
				gui.borstSettings.MaxShapes = shapes;
			} catch(NumberFormatException e) {
				LogUtils.warn("Invalid shapes number '%s'", text);
			}
		});
		shapesLabel.setLabelFor(maxShapesTextField);
		shapesPanel.add(maxShapesTextField);
		
		dialog.pack();
		dialog.setResizable(false);
	}
	
	public void openDialog() {
		dialog.setVisible(true);
		
		gui.borstSettings.Alpha = alphaCombobox.getSelectedIndex();
		String text = maxShapesTextField.getText();
		try {
			gui.borstSettings.MaxShapes = Integer.parseInt(text);
		} catch(NumberFormatException e) {
			maxShapesTextField.setText(Integer.toString(gui.borstSettings.MaxShapes));
			LogUtils.warn("Invalid shapes number '%s'", text);
		}
	}
	
	public Sign getSelectedSign() {
		return signPicker.getSelectedSign();
	}

	public void setLocation(Point point) {
		dialog.setLocation(point);
	}
}
