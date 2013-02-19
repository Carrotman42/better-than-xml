package com.ksoft.btx.test;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.ksoft.btx.BTXEvent;
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
	public void t() throws IOException {
		try (BTXParser parse = new BTXParser(testFile)) {
			BTXEvent ev;
			String indent = "";
			while ((ev = parse.next()) != BTXEvent.EOF) {
				switch (ev) {
					case ATTRIBUTE :
						System.out.println(indent
											+ "Attribute: " + parse.getEventData().getAttribute());
						break;
					case START_OBJECT :
						indent += "\t";
						System.out.println(indent + "Object: " + parse.getEventData().objName);
						break;
					case END_OBJECT :
						indent = indent.substring(1);
						break;
					default :
						System.out.println(indent + ev);
						break;
				}
			}
		}
	}
}
