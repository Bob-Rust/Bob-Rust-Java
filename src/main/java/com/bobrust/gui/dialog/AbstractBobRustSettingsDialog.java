package com.bobrust.gui.dialog;

import com.bobrust.gui.comp.JDimensionField;
import com.bobrust.gui.comp.JIntegerField;
import com.bobrust.lang.RustUI;
import com.bobrust.lang.RustUI.Type;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractBobRustSettingsDialog {
	protected final List<TabbedPane> tabbedPanes;
	
	public AbstractBobRustSettingsDialog() {
		this.tabbedPanes = new ArrayList<>();
	}
	
	public TabbedPane createPane(JTabbedPane tabbedPane, RustUI.Type title) {
		TabbedPane pane = new TabbedPane(tabbedPane, title);
		tabbedPanes.add(pane);
		return pane;
	}
	
	protected JIntegerField addIntegerField(TabbedPane tab, int def, int min, int max, RustUI.Type label, RustUI.Type tooltip) {
		int y_pos = tab.getElements();
		
		JLabel lblLabel = new JLabel(RustUI.getString(label));
		if (tooltip != null) {
			lblLabel.setToolTipText(RustUI.getString(tooltip));
		}
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = y_pos;
		tab.panel.add(lblLabel, gbc_label);
		
		JIntegerField intField = new JIntegerField(def);
		lblLabel.setLabelFor(intField);
		intField.setAlignmentX(Component.LEFT_ALIGNMENT);
		intField.setFocusable(true);
		intField.setMinimum(min);
		intField.setMaximum(max);
		intField.setMaximumSize(new Dimension(116, 20));
		GridBagConstraints gbc_intField = new GridBagConstraints();
		gbc_intField.fill = GridBagConstraints.HORIZONTAL;
		gbc_intField.insets = new Insets(0, 0, 5, 0);
		gbc_intField.gridx = 1;
		gbc_intField.gridy = y_pos;
		tab.panel.add(intField, gbc_intField);
		
		tab.localizationCallback.add(() -> {
			lblLabel.setText(RustUI.getString(label));
			if (tooltip != null) {
				lblLabel.setToolTipText(RustUI.getString(tooltip));
			}
		});
		
		return intField;
	}
	
	protected JDimensionField addDimensionField(TabbedPane tab, Dimension def, Dimension min, Dimension max, RustUI.Type label, RustUI.Type tooltip) {
		int y_pos = tab.getElements();
		
		JLabel lblLabel = new JLabel(RustUI.getString(label));
		if (tooltip != null) {
			lblLabel.setToolTipText(RustUI.getString(tooltip));
		}
		
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = y_pos;
		tab.panel.add(lblLabel, gbc_label);
		
		JDimensionField field = new JDimensionField(def);
		field.setMinimumDimension(min);
		field.setMaximumDimension(max);
		field.setMaximumSize(new Dimension(116, 20));
		GridBagConstraints gbc_intField = new GridBagConstraints();
		gbc_intField.fill = GridBagConstraints.HORIZONTAL;
		gbc_intField.insets = new Insets(0, 0, 5, 0);
		gbc_intField.gridx = 1;
		gbc_intField.gridy = y_pos;
		tab.panel.add(field, gbc_intField);
		
		tab.localizationCallback.add(() -> {
			lblLabel.setText(RustUI.getString(label));
			if (tooltip != null) {
				lblLabel.setToolTipText(RustUI.getString(tooltip));
			}
		});
		
		return field;
	}
	
	protected <T> JComboBox<T> addComboBoxField(TabbedPane tab, int defaultValue, T[] values, RustUI.Type label, RustUI.Type tooltip) {
		int y_pos = tab.getElements();
		
		JLabel lblLabel = new JLabel(RustUI.getString(label));
		if (tooltip != null) {
			lblLabel.setToolTipText(RustUI.getString(tooltip));
		}
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = y_pos;
		tab.panel.add(lblLabel, gbc_label);
		
		JComboBox<T> comboBox = new JComboBox<>();
		lblLabel.setLabelFor(comboBox);
		comboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		comboBox.setFocusable(false);
		comboBox.setMaximumSize(new Dimension(116, 20));
		comboBox.setModel(new DefaultComboBoxModel<>(values));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = y_pos;
		tab.panel.add(comboBox, gbc_comboBox);
		comboBox.setSelectedIndex(defaultValue);
		
		tab.localizationCallback.add(() -> {
			lblLabel.setText(RustUI.getString(label));
			if (tooltip != null) {
				lblLabel.setToolTipText(RustUI.getString(tooltip));
			}
		});
		
		return comboBox;
	}
	
	protected JButton addButtonField(TabbedPane tab, Type label, Type tooltip, Type button, ActionListener listener) {
		int y_pos = tab.getElements();
		
		Dimension buttonSize = new Dimension(120, 23);
		
		JLabel lblLabel = new JLabel(RustUI.getString(label));
		if (tooltip != null) {
			lblLabel.setToolTipText(RustUI.getString(tooltip));
		}
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = y_pos;
		tab.panel.add(lblLabel, gbc_label);
		
		JButton btnButton = new JButton(RustUI.getString(button));
		lblLabel.setLabelFor(btnButton);
		btnButton.addActionListener(listener);
		btnButton.setFocusable(false);
		btnButton.setPreferredSize(buttonSize);
		btnButton.setMinimumSize(buttonSize);
		btnButton.setMaximumSize(buttonSize);
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.fill = GridBagConstraints.HORIZONTAL;
		gbc_button.insets = new Insets(0, 0, 5, 0);
		gbc_button.gridx = 1;
		gbc_button.gridy = y_pos;
		tab.panel.add(btnButton, gbc_button);
		
		tab.localizationCallback.add(() -> {
			lblLabel.setText(RustUI.getString(label));
			if (tooltip != null) {
				lblLabel.setToolTipText(RustUI.getString(tooltip));
			}
			btnButton.setText(RustUI.getString(button));
		});
		
		return btnButton;
	}
	
	protected void updateLanguage() {
		for (TabbedPane pane : tabbedPanes) {
			pane.updateLanguage();
		}
	}
	
	protected static class TabbedPane {
		protected static final int MAX_HEIGHT = 10;
		
		protected final JTabbedPane tabbedPane;
		protected final RustUI.Type title;
		protected final JPanel panel;
		protected final List<Runnable> localizationCallback;
		protected final int index;
		
		private TabbedPane(JTabbedPane tabbedPane, RustUI.Type title) {
			this.tabbedPane = tabbedPane;
			this.title = title;
			this.panel = new JPanel();
			this.localizationCallback = new ArrayList<>();
			this.index = tabbedPane.getTabCount();
			
			panel.setFocusable(false);
			panel.setAlignmentY(Component.TOP_ALIGNMENT);
			panel.setBorder(new EmptyBorder(5, 5, 5, 5));
			panel.setOpaque(false);
			tabbedPane.addTab(RustUI.getString(title), panel);
			
			GridBagLayout gbl_panel = new GridBagLayout();
			gbl_panel.columnWidths = new int[] { 140, 0, 0 };
			gbl_panel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panel.rowHeights = new int[MAX_HEIGHT];
			gbl_panel.rowWeights = new double[MAX_HEIGHT];
			
			Arrays.fill(gbl_panel.rowHeights, 20);
			Arrays.fill(gbl_panel.rowWeights, 0.0);
			gbl_panel.rowWeights[MAX_HEIGHT - 1] = 1.0;
			
			panel.setLayout(gbl_panel);
		}
		
		public int getElements() {
			return panel.getComponentCount() / 2;
		}
		
		public void updateLanguage() {
			for (Runnable runnable : localizationCallback) {
				runnable.run();
			}
			
			tabbedPane.setTitleAt(index, RustUI.getString(title));
		}
	}
}
