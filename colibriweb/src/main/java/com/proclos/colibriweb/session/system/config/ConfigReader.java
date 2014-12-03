 package com.proclos.colibriweb.session.system.config;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class ConfigReader
{
	private static final Log logger = LogFactory.getLog(ConfigReader.class);

	private String domain;
	private String configPath;
	private String schemaPath;

	public ConfigReader(String domain, String configPath, String schemaPath)
	{
		this.domain = domain;
		this.configPath = configPath;
		this.schemaPath = schemaPath;
	}

	public Document getConfigDocument() throws ModuleConfigException
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			Schema schema = getConfigSchema();
			factory.setSchema(schema);
			logger.info("Loading module configuration from:" + configPath);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setErrorHandler(new ErrorHandler());
			if(schema != null)
			{
				schema.newValidator().validate(new StreamSource(getConfigReader(configPath)));
			}
			Document serviceDocument = builder.parse(new InputSource(getConfigReader(configPath)));
			return serviceDocument;
		}
		catch(Exception e)
		{
			throw new ModuleConfigException(domain + " config document not valid: " + e.getMessage());
		}
	}

	private Reader getConfigReader(String name) throws UnsupportedEncodingException, FileNotFoundException
	{
		return new BufferedReader(new InputStreamReader(new FileInputStream(name), "UTF8"));
	}

	private Schema getConfigSchema() throws ModuleConfigException
	{
		if(schemaPath == null)
		{
			return null;
		}
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = null;
		try
		{
			InputStream is = new FileInputStream(schemaPath);
			schema = sf.newSchema(new StreamSource(is));
		}
		catch(SAXException e)
		{
			throw new ModuleConfigException(domain + " config schema not valid: " + e.getMessage());
		}
		catch(FileNotFoundException e1)
		{
			throw new ModuleConfigException(domain + " config schema not found: " + e1.getMessage());
		}
		return schema;
	}

	private class ErrorHandler extends DefaultHandler
	{
		public void error(SAXParseException e) throws SAXException
		{
			logger.error(e.getMessage());
			super.error(e);
		}

		public void fatalError(SAXParseException e) throws SAXException
		{
			logger.fatal(e.getMessage());
			super.fatalError(e);
		}

		public void warning(SAXParseException e) throws SAXException
		{
			logger.warn(e.getMessage());
			super.warning(e);
		}
	}

}
