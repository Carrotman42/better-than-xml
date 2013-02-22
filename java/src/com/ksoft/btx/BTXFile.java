package com.ksoft.btx;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

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
	
	public static void xmlToBTX(File fin, File fout) throws XMLStreamException, IOException {
		XMLInputFactory fact = XMLInputFactory.newFactory();
		try (FileReader fr = new FileReader(fin); BTXPusher out = new BTXPusher(fout)) {
			XMLEventReader in = fact.createXMLEventReader(fr);
			
			XMLEvent s;
			while (!(s = in.nextEvent()).isEndDocument()) {
				if (!s.isStartDocument()) {
					if (s.isStartElement()) {
						StartElement st = (StartElement) s;
						out.startObject(st.getName().toString());
						@SuppressWarnings({"cast", "unchecked"})
						Iterator<Attribute> it = (Iterator<Attribute>) st.getAttributes();
						while (it.hasNext()) {
							Attribute at = it.next();
							out.addAttribute(at.getName().toString(), at.getValue());
						}
					} else if (s.isEndElement()) {
						out.endObject();
					} else if (s.isCharacters()) {
						Characters ch = (Characters) s;
						if (!ch.isWhiteSpace()) {
							out.startObject("text");
							out.addAttribute("t", ch.getData());
							out.endObject();
						}
					}
				}
			}
		}
	}
	
	public static void main(String... asdf) throws XMLStreamException, IOException {
		xmlToBTX(new File("d.xml"), new File("d.btx"));
		BTXParser.dump(new File("d.btx"));
	}
}
