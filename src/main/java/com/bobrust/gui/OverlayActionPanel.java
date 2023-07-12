package com.bobrust.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.bobrust.gui.BobRustDesktopOverlay.OverlayType;
import com.bobrust.gui.comp.JRandomPanel;
import com.bobrust.gui.comp.JStyledButton;
import com.bobrust.gui.comp.JStyledToggleButton;
import com.bobrust.lang.RustUI;
import com.bobrust.lang.RustUI.Type;
import com.bobrust.util.UrlUtils;

public class OverlayActionPanel extends JRandomPanel {
	private final BobRustDesktopOverlay desktopOverlay;
	private final JDialog dialog;

	final JPanel optionsPanel;
	final JLabel optionsPanelLabel;
	final JStyledButton btnToggleFullscreen;
	final JStyledButton btnSelectMonitor;
	final JStyledButton btnOpenImage;
	final JStyledButton btnOptions;
	
	final JPanel regionPanel;
	final JLabel regionPanelLabel;
	final JStyledToggleButton btnSelectCanvasRegion;
	final JStyledToggleButton btnSelectImageRegion;
	
	final JPanel previewPanel;
	final JLabel previewPanelLabel;
	final JStyledButton btnStartGenerate;
	final JStyledButton btnDrawImage;
	final JStyledButton btnClose;
	
	final JPanel helpPanel;
	final JLabel helpPanelLabel;
	final JStyledButton btnGithubIssue;
	final JStyledButton btnDonate;
	final JStyledButton btnAbout;
	
	public OverlayActionPanel(JDialog dialog, BobRustEditor gui, BobRustDesktopOverlay overlay) {
		super(gui);
		this.dialog = dialog;
		this.desktopOverlay = overlay;
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBounds(0, 0, 132, 500);
		
		Dimension buttonSize = new Dimension(120, 28);
		
		{
			optionsPanel = new JPanel();
			optionsPanel.setOpaque(false);
			optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			this.add(optionsPanel);
			optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
			
			optionsPanelLabel = new JLabel(RustUI.getString(Type.ACTION_OPTIONS_LABEL));
			optionsPanel.add(optionsPanelLabel);
			
			btnToggleFullscreen = new JStyledButton(RustUI.getString(Type.ACTION_MAKEFULLSCREEN_ON));
			btnToggleFullscreen.setMaximumSize(buttonSize);
			btnToggleFullscreen.addActionListener(this::performToggleFullscreen);
			optionsPanel.add(btnToggleFullscreen);
			
			btnSelectMonitor = new JStyledButton(RustUI.getString(Type.ACTION_SELECTMONITOR_BUTTON));
			btnSelectMonitor.setVisible(false);
			btnSelectMonitor.setMaximumSize(buttonSize);
			btnSelectMonitor.addActionListener(this::performSelectMonitor);
			optionsPanel.add(btnSelectMonitor);
			
			btnOpenImage = new JStyledButton(RustUI.getString(Type.ACTION_LOADIMAGE_BUTTON));
			btnOpenImage.setMaximumSize(buttonSize);
			btnOpenImage.addActionListener(this::performOpenImage);
			optionsPanel.add(btnOpenImage);
			
			btnOptions = new JStyledButton(RustUI.getString(Type.ACTION_OPTIONS_LABEL));
			btnOptions.setMaximumSize(buttonSize);
			btnOptions.addActionListener(this::performOpenOptions);
			optionsPanel.add(btnOptions);
		}
		
		{
			regionPanel = new JPanel();
			regionPanel.setVisible(false);
			regionPanel.setOpaque(false);
			regionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			this.add(regionPanel);
			regionPanel.setLayout(new BoxLayout(regionPanel, BoxLayout.Y_AXIS));
			
			regionPanelLabel = new JLabel(RustUI.getString(Type.ACTION_REGIONS_LABEL));
			regionPanelLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
			regionPanel.add(regionPanelLabel);
			
			btnSelectCanvasRegion = new JStyledToggleButton(RustUI.getString(Type.ACTION_CANVASREGION_BUTTON));
			btnSelectCanvasRegion.setMaximumSize(buttonSize);
			btnSelectCanvasRegion.setEnabled(false);
			btnSelectCanvasRegion.addActionListener(this::performSelectCanvasRegion);
			regionPanel.add(btnSelectCanvasRegion);
			
			btnSelectImageRegion = new JStyledToggleButton(RustUI.getString(Type.ACTION_IMAGEREGION_BUTTON));
			btnSelectImageRegion.setMaximumSize(buttonSize);
			btnSelectImageRegion.setEnabled(false);
			btnSelectImageRegion.addActionListener(this::performSelectImageRegion);
			regionPanel.add(btnSelectImageRegion);
		}
		
		{
			previewPanel = new JPanel();
			previewPanel.setOpaque(false);
			previewPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			this.add(previewPanel);
			previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
			
			previewPanelLabel = new JLabel(RustUI.getString(Type.ACTION_PREVIEWACTIONS_LABEL));
			previewPanelLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
			previewPanel.add(previewPanelLabel);
			
			btnStartGenerate = new JStyledButton(RustUI.getString(Type.ACTION_STARTGENERATE_BUTTON));
			btnStartGenerate.setMaximumSize(buttonSize);
			btnStartGenerate.setEnabled(false);
			btnStartGenerate.addActionListener(this::performStartGeneration);
			previewPanel.add(btnStartGenerate);
			
			btnDrawImage = new JStyledButton(RustUI.getString(Type.ACTION_DRAWIMAGE_BUTTON));
			btnDrawImage.setMaximumSize(buttonSize);
			btnDrawImage.setEnabled(false);
			btnDrawImage.addActionListener(this::performDrawImage);
			previewPanel.add(btnDrawImage);
			
			btnClose = new JStyledButton(RustUI.getString(Type.ACTION_CLOSE_BUTTON));
			btnClose.setVisible(false);
			btnClose.setMaximumSize(buttonSize);
			btnClose.addActionListener(this::performCloseApplication);
			previewPanel.add(btnClose);
		}
		
		{
			helpPanel = new JPanel();
			helpPanel.setOpaque(false);
			helpPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
			helpPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			this.add(helpPanel);
			helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));
			
