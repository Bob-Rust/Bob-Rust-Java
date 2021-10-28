package com.bobrust.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RustSigns {
	public static final Map<String, Sign> SIGNS;
	
	static {
		SIGNS = new HashMap<>();
		
		List.of(
			// Picture Frames
			new Sign("sign.pictureframe.landscape", 256, 128), // Landscape Picture Frame
			new Sign("sign.pictureframe.portrait", 128, 256),  // Portrait Picture Frame
			new Sign("sign.pictureframe.tall", 128, 512),      // Tall Picture Frame
			new Sign("sign.pictureframe.xl", 512, 512),        // XL Picture Frame
			new Sign("sign.pictureframe.xxl", 1024, 512),      // XXL Picture Frame
			
			// Wooden Signs
			new Sign("sign.wooden.small", 128, 64),  // Small Wooden Sign
			new Sign("sign.wooden.medium", 256, 128),// Wooden Sign
			new Sign("sign.wooden.large", 256, 128), // Large Wooden Sign
			new Sign("sign.wooden.huge", 512, 128),  // Huge Wooden Sign

			// Banners
			new Sign("sign.hanging.banner.large", 64, 256), // Large Banner Hanging
			new Sign("sign.pole.banner.large", 64, 256),    // Large Banner on Pole

			// Hanging Signs
			new Sign("sign.hanging", 128, 256),         // Two Sided Hanging Sign
			new Sign("sign.hanging.ornate", 256, 128),  // Two Sided Ornate Hanging Sign

			// Town Signs
			new Sign("sign.post.single", 128, 64),     // Single Sign Post
			new Sign("sign.post.double", 256, 256),    // Double Sign Post
			new Sign("sign.post.town", 256, 128),      // One Sided Town Sign Post
			new Sign("sign.post.town.roof", 256, 128)  // Two Sided Town Sign Post

			// new Sign("photoframe.large", 320, 240),
			// new Sign("photoframe.portrait", 320, 384),
			// new Sign("photoframe.landscape", 320, 240),

			// Other paintable assets
			// new Sign("spinner.wheel.deployed", 512, 512, 285, 285), // Spinning Wheel
		).forEach(i -> SIGNS.put(i.name, i));
	}
}
