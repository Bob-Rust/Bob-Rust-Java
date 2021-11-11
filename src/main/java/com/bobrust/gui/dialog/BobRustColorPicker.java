package com.bobrust.gui.dialog;

import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.bobrust.generator.BorstUtils;
import com.bobrust.gui.BobRustEditor;
import com.bobrust.lang.RustUI;
import com.bobrust.lang.RustUI.Type;
import com.bobrust.util.RustConstants;

public class BobRustColorPicker {
	private final BobRustEditor gui;
	private final JDialog dialog;
	private final JLabel colorLabel;
	private Color selectedColor;
	
	public BobRustColorPicker(BobRustEditor gui, JDialog parent) {
		this.gui = gui;
		this.selectedColor = gui.getSettingsBackground();
		
		dialog = new JDialog(parent, RustUI.getString(Type.EDITOR_COLORDIALOG_TITLE), ModalityType.APPLICATION_MODAL);
		dialog.setSize(165, 340);
		dialog.setResizable(false);
		
		Dimension panelSize = new Dimension(150, 301);
		JPanel panel = new JPanel();
		panel.setPreferredSize(panelSize);
		panel.setMinimumSize(panelSize);
		panel.setMaximumSize(panelSize);
		panel.setLayout(null);
		dialog.getContentPane().add(panel);

		JLabel lblCurrentColor = new JLabel(RustUI.getString(Type.EDITOR_COLORPICKER_LABEL_CURRENTCOLOR));
		lblCurrentColor.setBorder(new EmptyBorder(0, 5, 0, 0));
		lblCurrentColor.setForeground(Color.WHITE);
		lblCurrentColor.setHorizontalTextPosition(SwingConstants.LEFT);
		lblCurrentColor.setOpaque(true);
		lblCurrentColor.setBounds(0, 264, 75, 16);
		lblCurrentColor.setBackground(new Color(0x232823));
		panel.add(lblCurrentColor);
		
		colorLabel = new JLabel("");
		colorLabel.setOpaque(true);
		colorLabel.setBorder(new LineBorder(new Color(255, 255, 255)));
		colorLabel.setHorizontalTextPosition(SwingConstants.LEFT);
		colorLabel.setBounds(75, 264, 75, 16);
		colorLabel.setBackground(gui.getSettingsBackground());
		panel.add(colorLabel);
		
		JLabel lblColorPaletteImage = new JLabel(new ImageIcon(RustConstants.COLOR_PALETTE));
		lblColorPaletteImage.setBounds(0, 0, 150, 264);
		lblColorPaletteImage.setBorder(null);
		lblColorPaletteImage.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int mx = e.getX() - 12;
				int my = e.getY() - 12;
				if(mx > 0 && mx < 128 && my > 0 && my < 240) {
					int x = (mx * 4) / 127;
					int y = (my * 8) / 240;
					
					int cx = x * 31 + 15 + 12;
					int cy = y * 30 + 15 + 12;
					selectedColor = new Color(BorstUtils.getClosestColor(RustConstants.COLOR_PALETTE.getRGB(cx, cy)).rgb);
					colorLabel.setBackground(selectedColor);
				}
			}
		});
		panel.add(lblColorPaletteImage);
		
		JButton btnCustomColor = new JButton(RustUI.getString(Type.EDITOR_COLORPICKER_BUTTON_CUSTOM));
		btnCustomColor.setOpaque(false);
		btnCustomColor.setFocusable(false);
		btnCustomColor.setBounds(-1, 279, 77, 23);
		btnCustomColor.addActionListener((event) -> {
			Color color = JColorChooser.showDialog(dialog, RustUI.getString(Type.EDITOR_COLORDIALOG_TITLE), selectedColor);
			if(color != null) {
				selectedColor = color;
				colorLabel.setBackground(selectedColor);
			}
		});
		panel.add(btnCustomColor);
		
		JButton btnDefaultColor = new JButton(RustUI.getString(Type.EDITOR_COLORPICKER_BUTTON_DEFAULT));
		btnDefaultColor.setOpaque(false);
		btnDefaultColor.setFocusable(false);
		btnDefaultColor.setBounds(74, 279, 77, 23);
		btnDefaultColor.addActionListener((event) -> {
			selectedColor = null;
			colorLabel.setBackground(gui.getSettingsSign().getAverageColor());
		});
		panel.add(btnDefaultColor);
		
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
