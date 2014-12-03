/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.proclos.palosubsets;

/**
 *
 * @author chris
 */
import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.JDOMException;
import org.jdom2.output.XMLOutputter;

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
