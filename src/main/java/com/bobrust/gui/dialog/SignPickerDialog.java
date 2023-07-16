package com.bobrust.gui.dialog;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.bobrust.gui.comp.JStyledToggleButton;
import com.bobrust.lang.RustUI;
import com.bobrust.settings.Settings;
import com.bobrust.util.data.AppConstants;
import com.bobrust.util.data.RustSigns;
import com.bobrust.util.Sign;

// TODO: Make it possible to use escape or enter to exit the dialog
public class SignPickerDialog extends JDialog {
	private Sign selectedSign;
	
	public SignPickerDialog(JDialog parent) {
		super(parent, "Sign Picker", ModalityType.APPLICATION_MODAL);
		setIconImage(AppConstants.DIALOG_ICON);
		setSize(520, 670);
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		ButtonGroup buttonGroup = new ButtonGroup();
		Dimension buttonSize = new Dimension(120, 120);
		Dimension imageSize = new Dimension(80, 80);
		
		Sign guiSign = Settings.SettingsSign.get();
		selectedSign = guiSign;
		
		for (Sign sign : RustSigns.SIGNS.values()) {
			BufferedImage signImage = null;
			
			try (InputStream stream = SignPickerDialog.class.getResourceAsStream("/signs/%s.png".formatted(sign.getName()))) {
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
			
			JStyledToggleButton button = new JStyledToggleButton(fancyName(sign.getName()));
			button.setHoverColor(new Color(240, 240, 240));
			button.setDefaultColor(new Color(235, 235, 235));
			button.setIcon(new ImageIcon(scaledImage));
			button.setVerticalTextPosition(SwingConstants.TOP);
			button.setHorizontalTextPosition(SwingConstants.CENTER);
			button.setBorder(new EmptyBorder(0, 0, 0, 0));
			button.setPreferredSize(buttonSize);
			button.setMinimumSize(buttonSize);
			button.setMaximumSize(buttonSize);
			button.addActionListener((event) -> selectedSign = sign);
			getContentPane().add(button);
			
			buttonGroup.add(button);
			getContentPane().add(button);
			
			if (guiSign == sign) {
				button.setSelected(true);
			}
		}
	}
	
	private String fancyName(String text) {
		return switch (text) {
			case "bobrust.custom" -> "Customizable";
			
			case "sign.pictureframe.landscape" -> "Landscape Picture Frame";
			case "sign.pictureframe.portrait" -> "Portrait Picture Frame";
			case "sign.pictureframe.tall" -> "Tall Picture Frame";
			case "sign.pictureframe.xl" -> "XL Picture Frame";
			case "sign.pictureframe.xxl" -> "XLL Picture Frame";
			
			case "sign.wooden.small" -> "Small Wooden Sign";
			case "sign.wooden.medium" -> "Medium Wooden Sign";
			case "sign.wooden.large" -> "Large Wooden Sign";
			case "sign.wooden.huge" -> "Huge Wooden Sign";
			
			case "sign.hanging.banner.large" -> "Large Banner Hanging";
			case "sign.pole.banner.large" -> "Large Banner on pole";
			
			case "sign.hanging" -> "Hanging Sign";
			case "sign.hanging.ornate"-> "Ornate Hanging Sign";
			
			case "sign.post.single" -> "Single Sign Post";
			case "sign.post.double" -> "Double Sign Post";
			case "sign.post.town" -> "One Sided Town Sign";
			case "sign.post.town.roof" -> "Two Sided Town Sign";
			
			default -> text.replace("sign.", "").replace('.', ' ');
		};
	}

	public void openSignDialog(Point point) {
		setLocation(point);
		setVisible(true);
	}

	public Sign getSelectedSign() {
		return selectedSign;
	}
}
