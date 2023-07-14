package com.bobrust.gui;

import java.awt.*;

import javax.swing.*;

import com.bobrust.lang.RustTranslator;
import com.bobrust.settings.Settings;

// TODO: This should be visible in the top of the screen when drawing the image
//       This should be the same size as the RegionPickerDialog top
//       Which is 50 pixels
public class OverlayTopPanel extends JPanel {
	private static final int ESTIMATE_DELAY_OFFSET = 14;
	
	final JLabel generationLabel;
	final JLabel generationInfo;
	
	public OverlayTopPanel() {
		setLayout(new BorderLayout());
		setBackground(new Color(0x333e48));
		setDoubleBuffered(true);
		
		generationLabel = new JLabel("No active generation");
		generationLabel.setHorizontalAlignment(SwingConstants.CENTER);
		generationLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		generationLabel.setOpaque(false);
		generationLabel.setForeground(Color.lightGray);
		generationLabel.setFont(generationLabel.getFont().deriveFont(18.0f));
		add(generationLabel, BorderLayout.NORTH);
		
		generationInfo = new JLabel("");
		generationInfo.setHorizontalAlignment(SwingConstants.CENTER);
		generationInfo.setHorizontalTextPosition(SwingConstants.CENTER);
		generationInfo.setOpaque(false);
		generationInfo.setForeground(Color.WHITE);
		generationInfo.setFont(generationInfo.getFont().deriveFont(Font.BOLD, 16.0f));
		add(generationInfo, BorderLayout.CENTER);
	}
	
	/*
	public void setEstimatedGenerationLabel(int index, int maxShapes) {
		generationLabel.setText("%d/%d shapes generated".formatted(index, maxShapes));
		long time = (long) (index * 1.1 * (ESTIMATE_DELAY_OFFSET + 1000.0 / (double) Settings.SettingsClickInterval.get()));
		generationInfo.setText("Estimated %s".formatted(RustTranslator.getTimeMinutesMessage(time)));
	}
	*/
	
	public void setExactGenerationLabel(long time) {
		generationInfo.setText("Time %s".formatted(RustTranslator.getTimeMinutesMessage(time)));
	}
	
	/*
	public void setRemainingTime(int index, int maxShapes, long timeLeft) {
		generationLabel.setText("%d/%d shapes drawn".formatted(index, maxShapes));
		generationInfo.setText("Time left %s".formatted(RustTranslator.getTimeMinutesMessage(timeLeft)));
	}
	*/
	
	public void setGeneratedShapes(int shapesUsed, int maxShapes) {
		long time = (long) (shapesUsed * 1.1 * (ESTIMATE_DELAY_OFFSET + 1000.0 / (double) Settings.SettingsClickInterval.get()));
		generationLabel.setText("%d/%d shapes used".formatted(shapesUsed, maxShapes));
		generationInfo.setText("Estimated %s".formatted(RustTranslator.getTimeMinutesMessage(time)));
	}
	
	public void setDrawnShapes(int index, int maxShapes, long timeLeft) {
		generationLabel.setText("%d/%d shapes drawn".formatted(index, maxShapes));
		generationInfo.setText("Time left %s".formatted(RustTranslator.getTimeMinutesMessage(timeLeft)));
	}
}
