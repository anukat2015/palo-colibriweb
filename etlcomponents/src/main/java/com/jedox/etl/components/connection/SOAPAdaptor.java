/**
*   @brief <Description of Class>
*
*   @file
*
*   Copyright (C) 2008-2014 Jedox AG
*
*   This program is free software; you can redistribute it and/or modify it
*   under the terms of the GNU General Public License (Version 2) as published
*   by the Free Software Foundation at http://www.gnu.org/copyleft/gpl.html.
*
*   This program is distributed in the hope that it will be useful, but WITHOUT
*   ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
*   FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
*   more details.
*
*   You should have received a copy of the GNU General Public License along with
*   this program; if not, write to the Free Software Foundation, Inc., 59 Temple
*   Place, Suite 330, Boston, MA 02111-1307 USA
*
*   If you are developing and distributing open source applications under the
*   GPL License, then you are free to use Palo under the GPL License.  For OEMs,
*   ISVs, and VARs who distribute Palo with their products, and do not license
*   and distribute their source code under the GPL, Jedox provides a flexible
*   OEM Commercial License.
*
* 	Portions of the code developed by proclos OG, Wien on behalf of Jedox AG.
* 	Intellectual property rights for these portions has proclos OG Wien, 
* 	or otherwise Jedox AG, Freiburg. Exclusive worldwide exploitation right 
* 	(commercial copyright) has Jedox AG, Freiburg.
*
*   @author Christian Schwarzinger, proclos OG, Wien, Austria
*   @author Andreas Fröhlich, Jedox AG, Freiburg, Germany
*   @author Kais Haddadin, Jedox AG, Freiburg, Germany
*/
package com.jedox.etl.components.connection;

import java.net.URL;
import java.util.Map;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import com.jedox.etl.components.config.connection.ServiceDescriptor;
import com.jedox.etl.core.component.RuntimeException;

public class SOAPAdaptor {

	private ServiceDescriptor descriptor;
	private Service service;
	private static final Log log = LogFactory.getLog(SOAPAdaptor.class);

	public SOAPAdaptor(ServiceDescriptor descriptor) throws RuntimeException {
		
		this.descriptor = descriptor;
		try {
			QName serviceName = new QName(descriptor.getTargetNamespace(),descriptor.getServiceName());
			//endpoint url has to be in wsdl file!!
			URL url =  new URL(descriptor.getWsdlURL());
			service = Service.create(url,serviceName);
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}

	}
	
	private SOAPElement createParameter(ServiceDescriptor.MethodParameter parameter, SOAPElement parent) throws SOAPException {
		QName qName = new QName(descriptor.getTargetNamespace(),parameter.getName());
		SOAPElement value = parent.addChildElement(qName);
    	if (parameter.getParameters().isEmpty()) {
    		if (parameter.getValue() == null) {
    			value.addAttribute(new QName("http://www.w3.org/2001/XMLSchema-instance","nil","xsi"),"1");
    		} else {
    			value.setTextContent(parameter.getValue());
    		}
    	}
    	else {
    		for (ServiceDescriptor.MethodParameter p: parameter.getParameters()) {
    			createParameter(p, value);
    		}
    	}
    	return value;
	}
	
	private SOAPElement createPayLoad(SOAPElement body,  QName methodName) throws SOAPException, RuntimeException {
        SOAPElement method = body.addChildElement(methodName);
        for (ServiceDescriptor.MethodParameter parameter: descriptor.getParameter()) {
        	createParameter(parameter,method);
		}
        return method;
	}
  
