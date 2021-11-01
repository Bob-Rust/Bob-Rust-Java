package com.bobrust.util;

import java.util.List;

import com.bobrust.generator.BorstColor;
import com.bobrust.generator.Circle;
import com.bobrust.generator.Model;
import com.bobrust.generator.sorter.Blob;
import com.bobrust.generator.sorter.BlobList;

public class RustUtil {
	public static BlobList convertToList(Model model, int count) {
		final int len = Math.min(model.colors.size(), count);
		
		BlobList list = new BlobList();
		for(int i = 0; i < len; i++) {
			Circle circle = model.shapes.get(i);
			BorstColor color = model.colors.get(i);
			list.add(Blob.get(circle.x, circle.y, circle.r, color.rgb));
		}
		
		return list;
	}
	
	/**
	 * Returns the amount of size and color changes between blobs.
	 */
	public static int getScore(BlobList list) {
		List<Blob> blobList = list.getList();
		Blob last = null;
		
		int changes = 2;
		for(Blob blob : blobList) {
			if(last != null) {
				changes += (last.size != blob.size) ? 1:0;
				changes += (last.color != blob.color) ? 1:0;
			}
			
			last = blob;
		}
		
		return changes;
	}
}
