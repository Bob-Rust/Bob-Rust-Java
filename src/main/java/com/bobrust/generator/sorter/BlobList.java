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
	
	public void assign(List<Blob> list, int offset, int count) {
		this.list.clear();
		this.list.addAll(list.subList(offset, offset + count));
	}
	
	public List<Blob> getList() {
		return list;
	}
}
