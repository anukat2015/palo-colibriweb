package com.jedox.etl.components.connection;

import java.util.Properties;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;

import au.com.bytecode.opencsv.CSVParser;

import com.jedox.etl.core.aliases.IAliasMap;
import com.jedox.etl.core.component.InitializationException;
import com.jedox.etl.core.component.RuntimeException;
import com.jedox.etl.core.connection.Connection;
import com.jedox.etl.core.connection.IStreamReadable;
import com.jedox.etl.core.connection.IStreamWritable;
import com.jedox.etl.core.connection.MetadataCriteria;
import com.jedox.etl.core.node.IColumn;
import com.jedox.etl.core.node.Row;
import com.jedox.etl.core.source.processor.IProcessor;
import com.jedox.etl.core.source.processor.Processor;
import com.jedox.etl.core.source.processor.IProcessor.Facets;
import com.jedox.etl.core.util.SQLUtil;
import com.jedox.etl.core.writer.IWriter;

import java.util.Map;
import java.util.HashMap;

public class MQConnection extends Connection implements IStreamWritable, IStreamReadable
{

	public enum MessageFormats
	{
		string, map
	}

	private class MQWriter implements IWriter
	{

		private int lines = 0;

		@Override
		public void write(IProcessor rows) throws RuntimeException
		{
			CamelContext context = open();
			ProducerTemplate template = context.createProducerTemplate();
			Row row = rows.next();
			while(row != null)
			{
				lines++;
				switch(messageFormat)
				{
					case map:
					{
						Map<String, Object> map = new HashMap<String, Object>();
						for(IColumn c : row.getColumns())
						{
							map.put(c.getName(), c.getValue());
						}
						template.sendBody(getEndpointURI(), map);
						break;
					}
					default:
					{
						String result = SQLUtil.enumNames(SQLUtil.quoteNames(row.getColumnValues(), quote), delimiter);
						template.sendBody(getEndpointURI(), result);
					}
				}
				row = rows.next();
			}
			if(getTerminationCode() != null)
			{
				switch(messageFormat)
				{
					case map:
					{
						Map<String, Object> map = new HashMap<String, Object>();
						map.put(getTerminationCode(), getTerminationCode());
						template.sendBody(getEndpointURI(), map);
						break;
					}
					default:
						template.sendBody(getEndpointURI(), getTerminationCode());
				}
			}
			try
			{
				template.stop();
			}
			catch(Exception e)
			{
				throw new RuntimeException("Cannot stop MQ producer.");
			}
		}

		@Override
		public int getLinesOut()
		{
			return lines;
		}

	}
	
	private class MQProcessor extends Processor
	{

		private Row row;
		private CSVParser parser;
		private ConsumerTemplate consumer;
		private int size;
		private int consumed = 0;

		public MQProcessor(CamelContext context, Row row, int size) 
		{
			this.row = row;
			this.size = size;
			
		}

		@Override
		protected boolean fillRow(Row row) throws Exception
		{
			if(size > 0 && consumed >= size) return false;
			Object body = consumer.receiveBody(getEndpointURI());
			if(getMessageFormat().equals(MessageFormats.map) && body instanceof Map)
			{
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>)body;
				if(map.containsKey(getTerminationCode())) return false;
				for(IColumn c : row.getColumns())
				{
					c.setValue(map.get(c.getName()));
				}
			}
			else
			{
				String[] line = parser.parseLine(body.toString());
				if(line[0].equals(getTerminationCode())) return false;
				for(int i = 0; i < Math.min(row.size(), line.length); i++)
				{
					row.getColumn(i).setValue(line[i]);
				}
			}
			consumed++;
			return true;
		}

		@Override
		protected Row getRow()
		{
			return row;
		}

		public void close()
		{
			super.close();
			try
			{
				consumer.stop();
			}
			catch(Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		protected void init() throws RuntimeException {
			consumer = context.createConsumerTemplate();
			parser = new CSVParser(getDelimiter().toCharArray()[0], getQuote().toCharArray()[0]);
		}

	}

	private static final String componentName = "activemq";
	private CamelContext context;
	private String quote;
	private String delimiter;
	private String terminationCode;
	private IWriter writer = new MQWriter();
	private MessageFormats messageFormat;
	private String protocol;

	protected String getConnectionUrl()
	{
		StringBuffer parameters = new StringBuffer();
		for(Object key : getConnectionParameters().keySet())
		{
			parameters.append("&" + key.toString() + "=" + getConnectionParameters().getProperty(key.toString()));
		}
		if(parameters.length() > 0) parameters.replace(0, 1, "?");
		return getProtocol() + "://" + getHost() + (getPort() != null ? ":" + getPort() : "") + parameters.toString();
	}

	@Override
	public CamelContext open() throws RuntimeException
	{
		try {
			if(context == null)
			{
				context = new DefaultCamelContext();
	
				// DefaultManagementStrategy strategy = new DefaultManagementStrategy();
				// context.setManagementStrategy(strategy);
				// context.disableJMX();
				// ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("peer://group-etl/broker-" + getName()+"?useJmx=false");
				// context.addComponent(componentName, JmsComponent.jmsComponentAutoAcknowledge(factory));
				ActiveMQComponent component = ActiveMQComponent.activeMQComponent();
				component.setBrokerURL(getConnectionUrl());
				if(getUser() != null)
				{
					component.setUserName(getUser());
					component.setPassword(getPassword());
				}
				context.addComponent(componentName, component);
			}
			return context;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	} 

	@Override
	public void close()
	{
		if(context != null)
		{
			try
			{
				context.stop();
			}
			catch(Exception e)
			{}
			context = null;
		}
	}

	@Override
	public String getMetadata(Properties properties) throws RuntimeException
	{
		throw new RuntimeException("Not implemented in " + this.getClass().getCanonicalName());
	}

	protected String getHost()
	{
		return (super.getHost() != null) ? super.getHost() : "localhost";
	}

	protected String getProtocol()
	{
		return protocol;
	}

	@Override
	public IWriter getWriter(boolean append) throws RuntimeException
	{
		return writer;
	}


	public String getDelimiter()
	{
		return delimiter;
	}

	public String getQuote()
	{
		return quote;
	}

	public String getTerminationCode()
	{
		return terminationCode;
	}

	public String getEndpointURI()
	{
		return componentName + ":queue:" + getDatabase();
	}

	public String getDatabase()
	{
		return (super.getDatabase() != null) ? super.getDatabase() : getName();
	}

	public MessageFormats getMessageFormat()
	{
		return messageFormat;
	}

	public void init() throws InitializationException
	{
		try
		{
			super.init();
			delimiter = getParameter("delimiter", ",");
			quote = getParameter("quote", "\"");
			terminationCode = getParameter("terminationCode", null);
			messageFormat = MessageFormats.valueOf(getParameter("messageFormat", MessageFormats.string.name()));
			if(quote.equalsIgnoreCase("NONE")) quote = "";
			protocol = getParameter("protocol","tcp");
		}
		catch(Exception e)
		{
			throw new InitializationException(e);
		}
	}
	
	
	@Override
	public MetadataCriteria[] getMetadataCriterias() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IProcessor getProcessor(IAliasMap map, int size, long timeout) throws RuntimeException {
		return initProcessor(new MQProcessor(open(), map.getOutputDescription(), size),Facets.CONNECTION);
	}

}
