package com.ksoft.btx.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;

import com.ksoft.btx.BTXAttribute;
import com.ksoft.btx.BTXParser;
import com.ksoft.btx.BTXPusher;

public class TestBTXPusherParser {
	private final Random r;
	public TestBTXPusherParser() throws IOException {
		r = new Random(86753090L);
	}
	
	
	enum state {
		START_OBJ, START_ATTR, DO_CHILDREN, END_CHILDREN;
	}
	static void createRandomBTX(Random r, File output) throws IOException {
		ArrayList<state> stack = new ArrayList<>();
		state cur = state.DO_CHILDREN;
		int maxlen = r.nextInt(1024 * 8) + 1024 * 4;
		int len = 0;
		try (BTXPusher p = new BTXPusher(output)) {
			while (len < maxlen) {
				switch (cur) {
					case START_ATTR :
						if (r.nextFloat() > 0.90) {
							cur = state.DO_CHILDREN;
						} else {
							byte[] buf = new byte[r.nextInt(1024) + 1024];
							r.nextBytes(buf);
							p.addAttribute("attrnname" + r.nextDouble(), buf, buf.length);
							len++;
						}
						break;
					case DO_CHILDREN :
						if (r.nextFloat() > 0.90 - stack.size() * 0.5) {
							cur = state.END_CHILDREN;
						} else {
							cur = state.START_OBJ;
						}
						break;
					case START_OBJ :
						stack.add(cur);
						cur = state.START_ATTR;
						p.startObject("objname" + r.nextLong());
						len++;
						break;
					case END_CHILDREN :
						if (stack.size() == 0) {
							cur = state.DO_CHILDREN;
						} else {
							cur = stack.remove(stack.size() - 1);
						}
						break;
					default :
						break;
				}
			}
		}
	}
	
	@Test
	public void testMultipleTimes() throws IOException {
		for (int i = 0; i < 10; i++) {
			File src = File.createTempFile("TestBTXPusherParser-" + i + "-src-", "");
			File dest = File.createTempFile("TestBTXPusherParser-" + i + "-dest-", "");
			createRandomBTX(r, src);
			doTheTest(src, dest);
			System.out.println("Did test " + i + "; file size: " + src.length());
			src.delete();
			dest.delete();
		}
	}
	
	static void doTheTest(File infile, File outfile) throws IOException {
		try (BTXParser parse = new BTXParser(infile); BTXPusher push = new BTXPusher(outfile)) {
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
		
		compareFiles(infile, outfile);
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
