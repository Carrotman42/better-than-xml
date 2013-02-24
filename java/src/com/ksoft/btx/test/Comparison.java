package com.ksoft.btx.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.ksoft.btx.BTXAttribute;
import com.ksoft.btx.BTXEvent;
import com.ksoft.btx.BTXParser;
import com.ksoft.btx.Compatibility;

public class Comparison {
	static void p(Object s) {
		System.out.println(s);
	}
	static class Timer {
		final ArrayList<Long> marks = new ArrayList<>();
		final long start;
		public Timer() {
			start = System.nanoTime();
		}
		
		void mark() {
			marks.add(System.nanoTime());
		}
		
		void report(String lbl) {
			long end = System.nanoTime();
			p(lbl + ": Total Time: " + ((end - start) / 10000) / 100000.);
		}
	}
	
	static interface R {
		void r() throws Throwable;
	}
	
	public static void main(String... asdf) throws Throwable {
		final File seedBTX = TestHelp.createRandomBTX(new Random(234234), null, 1024 * 2, 100);
		final File seedXML = new File(seedBTX.getAbsolutePath() + ".xml");
		Compatibility.btxToXML(seedBTX, seedXML);
		System.out.println("FILE SIZES:\nBTX: " + seedBTX.length() + "\nXML: " + seedXML.length());
		
		for (int i = 0; i < 3; i++) {
			doTest("Iterate", new R() {
				@Override
				public void r() throws Throwable {
					iterateThroughBTX(seedBTX);
				}
			}, new R() {
				@Override
				public void r() throws Throwable {
					iterateThroughXML(seedXML);
				}
			});
			doTest("read all data", new R() {
				@Override
				public void r() throws Throwable {
					readBTX(seedBTX);
				}
			}, new R() {
				@Override
				public void r() throws Throwable {
					readXML(seedXML);
				}
			});
		}
	}
	
	static void doTest(String desc, R btx, R xml) throws Throwable {
		Timer t;
		
		p("--------------------------");
		p("Start test: " + desc);
		
		for (int r = 0; r < 3; r++) {
			t = new Timer();
			for (int i = 0; i < 50; i++) {
				btx.r();
				t.mark();
			}
			t.report("btx");
			
			t = new Timer();
			for (int i = 0; i < 50; i++) {
				xml.r();
				t.mark();
			}
			t.report("xml");
		}
	}
	
	
	final static XMLInputFactory fact = XMLInputFactory.newFactory();
	
	static void iterateThroughXML(File f) throws Throwable {
		XMLEventReader v = fact
				.createXMLEventReader(new BufferedInputStream(new FileInputStream(f)));
		
		while (v.hasNext()) {
			v.nextEvent();
		}
		
		v.close();
	}
	
	static void iterateThroughBTX(File f) throws Throwable {
		BTXParser v = new BTXParser(f);
		
		while (v.next() != BTXEvent.EOF) {
			;
		}
		
		v.close();
	}
	
	static void readXML(File f) throws Throwable {
		XMLEventReader v = fact
				.createXMLEventReader(new BufferedInputStream(new FileInputStream(f)));
		
		while (v.hasNext()) {
			XMLEvent ev = v.nextEvent();
			if (ev.isStartElement()) {
				StartElement s = (StartElement) ev;
				s.getName();
				@SuppressWarnings("unchecked")
				Iterator<Attribute> attrs = s.getAttributes();
				while (attrs.hasNext()) {
					Attribute at = attrs.next();
					at.getName();
					at.getValue();
				}
			}
		}
		
		v.close();
	}
	
	static void readBTX(File f) throws Throwable {
		BTXParser v = new BTXParser(f);
		
		l:
		while (true) {
			switch (v.next()) {
				case ATTRIBUTE :
					BTXAttribute at = v.getEventData().getAttribute();
					at.getName();
					at.asString();
					break;
				case END_OBJECT :
					break;
				default :
				case EOF :
					break l;
				case START_OBJECT :
					@SuppressWarnings("unused")
					// We still need to access the name to have apples to apples with xml
					String objName = v.getEventData().objName;
					break;
			}
		}
		
		v.close();
	}
}
