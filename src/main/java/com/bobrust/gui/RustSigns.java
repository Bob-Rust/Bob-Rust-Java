package com.bobrust.gui;

import java.util.HashMap;

import java.util.List;
import java.util.Map;

import static com.bobrust.gui.BobRustConstants.*;

public class RustSigns {
	public static final Map<String, Sign> SIGNS;
	public static final Sign FIRST;
	
	static {
		SIGNS = new HashMap<>();
		
		List<Sign> signs = List.of(
			// Picture Frames
			new Sign("sign.pictureframe.landscape", 256, 128, CANVAS_AVERAGE), // Landscape Picture Frame
			new Sign("sign.pictureframe.portrait", 128, 256, CANVAS_AVERAGE),  // Portrait Picture Frame
			new Sign("sign.pictureframe.tall", 128, 512, CANVAS_AVERAGE),      // Tall Picture Frame
			new Sign("sign.pictureframe.xl", 512, 512, CANVAS_AVERAGE),        // XL Picture Frame
			new Sign("sign.pictureframe.xxl", 1024, 512, CANVAS_AVERAGE),      // XXL Picture Frame
			
			// Wooden Signs
			new Sign("sign.wooden.small", 128, 64, WOODEN_AVERAGE),  // Small Wooden Sign
			new Sign("sign.wooden.medium", 256, 128, WOODEN_AVERAGE),// Wooden Sign
			new Sign("sign.wooden.large", 256, 128, WOODEN_AVERAGE), // Large Wooden Sign
			new Sign("sign.wooden.huge", 512, 128, WOODEN_AVERAGE),  // Huge Wooden Sign

			// Banners
			new Sign("sign.hanging.banner.large", 64, 256, CANVAS_AVERAGE), // Large Banner Hanging
			new Sign("sign.pole.banner.large", 64, 256, CANVAS_AVERAGE),    // Large Banner on Pole

			// Hanging Signs
			new Sign("sign.hanging", 128, 256, HANGING_METAL_AVERAGE),        // Two Sided Hanging Sign
			new Sign("sign.hanging.ornate", 256, 128, HANGING_METAL_AVERAGE), // Two Sided Ornate Hanging Sign

			// Town Signs
			new Sign("sign.post.single", 128, 64, CANVAS_AVERAGE),       // Single Sign Post
			new Sign("sign.post.double", 256, 256, CANVAS_AVERAGE),      // Double Sign Post
			new Sign("sign.post.town", 256, 128, TOWN_POST_AVERAGE),     // One Sided Town Sign Post
			new Sign("sign.post.town.roof", 256, 128, TOWN_POST_AVERAGE) // Two Sided Town Sign Post

			// new Sign("photoframe.large", 320, 240),
			// new Sign("photoframe.portrait", 320, 384),
			// new Sign("photoframe.landscape", 320, 240),

			// Other paintable assets
			// new Sign("spinner.wheel.deployed", 512, 512, 285, 285), // Spinning Wheel
		);
		
		FIRST = signs.get(0);
		signs.forEach(i -> SIGNS.put(i.name, i));
	}
}
