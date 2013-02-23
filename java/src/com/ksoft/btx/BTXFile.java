package com.ksoft.btx;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
	
	public static void writeToFile(File dest, BTXObject... roots) throws IOException {
		try (DataOutputStream f = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(dest)))) {
			f.writeByte(0); // Version 0
			f.writeInt(roots.length);
			for (BTXObject o : roots) {
				BTXHelp_0.writeObject(f, o);
			}
		}
	}
}
