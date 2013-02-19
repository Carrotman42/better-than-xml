package com.ksoft.btx;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BTXPusher implements Closeable {
	protected final RandomAccessFile f;
	
	protected int depth = 0;
	protected BTXEvent cur = null;
	
	public BTXPusher(File dest) throws IOException {
		f = new RandomAccessFile(dest, "rw");
		
		f.writeByte(0); // version
		eventData cur = new eventData(null);
		cur.attributeCountPos = -1;
		cur.childCountPos = f.getFilePointer();
		eventStack = cur;
		f.writeInt(0); // Dummy obj count
	}
	@Override
	public void close() throws IOException {
		while (eventStack != null) {
			endChildren();
		}
		f.close();
	}
	
	private static class eventData {
		final eventData par;
		
		long attributeCountPos;
		long childCountPos = -1;
		int attrCount;
		int childCount;
		
		eventData(eventData parent) {
			par = parent;
		}
	}
	
	protected eventData eventStack;
	public void startObject(String name) throws IOException {
		eventStack.childCount++;
		BTXHelp_0.writeString(f, name);
		eventStack = new eventData(eventStack);
		eventStack.attributeCountPos = f.getFilePointer();
		f.writeInt(0); // Dummy for attr count later
	}
	
	public void addAttribute(String name, String data) throws IOException {
		byte[] d = data.getBytes();
		addAttribute(name, d, d.length);
	}
	
	public void addAttribute(String name, byte[] data, int len) throws IOException {
		eventStack.attrCount++;
		BTXHelp_0.writeAttribute(f, name, data, len);
	}
	
	public void startChildren() throws IOException {
		// Done adding attributes
		eventStack.childCountPos = f.getFilePointer();
		f.seek(eventStack.attributeCountPos);
		f.writeInt(eventStack.attrCount);
		f.seek(eventStack.childCountPos);
		f.writeInt(0); // Dummy for child count later
	}
	
	public void endChildren() throws IOException {
		long savePos = f.getFilePointer();
		f.seek(eventStack.childCountPos);
		f.writeInt(eventStack.childCount);
		f.seek(savePos);
		
		eventStack = eventStack.par;
	}
}
