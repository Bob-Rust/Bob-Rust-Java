package com.bobrust.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface RustConstants {
	Logger LOGGER = LogManager.getLogger(RustConstants.class);
	
	// Used by BobRustPainter
	String CUSTOM_SIGN_NAME = "bobrust.custom";
	
	// Used by JRandomPanel
	boolean ENABLE_RANDOM_BACKGROUND = false;
	
	// Used by BorstGenerator
	boolean DEBUG_GENERATOR = false;
	
	// Used by BobRustPainter, BobRustDrawDialog
	boolean DEBUG_DRAWN_COLORS = false;
	
	// Used for image scaling
	int IMAGE_SCALING_NEAREST = 0;
	int IMAGE_SCALING_BILINEAR = 1;
	int IMAGE_SCALING_BICUBIC = 2;
	
	// TODO: Approximate these
	Color CANVAS_AVERAGE = new Color(0x8f887c);
	Color WOODEN_AVERAGE = new Color(0xd7c2ad); // 0x6d5033);
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
	
	BufferedImage COLOR_PALETTE = ResourceUtil.loadImageFromResources("/mapping/color_palette_new.png", LOGGER);
	Image DIALOG_ICON = ResourceUtil.loadMultiIconImageFromResources(
		"/icons/",
		List.of("16.png", "32.png", "64.png", "128.png"),
		LOGGER);
	String VERSION = ResourceUtil.readTextFromResources("/version", LOGGER);
}
