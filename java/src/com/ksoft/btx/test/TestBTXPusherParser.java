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
			push.startChildren();
			push.startObject("sub 1 1");
			push.startChildren();
			push.endChildren();
			push.endChildren();
			push.startObject("root2");
			push.startChildren();
			push.startObject("sub 2 1");
			push.startChildren();
			push.endChildren();
			push.startObject("sub 2 2");
			push.startChildren();
			push.endChildren();
			push.endChildren();
		}
		
		System.out.println(testFile);
	}
	
	@Test
	public void t() throws IOException {
		try (BTXParser parse = new BTXParser(testFile)) {
			BTXEvent ev;
			while ((ev = parse.next()) != BTXEvent.EOF) {
				switch (ev) {
					case ATTRIBUTE :
						System.out.println("Attribute: " + parse.getLastAttribute());
						break;
					case OBJECT :
						System.out.println("Object: " + parse.getObjectName());
						break;
					default :
						System.out.println(ev);
						break;
				}
			}
		}
	}
}
