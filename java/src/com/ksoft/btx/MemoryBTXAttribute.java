package com.ksoft.btx;

public class MemoryBTXAttribute implements BTXAttribute {
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
	public void fill(byte[] buf) {
		System.arraycopy(data, 0, buf, 0, data.length);
	}
	
	@Override
	public boolean isNull() {
		return data == null;
	}
}
