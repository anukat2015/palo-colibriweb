package com.proclos.colibriweb.session.system;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;

public class XMLUtil {

	public static String jdomToString(Element element) throws IOException {
		Element root = (Element) element.clone();
		StringWriter writer = new StringWriter();
		Document document = new Document();
		document.setRootElement(root);
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		//XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
		outputter.output(document, writer);
		return writer.toString();
	}

	public static Element stringTojdom(String xmlString) throws IOException, JDOMException {
		Document doc = new SAXBuilder().build(new StringReader(xmlString));
		return doc.getRootElement();
	}
}
