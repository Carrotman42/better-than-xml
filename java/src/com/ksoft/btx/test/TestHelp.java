package com.ksoft.btx.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.ksoft.btx.BTXPusher;
import com.ksoft.util.DummyInputStream;

public class TestHelp {
	static enum state {
		START_OBJ, START_ATTR, DO_CHILDREN, END_CHILDREN;
	}
	static File createRandomBTX(Random r, File output, int objSize, int attrLen) throws IOException {
		if (output == null) {
			output = File.createTempFile("random-btx-file", ".carrots");
		}
		DummyInputStream dum = new DummyInputStream();
		ArrayList<state> stack = new ArrayList<>();
		state cur = state.DO_CHILDREN;
		int maxlen = r.nextInt(objSize) + objSize;
		int len = 0;
		try (BTXPusher p = new BTXPusher(output)) {
			while (len < maxlen) {
				switch (cur) {
					case START_ATTR :
						if (r.nextFloat() > 0.90) {
							cur = state.DO_CHILDREN;
						} else {
							p.addAttribute("attrnname" + r.nextDouble(), dum, r.nextInt(attrLen)
																				+ attrLen);
						}
						break;
					case DO_CHILDREN :
						if (r.nextFloat() > 0.90 - stack.size() * 0.05) {
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
		return output;
	}
}
