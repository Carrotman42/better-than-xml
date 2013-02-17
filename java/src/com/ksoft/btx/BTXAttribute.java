package com.ksoft.btx;

public interface BTXAttribute {
	String getName();
	int getLength();
	void fill(byte[] buf);
	boolean isNull();
}
