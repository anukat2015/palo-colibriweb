package com.proclos.colibriweb.session.system.config;

import java.util.Properties;

import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;

public class DependencyProperty
{
	private String dependency;
	private String value;
	private String source;
	private String inputValue;
	private Properties properties = new Properties();
	private ValueExpression<Object> valueExpression;
	private ValueExpression<Object> inputValueExpression;

	public void addProperty(String key, String value)
	{
		properties.setProperty(key, value);
	}

	public Object getBindedInputValue()
	{
		return inputValueExpression.getValue();
	}

	public Object getBindedValue()
	{
		return valueExpression.getValue();
	}

	public String getDependency()
	{
		return dependency;
	}

	public String getInputValue()
	{
		return inputValue;
	}

	public Properties getProperties()
	{
		return properties;
	}

	public String getProperty(String key)
	{
		return properties.getProperty(key);
	}

	public String getSource()
	{
		return source;
	}

	public String getValue()
	{
		return value;
	}

	public void setBindedValue(Object value)
	{
		valueExpression.setValue(value);
	}

	public void setDependency(String dependency)
	{
		this.dependency = dependency;
	}

	public void setInputValue(String inputValue)
	{
		this.inputValue = inputValue;
		this.inputValueExpression = Expressions.instance().createValueExpression(inputValue);
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	public void setValue(String value)
	{
		this.value = value;
		this.valueExpression = Expressions.instance().createValueExpression(value);
	}
}
