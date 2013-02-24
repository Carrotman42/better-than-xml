package com.ksoft.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class DummyInputStream extends InputStream {
	
	@Override
	public int read() throws IOException {
		return 'a';
	}
	
	@Override
	public int read(byte[] buf, int start, int len) {
		Arrays.fill(buf, start, start + len, (byte) 'a');
		return len;
	}
}
