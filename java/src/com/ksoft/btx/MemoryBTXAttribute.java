package com.ksoft.btx;

public class MemoryBTXAttribute extends BTXAttribute {
	private final String name;
	private final byte[] data;
	public MemoryBTXAttribute(String name, byte[] data) {
		this.name = name;
		this.data = data;
	}
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public int getLength() {
		return data.length;
	}
	
	@Override
	public boolean isNull() {
		return data == null;
	}
	@Override
	public void fill(byte[] dest, int srcStart, int dstStart, int length) {
		System.arraycopy(data, srcStart, dest, dstStart, length);
	}
}
