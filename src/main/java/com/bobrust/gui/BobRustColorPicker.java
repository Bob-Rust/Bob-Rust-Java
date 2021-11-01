package com.bobrust.gui;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.border.LineBorder;

import com.bobrust.util.RustConstants;

import javax.swing.border.EmptyBorder;

public class BobRustColorPicker {
	private final BobRustEditor gui;
	private final JDialog dialog;
	private final JLabel colorLabel;
	private Color selectedColor;
	
	public BobRustColorPicker(BobRustEditor gui, JDialog parent) {
		this.gui = gui;
		
		dialog = new JDialog(parent, "Color picker", ModalityType.APPLICATION_MODAL);
		dialog.setSize(165, 340);
		dialog.setResizable(false);
		
		selectedColor = gui.getSettingsBackground();
		Dimension panelSize = new Dimension(150, 301);
		JPanel panel_2 = new JPanel();
		panel_2.setPreferredSize(panelSize);
		panel_2.setMinimumSize(panelSize);
		panel_2.setMaximumSize(panelSize);
		panel_2.setLayout(null);
		dialog.getContentPane().add(panel_2);

		JLabel lblCurrentColor = new JLabel("Current Color");
		lblCurrentColor.setBorder(new EmptyBorder(0, 5, 0, 0));
		lblCurrentColor.setForeground(Color.WHITE);
		lblCurrentColor.setHorizontalTextPosition(SwingConstants.LEFT);
		lblCurrentColor.setOpaque(true);
		lblCurrentColor.setBounds(0, 264, 75, 16);
		lblCurrentColor.setBackground(new Color(0x232823));
		panel_2.add(lblCurrentColor);
		
		colorLabel = new JLabel("");
		colorLabel.setOpaque(true);
		colorLabel.setBorder(new LineBorder(new Color(255, 255, 255)));
		colorLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		colorLabel.setBounds(75, 264, 75, 16);
		colorLabel.setBackground(gui.getSettingsBackground());
		panel_2.add(colorLabel);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(12, 12, 127, 240);
		panel_1.setBackground(new Color(0x01000000, true));
		panel_2.add(panel_1);
		panel_1.setLayout(null);
		panel_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int x = (e.getX() * 4) / 127;
				int y = (e.getY() * 8) / 240;
				
				int cx = x * 31 + 15 + 12;
				int cy = y * 30 + 15 + 12;
				selectedColor = new Color(RustConstants.COLOR_PALETTE.getRGB(cx, cy));
				colorLabel.setBackground(selectedColor);
			}
		});
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 0, 150, 264);
		panel.setBorder(null);
		panel_2.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setBorder(null);
		lblNewLabel.setIcon(new ImageIcon(RustConstants.COLOR_PALETTE));
		panel.add(lblNewLabel);
		
		JButton btnCustomColor = new JButton("Custom");
		btnCustomColor.setOpaque(false);
		btnCustomColor.setFocusable(false);
		btnCustomColor.setBounds(-1, 279, 77, 23);
		btnCustomColor.addActionListener((event) -> {
			Color color = JColorChooser.showDialog(dialog, "Color picker", selectedColor);
			if(color != null) {
				selectedColor = color;
				colorLabel.setBackground(selectedColor);
			}
		});
		panel_2.add(btnCustomColor);
		
		JButton btnDefaultColor = new JButton("Default");
		btnDefaultColor.setOpaque(false);
		btnDefaultColor.setFocusable(false);
		btnDefaultColor.setBounds(74, 279, 77, 23);
		btnDefaultColor.addActionListener((event) -> {
			selectedColor = null;
			colorLabel.setBackground(gui.getSettingsSign().getAverageColor());
		});
		panel_2.add(btnDefaultColor);
		
		dialog.pack();
	}

	public Color openColorDialog(Point point) {
		if(selectedColor == null) {
			colorLabel.setBackground(gui.getSettingsSign().getAverageColor());
		}
		
		dialog.setLocation(point);
		dialog.setVisible(true);
		return selectedColor;
	}

	public Color getSelectedColor() {
		return selectedColor;
	}
}
