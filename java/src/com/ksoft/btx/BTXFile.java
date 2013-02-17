package com.ksoft.btx;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BTXFile implements AutoCloseable {
	public static enum EventType {
		SOF, EOF, OBJECT, ATTRIBUTE, CHILDREN, ENDCHILDREN;
	}
	private final RandomAccessFile f;
	public BTXFile(File source, String mode) throws IOException {
		if (mode.contains("w")) {
			throw new UnsupportedOperationException("Read-only is currently only supported");
		}
		f = new RandomAccessFile(source, mode);
		
		int v = f.readByte();
		if (v != 0) {
			throw new UnsupportedOperationException("Unsupported BTX version: " + v);
		}
	}
	
	@Override
	public void close() throws IOException {
		f.close();
	}
	
	public static BTXObject[] readIntoMemory(File src) throws IOException {
		try (BTXFile f = new BTXFile(src, "r")) {
			int rootCount = f.f.readInt();
			BTXObject[] ret = new BTXObject[rootCount];
			for (int i = 0; i < rootCount; i++) {
				ret[i] = BTXHelp_0.readObject_0(f.f);
			}
			return ret;
		}
	}
	
}
