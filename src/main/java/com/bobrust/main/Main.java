package com.bobrust.main;

import java.util.List;

import com.bobrust.generator.*;
import com.bobrust.generator.BorstGenerator.BorstData;
import com.bobrust.generator.sorter.Blob;
import com.bobrust.generator.sorter.BlobList;
import com.bobrust.generator.sorter.BorstSorter;
import com.bobrust.gui.BobRustEditor;
import com.bobrust.logging.LogUtils;

public class Main {
	public static void main(String[] args) {
		new BobRustEditor();
	}
	
	public static void dumpInfo(BorstData data) {
		Model model = data.getModel();
		
		BlobList list = new BlobList();
		for(int i = 0, len = model.colors.size(); i < len; i++) {
			Circle circle = model.shapes.get(i);
			BorstColor color = model.colors.get(i);
			list.add(Blob.get(circle.x, circle.y, circle.r, color.rgb));
		}
		
		LogUtils.info("Size change: %d / %d", score(list), score(BorstSorter.sort(list)));
	}
	
	public static int score(BlobList data) {
		if(data == null) return 0;
		return score(data.getList(), data.size());
	}
	
	public static int score(BlobList data, int shapes) {
		if(data == null) return 0;
		return score(data.getList(), shapes);
	}
	
	public static int score(List<Blob> list, int shapes) {
		Blob last = null;
		
		int index = 0;
		int changes = 2;
		for(Blob blob : list) {
			if(index++ > shapes) break;
			if(last != null) {
				if(last.size != blob.size) changes++;
				if(last.color != blob.color) changes++;
			}
			
			last = blob;
		}
		
		return changes;
	}
}
