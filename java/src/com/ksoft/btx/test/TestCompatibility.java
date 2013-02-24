package com.ksoft.btx.test;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import com.ksoft.btx.Compatibility;

public class TestCompatibility {
	@Test
	public void testCompatibility() throws IOException, XMLStreamException {
		Random r = new Random(3983872092L);
		File fbtx = TestHelp.createRandomBTX(r, null, 1024, 1024);
		System.out.println("Made btx: " + fbtx.length());
		
		File fxml = new File(fbtx.getAbsolutePath() + ".xml");
		Compatibility.btxToXML(fbtx, fxml);
		System.out.println("Conv to xml: " + fxml.length());
		
		File fbtx2 = new File(fbtx.getAbsolutePath() + "2");
		Compatibility.xmlToBTX(fxml, fbtx2);
		System.out.println("Conv back to btx: " + fbtx2.length());
	}
}
