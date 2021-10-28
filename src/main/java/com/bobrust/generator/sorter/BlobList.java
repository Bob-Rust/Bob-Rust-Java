package com.bobrust.generator.sorter;

import java.util.*;

public class BlobList {
	private final List<Blob> list;
	
	public BlobList() {
		list = new ArrayList<>();
	}
	
	public BlobList(Collection<Blob> collection) {
		list = new ArrayList<>(collection);
	}
	
	public int size() {
		return list.size();
	}
	
	public Blob get(int index) {
		return list.get(index);
	}
	
	public void add(Blob blob) {
		list.add(blob);
	}
	
	public List<Blob> list() {
		return list;
	}
	
//	public static BlobList populate(int length) {
//		Random random = new Random(0);
//		BlobList list = new BlobList();
//		for(int i = 0; i < length; i++) {
//			list.add(Blob.get(
//				random.nextInt(512),
//				random.nextInt(512),
//				BorstUtils.SIZES[random.nextInt(BorstUtils.SIZES.length)],
//				BorstUtils.COLORS[random.nextInt(BorstUtils.COLORS.length)].getRgba()
//			));
//		}
//		
//		return list;
//	}
}
