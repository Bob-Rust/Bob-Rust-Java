package com.bobrust.generator.sorter;

import java.util.Arrays;

class IntList {
	private int[] array;
	private int size;
	
	public IntList() {
		this.array = new int[16];
	}
	
	public int size() {
		return size;
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public int get(int index) {
		return array[index];
	}
	
	public void reverse() {
		int[] copy = toArray();
		for(int i = 0; i < size; i++) {
			array[i] = copy[size - i - 1];
		}
	}
	
	private int[] grow(int length) {
		int old_length = array.length;
		return array = Arrays.copyOf(array, Math.max(length - old_length, old_length >> 1) + old_length);
	}
	
	public void add(int value) {
		if(size == array.length) array = grow(size + 1);
		array[size++] = value;
	}
	
	public void remove(int index) {
		final int[] arr = array;
		
		final int new_size = size - 1;
		if(new_size > index) {
			System.arraycopy(arr, index + 1, arr, index, new_size - index);
		}
		
		size = new_size;
	}
	
	public void sort() {
		Arrays.sort(array, 0, size);
	}
	
	public void popLast() {
		size--;
	}
	
	public int[] toArray() {
		return Arrays.copyOf(array, size);
	}
	
	private static final IntList EMPTY_LIST = new IntList() {
		@Override
		public int get(int index) {
			return 0;
		}
		
		@Override
		public void add(int value) {
			
		}
		
		@Override
		public void remove(int index) {
			
		}
		
		@Override
		public int size() {
			return 0;
		}
		
		@Override
		public boolean isEmpty() {
			return true;
		}
	};
	
	public static final IntList emptyList() {
		return EMPTY_LIST;
	}
}
