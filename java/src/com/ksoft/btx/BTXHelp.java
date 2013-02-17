package com.ksoft.btx;

import java.io.DataInput;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class BTXHelp {
	public interface Translator<Out> {
		Out translate(byte[] in, int len);
	}
	
	public static <E> void fillAttributeMap(BTXObject source, Map<String, E> dest, Translator<E> fac) {
		byte[] buffer = new byte[1024]; // Probably enough
		Iterator<BTXAttribute> it = source.getAttributes();
		while (it.hasNext()) {
			BTXAttribute cur = it.next();
			int len = cur.getLength();
			if (buffer.length < len) {
				buffer = new byte[len]; // Make a bigger buffer
			}
			cur.fill(buffer);
			dest.put(cur.getName(), fac.translate(buffer, len));
		}
	}
	public static void fillAttributeMap(BTXObject source, Map<String, String> dest) {
		byte[] buffer = new byte[1024]; // Probably enough
		Iterator<BTXAttribute> it = source.getAttributes();
		while (it.hasNext()) {
			BTXAttribute cur = it.next();
			int len = cur.getLength();
			if (buffer.length < len) {
				buffer = new byte[len]; // Make a bigger buffer
			}
			cur.fill(buffer);
			dest.put(cur.getName(), new String(buffer, 0, len));
		}
	}
}

class BTXHelp_0 {
	static MemoryBTXObject readObject_0(DataInput in) throws IOException {
		String name = new String(readRunLenBytes(in));
		MemoryBTXObject ret = new MemoryBTXObject(name);
		int count = read32BitUnsigned(in);
		while (count > 0) {
			String atrrName = new String(readRunLenBytes(in));
			byte meta = in.readByte();
			byte[] data;
			if (meta == 0) {
				data = null;
			} else {
				data = readRunLenBytes(in);
			}
			ret.attrs.add(new MemoryBTXAttribute(atrrName, data));
		}
		count = read32BitUnsigned(in);
		while (count > 0) {
			ret.children.add(readObject_0(in));
		}
		return ret;
	}
	
	public static int read32BitUnsigned(DataInput in) throws IOException {
		int len = in.readInt();
		if (len < 0) {
			throw new JavaDoesntHaveUnsigned32BitArrays();
		}
		return len;
	}
	
	public static byte[] readRunLenBytes(DataInput in) throws IOException {
		int len = read32BitUnsigned(in);
		byte[] buf = new byte[len];
		in.readFully(buf);
		return buf;
	}
}
