package com.bobrust.util;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.generator.BorstColor;
import com.bobrust.generator.Circle;
import com.bobrust.generator.Model;
import com.bobrust.generator.BorstGenerator.BorstData;
import com.bobrust.generator.sorter.Blob;
import com.bobrust.generator.sorter.BlobList;
import com.bobrust.generator.sorter.BorstSorter;

public class RustUtil {
	private static final Logger LOGGER = LogManager.getLogger(RustUtil.class);
	
	public static BlobList convertToList(Model model, int count) {
		int len = Math.min(model.colors.size(), count);
		
		BlobList list = new BlobList();
		for (int i = 0; i < len; i++) {
			Circle circle = model.shapes.get(i);
			BorstColor color = model.colors.get(i);
			list.add(Blob.get(circle.x, circle.y, circle.r, color.rgb));
		}
		
		return list;
	}
	
	/**
	 * Returns the amount of size and color changes between blobs.
	 */
	public static int getScore(BlobList list, int offset) {
		List<Blob> blobList = list.getList();
		Blob last = null;
		
		int changes = 2;
		for (int i = offset ; i < blobList.size(); i++) {
			Blob blob = blobList.get(i);
			if (last != null) {
				changes += (last.size != blob.size) ? 1:0;
				changes += (last.color != blob.color) ? 1:0;
			}
			
			last = blob;
		}
		
		return changes;
	}
	
	public static void dumpInfo(BorstData data) {
		Model model = data.getModel();
		
		BlobList list = new BlobList();
		for (int i = 0, len = model.colors.size(); i < len; i++) {
			Circle circle = model.shapes.get(i);
			BorstColor color = model.colors.get(i);
			list.add(Blob.get(circle.x, circle.y, circle.r, color.rgb));
		}
		
		LOGGER.info("Size change: {} / {}", getScore(list, 0), getScore(BorstSorter.sort(list), 0));
	}

	public static int clamp(int value, int min, int max) {
		return value < min ? min : (value > max ? max:value);
	}
}
