package com.bobrust.gui.dialog;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.bobrust.gui.BobRustEditor;
import com.bobrust.gui.comp.JStyledToggleButton;
import com.bobrust.lang.RustUI;
import com.bobrust.lang.RustUI.Type;
import com.bobrust.util.RustSigns;
import com.bobrust.util.Sign;

public class BobRustSignPicker {
	private final JDialog dialog;
	private Sign selectedSign;
	
	public BobRustSignPicker(BobRustEditor gui, JDialog parent) {
		dialog = new JDialog(parent, RustUI.getString(Type.EDITOR_SIGNPICKERDIALOG_TITLE), ModalityType.APPLICATION_MODAL);
		dialog.setSize(520, 670);
		dialog.setResizable(false);
		dialog.getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		ButtonGroup buttonGroup = new ButtonGroup();
		
		ActionListener buttonListener = (event) -> {
			Sign sign = RustSigns.SIGNS.get("sign." + event.getActionCommand());
			if(sign != null) {
				dialog.dispose();
				selectedSign = sign;
			}
		};
		
		Dimension buttonSize = new Dimension(120, 120);
		Dimension imageSize = new Dimension(80, 80);
		
		Sign guiSign = gui.getSettingsSign();
		selectedSign = guiSign;
		
		for(Sign sign : RustSigns.SIGNS.values()) {
			BufferedImage signImage = null;
			
			try(InputStream stream = BobRustSignPicker.class.getResourceAsStream("/signs/%s.png".formatted(sign.name))) {
				if(stream != null) {
					signImage = ImageIO.read(stream);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			if(signImage == null) {
				continue;
			}
			
			BufferedImage scaledImage = new BufferedImage(imageSize.width, imageSize.height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = scaledImage.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.drawImage(signImage, 0, 0, imageSize.width, imageSize.height, null);
			g.dispose();
			
			JStyledToggleButton button = new JStyledToggleButton(sign.name.replace("sign.", ""));
			button.setHoverColor(new Color(240, 240, 240));
			button.setIcon(new ImageIcon(scaledImage));
			button.setVerticalTextPosition(SwingConstants.TOP);
			button.setHorizontalTextPosition(SwingConstants.CENTER);
			button.setBorder(new EmptyBorder(0, 0, 0, 0));
			button.setPreferredSize(buttonSize);
			button.setMinimumSize(buttonSize);
			button.setMaximumSize(buttonSize);
			button.addActionListener(buttonListener);
			dialog.getContentPane().add(button);
			
			buttonGroup.add(button);
			dialog.getContentPane().add(button);
			
			if(guiSign == sign) {
				button.setSelected(true);
			}
		}
	}

	public void openSignDialog(Point point) {
		dialog.setLocation(point);
		dialog.setVisible(true);
	}

	public Sign getSelectedSign() {
		return selectedSign;
	}
}
