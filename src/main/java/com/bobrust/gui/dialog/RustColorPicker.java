package com.bobrust.gui.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.bobrust.generator.BorstUtils;
import com.bobrust.settings.Settings;
import com.bobrust.util.data.AppConstants;

/**
 * A color picker that looks similar to the color picker in rust
 */
public class RustColorPicker extends JDialog {
	private final JLabel colorLabel;
	private Color selectedColor;
	
	public RustColorPicker(JDialog parent) {
		super(parent, "Color picker", ModalityType.APPLICATION_MODAL);
		this.selectedColor = Settings.SettingsBackground.get();
		
		setSize(165, 340);
		setIconImage(AppConstants.DIALOG_ICON);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		Dimension panelSize = new Dimension(150, 301);
		JPanel panel = new JPanel();
		panel.setPreferredSize(panelSize);
		panel.setMinimumSize(panelSize);
		panel.setMaximumSize(panelSize);
		panel.setLayout(null);
		getContentPane().add(panel);

		JLabel lblCurrentColor = new JLabel("Current Color");
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
		colorLabel.setBackground(Settings.SettingsBackground.get());
		panel.add(colorLabel);
		
		JLabel lblColorPaletteImage = new JLabel(new ImageIcon(AppConstants.COLOR_PALETTE));
		lblColorPaletteImage.setBounds(0, 0, 150, 264);
		lblColorPaletteImage.setBorder(null);
		lblColorPaletteImage.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int mx = e.getX() - 12;
				int my = e.getY() - 12;
				if(mx > 0 && mx < 128 && my > 0 && my < 240) {
					int x = (mx * 4) / 127;
					int y = (my * 16) / 240;
					
					int cx = x * 31 + 25 + 12;
					int cy = y * 15 + 6 + 12;
					selectedColor = new Color(BorstUtils.getClosestColor(AppConstants.COLOR_PALETTE.getRGB(cx, cy)).rgb);
					colorLabel.setBackground(selectedColor);
				}
			}
		});
		panel.add(lblColorPaletteImage);
		
		JButton btnCustomColor = new JButton("Custom");
		btnCustomColor.setOpaque(false);
		btnCustomColor.setFocusable(false);
		btnCustomColor.setBounds(-1, 279, 77, 23);
		btnCustomColor.addActionListener((event) -> {
			Color color = JColorChooser.showDialog(this, "Color picker", selectedColor);
			if(color != null) {
				selectedColor = color;
				colorLabel.setBackground(selectedColor);
			}
		});
		panel.add(btnCustomColor);
		
		JButton btnDefaultColor = new JButton("Default");
		btnDefaultColor.setOpaque(false);
		btnDefaultColor.setFocusable(false);
		btnDefaultColor.setBounds(74, 279, 77, 23);
		btnDefaultColor.addActionListener((event) -> {
			selectedColor = null;
			colorLabel.setBackground(Settings.SettingsSign.get().getAverageColor());
		});
		panel.add(btnDefaultColor);
		
		pack();
	}

	public Color openColorDialog(Point point) {
		if (selectedColor == null) {
			colorLabel.setBackground(Settings.getSettingsBackgroundCalculated());
		}
		
		setLocation(point);
		setVisible(true);
		return selectedColor;
	}
}
