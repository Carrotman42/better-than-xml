package com.ksoft.btx;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BTXParser implements Closeable {
	protected final DataInput f;
	protected final Closeable cl;
	
	public static void dump(File f) throws IOException {
		try (BTXParser p = new BTXParser(f)) {
			String indent = "";
			l:
			while (true) {
				switch (p.next()) {
					case ATTRIBUTE : {
						BTXAttribute at = p.getEventData().getAttribute();
						byte[] buf = new byte[at.getLength()];
						at.fill(buf);
						System.out.println(indent + at.getName() + "=" + new String(buf));
						break;
					}
					case END_OBJECT :
						indent = indent.substring(2);
						break;
					case EOF :
						break l;
					case START_OBJECT :
						System.out.println(indent + p.getEventData().objName);
						indent += "  ";
						break;
					default :
						break;
				
				}
			}
		}
	}
	
	protected void init() throws IOException {
		int v = f.readByte();
		if (v != 0) {
			throw new UnsupportedOperationException("Unsupported BTX version: " + v);
		}
		
		// Prime the document iteration with the pseudo rootelement
		eventStack = new ParseEventData(null, null, // No name nor parent for psuedo rootelement
				0, BTXHelp_0.read32BitUnsigned(f)); // No attrs either
	}
	
	
	public BTXParser(File source) throws IOException {
		this(new DataInputStream(new BufferedInputStream(new FileInputStream(source))));
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
	
	public static class ParseEventData {
		final ParseEventData par;
		public final String objName;
		public final boolean hasChildren;
		int attrsLeft;
		int objsLeft;
		
		MemoryBTXAttribute curAttr;
		
		public BTXAttribute getAttribute() {
			return curAttr;
		}
		
		public int getRemainingAttributes() {
			return attrsLeft;
		}
		
		public int getRemainingChildren() {
			return objsLeft;
		}
		
		ParseEventData(ParseEventData parent, String name, int attrs, int objs) {
			par = parent;
			objName = name;
			attrsLeft = attrs;
			objsLeft = objs;
			hasChildren = objs > 0;
		}
	}
	
	protected ParseEventData eventStack;
	public BTXEvent next() throws IOException {
		ParseEventData cur = eventStack;
		if (cur == null) {
			// If there's nothing left in the stack, we're at the end of the file!
			return BTXEvent.EOF;
		}
		
		if (cur.attrsLeft > 0) {
			cur.attrsLeft--;
			cur.curAttr = BTXHelp_0.readAttribute(f);
			return BTXEvent.ATTRIBUTE;
		}
		if (cur.objsLeft == 0) {
			// Out of children and done with the current object!
			eventStack = eventStack.par;
			return eventStack == null ? BTXEvent.EOF : BTXEvent.END_OBJECT;
		} else { // cur.objsLeft > 0
			// Start reading a new object
			eventStack.curAttr = null; // Clear last curAttr
										// (don't need to do this really, but we should)
			cur.objsLeft--;
			eventStack = new ParseEventData(eventStack, // push it to the top of the stack
					BTXHelp_0.readString(f), // Name
					BTXHelp_0.read32BitUnsigned(f), // Attribute count
					BTXHelp_0.read32BitUnsigned(f)); // Object count
			return BTXEvent.START_OBJECT;
		}
	}
	
	public ParseEventData getEventData() {
		return eventStack; // Return the current event
	}
}
