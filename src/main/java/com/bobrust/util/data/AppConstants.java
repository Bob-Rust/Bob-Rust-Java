package com.bobrust.util.data;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.List;

import com.bobrust.util.ResourceUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface AppConstants {
	Logger LOGGER = LogManager.getLogger(AppConstants.class);
	
	// Used to debug
	boolean IS_IDE = !AppConstants.class.getProtectionDomain()
		.getCodeSource()
		.getLocation()
		.getPath().endsWith(".jar");

	// Used by BorstGenerator
	boolean DEBUG_AUTO_IMAGE = true;
	boolean DEBUG_GENERATOR = false;
	boolean DEBUG_DRAWN_COLORS = false;
	boolean DEBUG_TIME = false;
	int MAX_SORT_GROUP = 1000; // Max 1000 elements per sort
	
	// Average canvas colors. Used as default colors
	Color CANVAS_AVERAGE = new Color(0xb3aba0);
	Color WOODEN_AVERAGE = new Color(0xa89383);
	Color TOWN_POST_AVERAGE = new Color(0x94624d);
	Color HANGING_METAL_AVERAGE = new Color(0x534c46);
	
	// Shapes
	int CIRCLE_SHAPE = 1;
	int SQUARE_SHAPE = 3;
	
	// Used by JStyledButton, JStyledToggleButton
	int BUTTON_BORDER_RADIUS = 20;
	Color BUTTON_DEFAULT_COLOR = new Color(242, 242, 242);
	Color BUTTON_HOVER_COLOR = new Color(229, 243, 255);
	Color BUTTON_DISABLED_COLOR = new Color(192, 192, 192);
	Color BUTTON_SELECTED_COLOR = new Color(204, 232, 255);
	
	BufferedImage COLOR_PALETTE = ResourceUtil.loadImageFromResources("/mapping/palette_2.png", LOGGER);
	Image DIALOG_ICON = ResourceUtil.loadMultiIconImageFromResources(
		"/icons/",
		List.of("16.png", "32.png", "64.png", "128.png"),
		LOGGER);
	String VERSION = ResourceUtil.readTextFromResources("/version", LOGGER);
}
