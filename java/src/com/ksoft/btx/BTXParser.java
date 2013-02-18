package com.ksoft.btx;

import java.io.Closeable;
import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class BTXParser implements Closeable {
	public static enum EventType {
		EOF, OBJECT, ATTRIBUTE, STARTCHILDREN, ENDCHILDREN;
	}
	
	private final Closeable cl;
	private final DataInput f;
	
	protected void init() throws IOException {
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
	
	
	public BTXParser(File source) throws IOException {
		RandomAccessFile fi = new RandomAccessFile(source, "r");
		f = fi;
		cl = fi;
		
		init();
	}
	
	public BTXParser(DataInput src) throws IOException {
		f = src;
		cl = null; // Can't close this
		init();
	}
	
	public <E extends Closeable & DataInput> BTXParser(E src) throws IOException {
		f = src;
		cl = src;
		init();
	}
	
	@Override
	public void close() throws IOException {
		if (cl != null) {
			cl.close();
		}
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
