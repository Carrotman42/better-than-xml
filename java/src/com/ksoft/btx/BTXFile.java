package com.ksoft.btx;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class BTXFile {
	public static enum EventType {
		EOF, OBJECT, ATTRIBUTE, STARTCHILDREN, ENDCHILDREN;
	}
	
	public static BTXObject[] readIntoMemory(File src) throws IOException {
		try (DataInputStream f = new DataInputStream(new BufferedInputStream(new FileInputStream(
				src)))) {
			int v = f.readByte();
			if (v != 0) {
				throw new UnsupportedOperationException("Unsupported BTX version: " + v);
			}
			int rootCount = f.readInt();
			BTXObject[] ret = new BTXObject[rootCount];
			for (int i = 0; i < rootCount; i++) {
				ret[i] = BTXHelp_0.readObject(f);
			}
			return ret;
		}
	}
	
	private final DataInput f;
	public BTXFile(File source, String mode) throws IOException {
		if (mode.contains("w")) {
			throw new UnsupportedOperationException("Read-only is currently only supported");
		}
		f = new RandomAccessFile(source, mode);
		
		int v = f.readByte();
		if (v != 0) {
			throw new UnsupportedOperationException("Unsupported BTX version: " + v);
		}
		
		// Prime the document iteration with the pseudo rootelement
		eventData cur = new eventData(null); // No name for psuedo rootelement
		// cur.attrsLeft = 0; // No attributes in root element, but this is set by default
		cur.objsLeft = BTXHelp_0.read32BitUnsigned(f);
		eventStack.add(cur);
	}
	
	private static class eventData {
		final String objName;
		int attrsLeft;
		int objsLeft = -1; // Representing that the value hasn't been filled yet
		
		MemoryBTXAttribute curAttr;
		
		eventData(String name) {
			objName = name;
		}
	}
	private final ArrayList<eventData> eventStack = new ArrayList<>();
	public EventType next() throws IOException {
		int s = eventStack.size() - 1;
		if (s == -1) {
			// If there's nothing left in the stack, we're at the end of the file!
			return EventType.EOF;
		}
		eventData cur = eventStack.get(s);
		
		if (cur.attrsLeft > 0) {
			cur.attrsLeft--;
			cur.curAttr = BTXHelp_0.readAttribute(f);
			return EventType.ATTRIBUTE;
		}
		if (cur.objsLeft == -1) {
			// We just ran out of ATTRIBUTEs and now we're at the number of child objects
			int objs;
			cur.objsLeft = objs = BTXHelp_0.read32BitUnsigned(f);
			if (objs > 0) { // There are children, signal that!
				return EventType.STARTCHILDREN;
			} else {
				// Else there are no children. To show that fact,
				// we will fall through and just return ENDCHILDREN
				// and remove this current event.
			}
		}
		if (cur.objsLeft == 0) {
			// Out of children and done with the current object!
			eventStack.remove(s);
			return EventType.ENDCHILDREN;
		} else { // cur.objsLeft > 0
			// Start reading a new object
			cur.objsLeft--;
			eventData next = new eventData(BTXHelp_0.readString(f));
			next.attrsLeft = BTXHelp_0.read32BitUnsigned(f);
			return EventType.OBJECT;
		}
	}
	
	public int getChildrenLeft() {
		return eventStack.get(eventStack.size() - 1).objsLeft;
	}
	public int getAttrsLeft() {
		return eventStack.get(eventStack.size() - 1).attrsLeft;
	}
	public String getObjectName() {
		return eventStack.get(eventStack.size() - 1).objName;
	}
	
	public BTXAttribute getLastAttribute() {
		return eventStack.get(eventStack.size() - 1).curAttr;
	}
	
	
	
}
