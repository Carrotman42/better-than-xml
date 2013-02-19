package com.ksoft.btx.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.ksoft.btx.BTXAttribute;
import com.ksoft.btx.BTXParser;
import com.ksoft.btx.BTXPusher;

public class TestBTXPusherParser {
	private final File testFile;
	public TestBTXPusherParser() throws IOException {
		testFile = File.createTempFile("asdfff", "");
		
		try (BTXPusher push = new BTXPusher(testFile)) {
			push.startObject("root1");
			push.startObject("sub 1 1");
			push.endObject();
			push.endObject();
			push.startObject("root2");
			push.startObject("sub 2 1");
			push.endObject();
			push.startObject("sub 2 2");
			push.endObject();
			push.endObject();
		}
		
		System.out.println(testFile);
	}
	
	@Test
	public void doTheTest() throws IOException {
		File dest = File.createTempFile("asdfff", "");
		try (BTXParser parse = new BTXParser(testFile); BTXPusher push = new BTXPusher(dest)) {
			l:
			while (true) {
				switch (parse.next()) {
					case ATTRIBUTE :
						BTXAttribute attr = parse.getEventData().getAttribute();
						byte[] buf = new byte[attr.getLength()];
						attr.fill(buf);
						push.addAttribute(attr.getName(), buf, buf.length);
						break;
					case START_OBJECT :
						push.startObject(parse.getEventData().objName);
						break;
					case END_OBJECT :
						push.endObject();
						break;
					case EOF :
						break l;
				}
			}
		}
		
		compareFiles(testFile, dest);
	}
	
	public static void compareFiles(File f1, File f2) throws IOException {
		assertEquals(f1.length(), f2.length());
		try (BufferedInputStream b1 = new BufferedInputStream(new FileInputStream(f1));
				BufferedInputStream b2 = new BufferedInputStream(new FileInputStream(f2))) {
			byte[] buf1 = new byte[1024 * 16], buf2 = new byte[1024 * 16];
			
			while (true) {
				boolean eof = readFully(b1, buf1);
				if (eof) {
					break;
				}
				readFully(b2, buf2);
				assertArrayEquals(buf1, buf2);
			}
		}
	}
	
	private static boolean readFully(InputStream in, byte[] buf) throws IOException {
		int len = buf.length;
		int pos = 0;
		while (pos < len) {
			int read = in.read(buf, pos, len - pos);
			if (read == -1) {
				return true;
			}
			pos += read;
		}
		return false;
	}
}
