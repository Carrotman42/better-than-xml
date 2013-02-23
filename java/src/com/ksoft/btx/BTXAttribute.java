package com.ksoft.btx;

/**
 * If java 8 were out this would be an interface with default implementations, rather than a class.
 * 
 * @author Kevin Malachowski
 * 
 */
public abstract class BTXAttribute {
	public abstract String getName();
	public abstract int getLength();
	public abstract boolean isNull();
	
	public abstract void fill(byte[] dest, int srcStart, int dstStart, int length);
	
	public void fill(byte[] buf) {
		fill(buf, 0, 0, buf.length);
	}
	
	public String asString() {
		byte[] buf = new byte[getLength()];
		fill(buf);
		return new String(buf);
	}
	
	@Override
	public String toString() {
		return getName() + "=" + asString();
	}
}
