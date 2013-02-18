package com.ksoft.btx;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BTXFile {
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
}
