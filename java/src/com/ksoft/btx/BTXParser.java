package com.ksoft.btx;

import java.io.Closeable;
import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BTXParser implements Closeable {
	protected final DataInput f;
	protected final Closeable cl;
	
	protected void init() throws IOException {
		int v = f.readByte();
		if (v != 0) {
			throw new UnsupportedOperationException("Unsupported BTX version: " + v);
		}
		
		// Prime the document iteration with the pseudo rootelement
		eventData cur = new eventData(null, null); // No name nor parent for psuedo rootelement
		// cur.attrsLeft = 0; // No attributes in root element, but this is set by default
		cur.objsLeft = BTXHelp_0.read32BitUnsigned(f);
		eventStack = cur;
	}
	
	
	public BTXParser(File source) throws IOException {
		this(new RandomAccessFile(source, "r"));
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
		final eventData prev;
		final String objName;
		int attrsLeft;
		int objsLeft = -1; // Representing that the value hasn't been filled yet
		
		MemoryBTXAttribute curAttr;
		
		eventData(eventData p, String name) {
			prev = p;
			objName = name;
		}
	}
	protected eventData eventStack;
	public BTXEvent next() throws IOException {
		eventData cur = eventStack;
		if (cur == null) {
			// If there's nothing left in the stack, we're at the end of the file!
			return BTXEvent.EOF;
		}
		
		if (cur.attrsLeft > 0) {
			cur.attrsLeft--;
			cur.curAttr = BTXHelp_0.readAttribute(f);
			return BTXEvent.ATTRIBUTE;
		}
		if (cur.objsLeft == -1) {
			// We just ran out of ATTRIBUTEs and now we're at the number of child objects
			int objs;
			cur.objsLeft = objs = BTXHelp_0.read32BitUnsigned(f);
			if (objs > 0) { // There are children, signal that!
				return BTXEvent.STARTCHILDREN;
			} else {
				// Else there are no children. To show that fact,
				// we will fall through and just return ENDCHILDREN
				// and remove this current event.
			}
		}
		if (cur.objsLeft == 0) {
			// Out of children and done with the current object!
			eventStack = eventStack.prev;
			return BTXEvent.ENDCHILDREN;
		} else { // cur.objsLeft > 0
			// Start reading a new object
			cur.objsLeft--;
			eventData next = new eventData(eventStack, BTXHelp_0.readString(f));
			next.attrsLeft = BTXHelp_0.read32BitUnsigned(f);
			eventStack = next; // put it as the top of the stack
			return BTXEvent.OBJECT;
		}
	}
	
	public int getChildrenLeft() {
		return eventStack.objsLeft;
	}
	public int getAttrsLeft() {
		return eventStack.attrsLeft;
	}
	public String getObjectName() {
		return eventStack.objName;
	}
	
	public BTXAttribute getLastAttribute() {
		return eventStack.curAttr;
	}
}