			helpPanelLabel = new JLabel(RustUI.getString(Type.ACTION_HELP_LABEL));
			helpPanelLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
			helpPanel.add(helpPanelLabel);
			
			btnGithubIssue = new JStyledButton(RustUI.getString(Type.ACTION_REPORTISSUE_BUTTON));
			btnGithubIssue.setMaximumSize(buttonSize);
			btnGithubIssue.addActionListener(this::performOpenGithubIssue);
			helpPanel.add(btnGithubIssue);
			
			btnDonate = new JStyledButton(RustUI.getString(Type.ACTION_DONATE_BUTTON));
			btnDonate.setMaximumSize(buttonSize);
			btnDonate.addActionListener(this::performOpenDonateUrl);
			helpPanel.add(btnDonate);
			
			btnAbout = new JStyledButton(RustUI.getString(Type.ACTION_ABOUT_BUTTON));
			btnAbout.setMaximumSize(buttonSize);
			btnAbout.addActionListener(this::performShowAboutDialog);
			helpPanel.add(btnAbout);
		}
	}
	
	/**
	 * This method will update the language of all elements in this component.
	 */
	public void updateLanguage() {
		// Option Panel
		optionsPanelLabel.setText(RustUI.getString(Type.ACTION_OPTIONS_LABEL));
		btnToggleFullscreen.setText(desktopOverlay.isFullscreen()
			? RustUI.getString(Type.ACTION_MAKEFULLSCREEN_OFF)
			: RustUI.getString(Type.ACTION_MAKEFULLSCREEN_ON)
		);
		btnSelectMonitor.setText(RustUI.getString(Type.ACTION_SELECTMONITOR_BUTTON));
		btnOpenImage.setText(RustUI.getString(Type.ACTION_LOADIMAGE_BUTTON));
		btnOptions.setText(RustUI.getString(Type.ACTION_OPTIONS_LABEL));
		
		// Region Panel
		regionPanelLabel.setText(RustUI.getString(Type.ACTION_REGIONS_LABEL));
		btnSelectCanvasRegion.setText(RustUI.getString(Type.ACTION_CANVASREGION_BUTTON));
		btnSelectImageRegion.setText(RustUI.getString(Type.ACTION_IMAGEREGION_BUTTON));
		
		// Preview Panel
		previewPanelLabel.setText(RustUI.getString(Type.ACTION_PREVIEWACTIONS_LABEL));
		btnStartGenerate.setText(!desktopOverlay.isGeneratorRunning()
			? RustUI.getString(Type.ACTION_STARTGENERATE_BUTTON)
			: RustUI.getString(Type.ACTION_RESETGENERATE_BUTTON)
		);
		btnDrawImage.setText(RustUI.getString(Type.ACTION_DRAWIMAGE_BUTTON));
		btnClose.setText(RustUI.getString(Type.ACTION_CLOSE_BUTTON));
		
		// Help Panel
		helpPanelLabel.setText(RustUI.getString(Type.ACTION_HELP_LABEL));
		btnGithubIssue.setText(RustUI.getString(Type.ACTION_REPORTISSUE_BUTTON));
		btnDonate.setText(RustUI.getString(Type.ACTION_DONATE_BUTTON));
		btnAbout.setText(RustUI.getString(Type.ACTION_ABOUT_BUTTON));
	}
	
	/**
	 * This method will update the foreground color of all labels in this component.
	 */
	public void updateLabelForeground(Color foreground) {
		optionsPanelLabel.setForeground(foreground);
		regionPanelLabel.setForeground(foreground);
		previewPanelLabel.setForeground(foreground);
		helpPanelLabel.setForeground(foreground);
	}
	
	/**
	 * This method will update the button states inside this panel.
	 */
	public void updateButtons() {
		boolean isFullscreen = desktopOverlay.isFullscreen();
		boolean isGeneratorRunning = desktopOverlay.isGeneratorRunning();
		boolean hasImage = desktopOverlay.hasImage();
		OverlayType action = desktopOverlay.getOverlayType();
		
		boolean defaultAction = action == OverlayType.NONE;
		
		// You should only be able to pick monitor the screen is enabled
		btnSelectMonitor.setEnabled(defaultAction || isGeneratorRunning);
		regionPanel.setVisible(isFullscreen);
		
		btnSelectCanvasRegion.setEnabled(defaultAction || action == OverlayType.SELECT_CANVAS_REGION);
		btnSelectImageRegion.setEnabled(defaultAction && hasImage || action == OverlayType.SELECT_IMAGE_REGION);
		
		btnOpenImage.setEnabled(defaultAction);
		boolean hasRegion = btnSelectCanvasRegion.isSelected() || btnSelectImageRegion.isSelected();
		btnStartGenerate.setEnabled(isFullscreen && hasImage && desktopOverlay.canPerformGenerate() && !hasRegion);
		btnDrawImage.setEnabled(isFullscreen && isGeneratorRunning);
	}
	
	// Action events
	private void performSelectMonitor(ActionEvent event) {
		desktopOverlay.selectMonitor();
	}
	
	private void performToggleFullscreen(ActionEvent event) {
		boolean isFullscreen = desktopOverlay.isFullscreen();
		btnToggleFullscreen.setText(isFullscreen
			? RustUI.getString(Type.ACTION_MAKEFULLSCREEN_ON)
			: RustUI.getString(Type.ACTION_MAKEFULLSCREEN_OFF)
		);
		btnClose.setVisible(!isFullscreen);
		btnSelectMonitor.setVisible(!isFullscreen);
		
		desktopOverlay.toggleFullscreen();
	}
	
	private void performOpenImage(ActionEvent event) {
		desktopOverlay.openImage();
	}
	
	private void performOpenOptions(ActionEvent event) {
		desktopOverlay.openSettings(btnOptions.getLocationOnScreen());
	}
	
	private void performSelectCanvasRegion(ActionEvent event) {
		desktopOverlay.startSelectCanvasRegion(btnSelectCanvasRegion.isSelected());
	}
	
	private void performSelectImageRegion(ActionEvent event) {
		desktopOverlay.startSelectImageRegion(btnSelectImageRegion.isSelected());
	}
	
	private void performStartGeneration(ActionEvent event) {
		btnStartGenerate.setText(desktopOverlay.isGeneratorRunning()
			? RustUI.getString(Type.ACTION_STARTGENERATE_BUTTON)
			: RustUI.getString(Type.ACTION_RESETGENERATE_BUTTON)
		);
		
		if(!desktopOverlay.isGeneratorRunning()) {
			// Fix canvas region bug
			if (btnSelectCanvasRegion.isSelected()) {
				btnSelectCanvasRegion.setSelected(false);
				desktopOverlay.startSelectCanvasRegion(false);
			}
			
			if (btnSelectImageRegion.isSelected()) {
				btnSelectImageRegion.setSelected(false);
				desktopOverlay.startSelectImageRegion(false);
			}
			
			desktopOverlay.startGeneration();
		} else {
			desktopOverlay.resetGeneration();
		}
	}
	
	private void performDrawImage(ActionEvent event) {
		desktopOverlay.openDrawImage(btnDrawImage.getLocationOnScreen());
	}
	
	private void performCloseApplication(ActionEvent event) {
		int dialogResult = JOptionPane.showConfirmDialog(dialog,
			RustUI.getString(Type.ACTION_CLOSEDIALOG_MESSAGE),
			RustUI.getString(Type.ACTION_CLOSEDIALOG_TITLE),
			JOptionPane.YES_NO_OPTION
		);
		
		if(dialogResult == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}
	
	private void performOpenGithubIssue(ActionEvent event) {
		UrlUtils.openIssueUrl();
	}
	
	private void performOpenDonateUrl(ActionEvent event) {
		UrlUtils.openDonationUrl();
	}
	
	private void performShowAboutDialog(ActionEvent event) {
		JOptionPane.showMessageDialog(dialog,
			RustUI.getString(Type.ACTION_ABOUTME_MESSAGE),
			RustUI.getString(Type.ACTION_ABOUTME_TITLE),
			JOptionPane.INFORMATION_MESSAGE
		);
	}
}
