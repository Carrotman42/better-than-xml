package com.ksoft.btx;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.ksoft.btx.BTXParser.ParseEventData;

public class Compatibility {
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
							out.startObject("<t");
							out.addAttribute("", ch.getData());
							out.endObject();
						}
					}
				}
			}
		}
	}
	
	private static void btxToXML_help(BTXParser in, XMLEventFactory ev, XMLEventWriter out)
			throws IOException, XMLStreamException {
		in.next(); // START_OBJECT
		ParseEventData d = in.getEventData();
		if (d.objName.equals("<t")) {
			// Special for text type!
			// Single unnamed attribute for the text
			in.next(); // ATTRIBUTE
			out.add(ev.createCData(in.getEventData().getAttribute().asString()));
			in.next(); // END_OBJECT
			return;
		}
		int atCount = d.getRemainingAttributes();
		Iterator<Attribute> theAttrs;
		if (atCount == 0) {
			theAttrs = null;
		} else {
			ArrayList<Attribute> attrs = new ArrayList<>(d.getRemainingAttributes());
			do {
				in.next(); // ATTRIBUTE
				BTXAttribute attr = in.getEventData().getAttribute();
				attrs.add(ev.createAttribute(attr.getName(), attr.asString()));
				atCount--;
			} while (atCount > 0);
			theAttrs = attrs.iterator();
		}
		QName objName = new QName(d.objName);
		out.add(ev.createStartElement(objName, theAttrs, null));
		atCount = d.getRemainingChildren();
		while (atCount > 0) {
			atCount--;
			btxToXML_help(in, ev, out);
		}
		out.add(ev.createEndElement(objName, null));
		in.next(); // END_OBJECT
	}
	
	public static void btxToXML(File fin, File fout) throws IOException, XMLStreamException {
		XMLOutputFactory fact = XMLOutputFactory.newFactory();
		XMLEventFactory ev = XMLEventFactory.newFactory();
		try (FileWriter fw = new FileWriter(fout); BTXParser in = new BTXParser(fin)) {
			XMLEventWriter out = fact.createXMLEventWriter(fw);
			out.add(ev.createStartDocument());
			QName rname = new QName("ROOT");
			out.add(ev.createStartElement(rname, null, null));
			btxToXML_help(in, ev, out);
			out.add(ev.createEndElement(rname, null));
			out.add(ev.createEndDocument());
		}
	}
}
