package com.bobrust.gui.dialog;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.bobrust.gui.comp.JStyledToggleButton;
import com.bobrust.lang.RustUI;
import com.bobrust.lang.RustUI.Type;
import com.bobrust.settings.Settings;
import com.bobrust.util.RustSigns;
import com.bobrust.util.Sign;

public class BobRustSignPicker {
	private final JDialog dialog;
	private Sign selectedSign;
	
	public BobRustSignPicker(JDialog parent) {
		dialog = new JDialog(parent, RustUI.getString(Type.EDITOR_SIGNPICKERDIALOG_TITLE), ModalityType.APPLICATION_MODAL);
		dialog.setSize(520, 670);
		dialog.setResizable(false);
		dialog.getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		ButtonGroup buttonGroup = new ButtonGroup();
		Dimension buttonSize = new Dimension(120, 120);
		Dimension imageSize = new Dimension(80, 80);
		
		Sign guiSign = Settings.SettingsSign.get();
		selectedSign = guiSign;
		
		for (Sign sign : RustSigns.SIGNS.values()) {
			BufferedImage signImage = null;
			
			try (InputStream stream = BobRustSignPicker.class.getResourceAsStream("/signs/%s.png".formatted(sign.name))) {
				if (stream != null) {
					signImage = ImageIO.read(stream);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (signImage == null) {
				continue;
			}
			
			Image scaledImage = signImage.getScaledInstance(imageSize.width, imageSize.height, Image.SCALE_SMOOTH);
			
			JStyledToggleButton button = new JStyledToggleButton(fancyName(sign.name));
			button.setHoverColor(new Color(240, 240, 240));
			button.setIcon(new ImageIcon(scaledImage));
			button.setVerticalTextPosition(SwingConstants.TOP);
			button.setHorizontalTextPosition(SwingConstants.CENTER);
			button.setBorder(new EmptyBorder(0, 0, 0, 0));
			button.setPreferredSize(buttonSize);
			button.setMinimumSize(buttonSize);
			button.setMaximumSize(buttonSize);
			button.addActionListener((event) -> selectedSign = sign);
			dialog.getContentPane().add(button);
			
			buttonGroup.add(button);
			dialog.getContentPane().add(button);
			
			if (guiSign == sign) {
				button.setSelected(true);
			}
		}
	}
	
	private String fancyName(String text) {
		return text.replace("sign.", "").replace('.', ' ');
	}

	public void openSignDialog(Point point) {
		dialog.setLocation(point);
		dialog.setVisible(true);
	}

	public Sign getSelectedSign() {
		return selectedSign;
	}
}
