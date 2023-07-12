package com.bobrust.gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.*;

import com.bobrust.lang.RustTranslator;
import com.bobrust.settings.Settings;

@SuppressWarnings("serial")
public class OverlayTopPanel extends JPanel {
	private static final int ESTIMATE_DELAY_OFFSET = 14;
	
	private final BobRustEditor gui;
	
	final JLabel generationLabel;
	final JLabel generationInfo;
	
	public OverlayTopPanel(BobRustEditor gui) {
		this.gui = gui;
		this.setBounds(150, 5, 10, 10);
		this.setBackground(new Color(0x7f000000, true));
		this.setLayout(null);
		
		generationLabel = new JLabel("No active generation");
		generationLabel.setHorizontalAlignment(SwingConstants.CENTER);
		generationLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		generationLabel.setForeground(Color.lightGray);
		generationLabel.setOpaque(false);
		generationLabel.setFont(generationLabel.getFont().deriveFont(18.0f));
		generationLabel.setBounds(0, 0, 440, 25);
		generationLabel.setBackground(Color.blue);
		this.add(generationLabel);
		
		generationInfo = new JLabel("");
		generationInfo.setHorizontalAlignment(SwingConstants.CENTER);
		generationInfo.setHorizontalTextPosition(SwingConstants.CENTER);
		generationInfo.setOpaque(false);
		generationInfo.setForeground(Color.WHITE);
		generationInfo.setFont(generationInfo.getFont().deriveFont(Font.BOLD, 16.0f));
		generationInfo.setBounds(0, 20, 440, 20);
		generationInfo.setBackground(Color.red);
		this.add(generationInfo);
	}
	
	/**
	 * This method will update the language of all elements in this component.
	 */
	public void updateLanguage() {
		
	}
	
	public void setEstimatedGenerationLabel(int index, int maxShapes) {
		generationLabel.setText("%d/%d shapes generated".formatted(index, maxShapes));
		long time = (long)(index * 1.1 * (ESTIMATE_DELAY_OFFSET + 1000.0 / (double) Settings.SettingsClickInterval.get()));
		generationInfo.setText("Estimated %s".formatted(RustTranslator.getTimeMinutesMessage(time)));
	}
	
	public void setExactGenerationLabel(long time) {
		generationInfo.setText("Time %s".formatted(RustTranslator.getTimeMinutesMessage(time)));
	}
	
	public void setRemainingTime(int index, int maxShapes, long timeLeft) {
		generationLabel.setText("%d/%d shapes drawn".formatted(index, maxShapes));
		generationInfo.setText("Time left %s".formatted(RustTranslator.getTimeMinutesMessage(timeLeft)));
	}
}
