package com.bobrust.util;

import static com.bobrust.util.RustConstants.*;

import java.util.HashMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RustSigns {
	public static final Map<String, Sign> SIGNS;
	public static final Sign FIRST;
	
	static {
		SIGNS = new LinkedHashMap<>();
		
		List<Sign> signs = List.of(
			// Custom frame
			new Sign(CUSTOM_SIGN_NAME, -1, -1, CANVAS_AVERAGE), // Custom picture frame
			
			// Picture Frames
			new Sign("sign.pictureframe.landscape", 256, 128, CANVAS_AVERAGE), // Landscape Picture Frame
			new Sign("sign.pictureframe.portrait", 128, 256, CANVAS_AVERAGE),  // Portrait Picture Frame
			new Sign("sign.pictureframe.tall", 128, 512, CANVAS_AVERAGE),      // Tall Picture Frame
			new Sign("sign.pictureframe.xl", 512, 512, CANVAS_AVERAGE),        // XL Picture Frame
			new Sign("sign.pictureframe.xxl", 1024, 512, CANVAS_AVERAGE),      // XXL Picture Frame
			
			// Wooden Signs
			new Sign("sign.wooden.small", 128 * 2, 64 * 2, WOODEN_AVERAGE),   // Small Wooden Sign
			new Sign("sign.wooden.medium", 256 * 2, 128 * 2, WOODEN_AVERAGE), // Wooden Sign
			new Sign("sign.wooden.large", 256 * 2, 128 * 2, WOODEN_AVERAGE),  // Large Wooden Sign
			new Sign("sign.wooden.huge", 512 * 2, 128 * 2, WOODEN_AVERAGE),   // Huge Wooden Sign

			// Banners
			new Sign("sign.hanging.banner.large", 64 * 2, 256 * 2, CANVAS_AVERAGE), // Large Banner Hanging
			new Sign("sign.pole.banner.large", 64 * 2, 256 * 2, CANVAS_AVERAGE),    // Large Banner on Pole

			// Hanging Signs
			new Sign("sign.hanging", 128 * 2, 256 * 2, HANGING_METAL_AVERAGE),        // Two Sided Hanging Sign
			new Sign("sign.hanging.ornate", 256 * 2, 128 * 2, HANGING_METAL_AVERAGE), // Two Sided Ornate Hanging Sign

			// Town Signs
			new Sign("sign.post.single", 128 * 2, 64 * 2, CANVAS_AVERAGE),       // Single Sign Post
			new Sign("sign.post.double", 256 * 2, 256 * 2, CANVAS_AVERAGE),      // Double Sign Post
			new Sign("sign.post.town", 256 * 2, 128 * 2, TOWN_POST_AVERAGE),     // One Sided Town Sign Post
			new Sign("sign.post.town.roof", 256 * 2, 128 * 2, TOWN_POST_AVERAGE) // Two Sided Town Sign Post

			// new Sign("photoframe.large", 320, 240),
			// new Sign("photoframe.portrait", 320, 384),
			// new Sign("photoframe.landscape", 320, 240),

			// Other paintable assets
			// new Sign("spinner.wheel.deployed", 512, 512, 285, 285), // Spinning Wheel
		);
		
		FIRST = signs.get(1);
		signs.forEach(i -> SIGNS.put(i.name, i));
	}
}
