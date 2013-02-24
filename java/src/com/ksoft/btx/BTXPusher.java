package com.ksoft.btx;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class BTXPusher implements Closeable {
	protected final RandomAccessFile f;
	
	protected int depth = 0;
	protected BTXEvent cur = null;
	
	public BTXPusher(File dest) throws IOException {
		f = new RandomAccessFile(dest, "rw");
		
		f.writeByte(0); // version
		eventData cur = new eventData(null);
		cur.metaPos = f.getFilePointer();
		eventStack = cur;
		f.writeInt(0); // Dummy obj count
	}
	@Override
	public void close() throws IOException {
		while (eventStack != null) {
			endObject();
		}
		f.close();
	}
	
	private static class eventData {
		final eventData par;
		
		long metaPos;
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
		eventStack.metaPos = f.getFilePointer();
		f.writeInt(0); // Dummy for attr count later
		f.writeInt(0); // Dummy for child count later
	}
	
	public void addAttribute(String name, String data) throws IOException {
		byte[] d = data.getBytes();
		addAttribute(name, d, d.length);
	}
	
	public void addAttribute(String name, InputStream from, int len) throws IOException {
		eventStack.attrCount++;
		BTXHelp_0.writeAttribute(f, name, from, len);
	}
	
	public void addAttribute(String name, byte[] data, int len) throws IOException {
		eventStack.attrCount++;
		BTXHelp_0.writeAttribute(f, name, data, len);
	}
	
	public void endObject() throws IOException {
		eventData cur = eventStack;
		// Pop the next one off the stack
		eventStack = cur.par;
		
		long savePos = f.getFilePointer();
		f.seek(cur.metaPos);
		if (eventStack != null) {
			f.writeInt(cur.attrCount);
		} else {
			// Special case of top-level psuedo rootelement:
			// No attributes allowed
		}
		
		f.writeInt(cur.childCount);
		f.seek(savePos);
	}
}