	public Document execute(String username, String password) throws RuntimeException {
		QName portName = new QName(descriptor.getTargetNamespace(),descriptor.getPortName());
		QName operationName = new QName(descriptor.getTargetNamespace(),descriptor.getOperationName());
		/** Create a Dispatch instance from a service.**/ 
		String namespace = portName.getNamespaceURI();
		// for a reason, some namespaces are stored without / at the end, for our purpose they always should have this
		if(!namespace.endsWith("/"))
			namespace = namespace.concat("/");
		
		Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Service.Mode.MESSAGE);
		Map<String, Object>  map = dispatch.getRequestContext();
		map.put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
		map.put(BindingProvider.SOAPACTION_URI_PROPERTY,namespace + operationName.getLocalPart());
		map.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, descriptor.getServiceUrl() != null ? descriptor.getServiceUrl() : descriptor.getWsdlURL());
		map.put(BindingProvider.USERNAME_PROPERTY,username);
		map.put(BindingProvider.PASSWORD_PROPERTY, password);
		map.put(MessageContext.WSDL_OPERATION, operationName);
		try {     
			/** Create SOAPMessage request. **/
			// compose a request message
			MessageFactory mf = descriptor.getSoapVersion() != null ? MessageFactory.newInstance(descriptor.getSoapVersion()) : MessageFactory.newInstance();

			// Create a message.  This example works with the SOAPPART.
			SOAPMessage request = mf.createMessage();

			SOAPPart part = request.getSOAPPart();

			// Obtain the SOAPEnvelope and header and body elements.
			SOAPEnvelope env = part.getEnvelope();
			SOAPBody body = env.getBody();
			SOAPHeader header = env.getHeader();
			if (header == null) {
                header = env.addHeader();
            }
			//add header
			
			for (ServiceDescriptor.MethodParameter parameter : descriptor.getHeaderParameter()) {
				QName qName = new QName(descriptor.getTargetNamespace(),parameter.getName());
				SOAPElement value = header.addHeaderElement(qName);
		    	if (parameter.getParameters().isEmpty()) {
		    		value.setTextContent(parameter.getValue());
		    	} else {
		    		for (ServiceDescriptor.MethodParameter p: parameter.getParameters()) {
		    			createParameter(p, value);
		    		}
		    	}
				log.debug("Added to Header:" + value);
			}

			createPayLoad(body, operationName);
			
			request.saveChanges();

			/** Invoke the service endpoint. **/
			Source response = dispatch.invoke(request.getSOAPPart().getContent());
			
			
			DOMResult r = new DOMResult();
			TransformerFactory.newInstance().newTransformer().transform(response,r);
			Document document = (Document)r.getNode();
			convertTextContentToXML(document, document.getDocumentElement());
			
			if(descriptor.getXmlInfo()){
				TransformerFactory transFact = TransformerFactory.newInstance();
				Transformer trans = transFact.newTransformer();
				StringWriter sw = new StringWriter();
				StreamResult result = new StreamResult(sw);
				trans.transform(new DOMSource(document), result);
				String encodedXML = sw.toString();
				int blockPart = 1;
				int startIndex = 0;
				int blockSize = 800;
				int endIndex = Math.min(encodedXML.length(), blockSize);
				log.info("Starting printing Webservice output.");
				log.info("Webservice output part" + blockPart + ":\n" + encodedXML.substring(startIndex,endIndex));
				while(endIndex < encodedXML.length()){
					startIndex = endIndex;
					blockSize += 800;
					blockPart++;
					endIndex = Math.min(encodedXML.length(), blockSize);
					log.info("Webservice output part" + blockPart + ":\n" + encodedXML.substring(startIndex,endIndex));
				}
				log.info("Finished printing Webservice output.");
			}
			
			return document;

		}
		catch (SOAPException|TransformerException e) {
			throw new RuntimeException("Soap fault: "+e.getMessage());
		} 
	}
	
	private Document loadXMLFromString(String xml) 
	{
		try {
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    InputSource is = new InputSource(new StringReader(xml));
		    builder.setErrorHandler(null);
		    return builder.parse(is);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	private void convertTextContentToXML(Document document, Element element) {
		NodeList children = element.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node child = children.item(i);
			if (child instanceof Element) {
				convertTextContentToXML(document, (Element)child);
			}
			if (child instanceof Text) {
				String content = child.getTextContent();
				if (!StringUtils.isEmpty(content)) {
					String decodedContent = StringEscapeUtils.unescapeXml(content);
					Document inlineDocument = loadXMLFromString(decodedContent);
					if (inlineDocument != null) {
						Node importedNode = document.adoptNode(inlineDocument.getDocumentElement());
						element.appendChild(importedNode);
						element.removeChild(child);
					}
				}
			}
		}	
	}

}
