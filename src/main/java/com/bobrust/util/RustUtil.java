package com.bobrust.util;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bobrust.generator.BorstGenerator.BorstData;
import com.bobrust.generator.sorter.Blob;
import com.bobrust.generator.sorter.BlobList;
import com.bobrust.generator.sorter.BorstSorter;

public class RustUtil {
	private static final Logger LOGGER = LogManager.getLogger(RustUtil.class);
	
	public static BlobList convertToList(BorstData data, int offset, int count) {
		int len = Math.max(0, Math.min(data.getBlobs().size(), offset + count) - offset);
		
		BlobList list = new BlobList();
		list.assign(data.getBlobs(), offset, len);
		return list;
	}
	
	/**
	 * Returns the amount of size and color changes between blobs.
	 */
	public static int getScore(BlobList list, int offset) {
		List<Blob> blobList = list.getList();
		Blob last = null;
		
		int changes = 4;
		for (int i = offset; i < blobList.size(); i++) {
			Blob blob = blobList.get(i);
			if (last != null) {
				changes += (last.size != blob.size) ? 1 : 0;
				changes += (last.color != blob.color) ? 1 : 0;
				changes += (last.alpha != blob.alpha) ? 1 : 0;
				changes += (last.shapeIndex != blob.shapeIndex) ? 1 : 0;
			}
			
			last = blob;
		}
		
		return changes;
	}
	
	public static void dumpInfo(BorstData data) {
		BlobList list = new BlobList();
		for (int i = 0, len = data.getBlobs().size(); i < len; i++) {
			list.add(data.getBlobs().get(i));
		}
		
		LOGGER.info("Size change: {} / {}", getScore(list, 0), getScore(BorstSorter.sort(list), 0));
	}

	public static int clamp(int value, int min, int max) {
		return value < min ? min : (value > max ? max : value);
	}
}
