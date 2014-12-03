package at.adaptive.components.datahandling.common;

import java.io.Serializable;

import at.adaptive.components.datahandling.dataexport.function.Function;

public class ColumnDefinition implements Serializable
{
	private static final long serialVersionUID = 8522628538016333270L;

	private int position;

	private String value;

	private String propertyValue;

	private boolean beanProperty = true;

	private Function function;

	private boolean export = true;

	private boolean hideIfEmpty = false;

	@SuppressWarnings("unchecked")
	private Converter converter;

	public ColumnDefinition()
	{}

	public ColumnDefinition(int position, String value, boolean export)
	{
		this.position = position;
		this.value = value;
		this.export = export;
	}

	public ColumnDefinition(int position, String value, String propertyValue)
	{
		this(position, value, propertyValue, false);
	}

	public ColumnDefinition(int position, String value, String propertyValue, boolean hideIfEmpty)
	{
		super();
		this.position = position;
		this.value = value;
		this.propertyValue = propertyValue;
		this.hideIfEmpty = hideIfEmpty;
	}

	public ColumnDefinition(String value)
	{
		this(value, value);
	}

	public ColumnDefinition(String value, String propertyValue)
	{
		this(-1, value, propertyValue, false);
	}

	public ColumnDefinition(String value, String propertyValue, boolean hideIfEmpty)
	{
		this(-1, value, propertyValue, hideIfEmpty);
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof ColumnDefinition)) return false;
		ColumnDefinition other = (ColumnDefinition)obj;
		if(beanProperty != other.beanProperty) return false;
		if(converter == null)
		{
			if(other.converter != null) return false;
		}
		else if(!converter.equals(other.converter)) return false;
		if(export != other.export) return false;
		if(propertyValue == null)
		{
			if(other.propertyValue != null) return false;
		}
		else if(!propertyValue.equals(other.propertyValue)) return false;
		if(value == null)
		{
			if(other.value != null) return false;
		}
		else if(!value.equals(other.value)) return false;
		return true;
	}

	@SuppressWarnings("unchecked")
	public Converter getConverter()
	{
		return converter;
	}

	public Function getFunction()
	{
		return function;
	}

	public int getPosition()
	{
		return position;
	}

	public String getPropertyValue()
	{
		return propertyValue;
	}

	public String getRawPropertyValue()
	{
		if(propertyValue == null)
		{
			return null;
		}
		String rawPropertyValue = new String(propertyValue);
		if(rawPropertyValue.startsWith("#{bean"))
		{
			rawPropertyValue = rawPropertyValue.substring(7);
		}
		if(rawPropertyValue.endsWith("}"))
		{
			rawPropertyValue = rawPropertyValue.substring(0, rawPropertyValue.length() - 1);
		}
		return rawPropertyValue;
	}

	public String getValue()
	{
		return value;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (beanProperty ? 1231 : 1237);
		result = prime * result + ((converter == null) ? 0 : converter.hashCode());
		result = prime * result + (export ? 1231 : 1237);
		result = prime * result + ((propertyValue == null) ? 0 : propertyValue.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	public boolean isBeanProperty()
	{
		return beanProperty;
	}

	public boolean isExport()
	{
		return export;
	}

	public boolean isFunctionSet()
	{
		return function != null;
	}

	public boolean isHideIfEmpty()
	{
		return hideIfEmpty;
	}

	public boolean isPositionSet()
	{
		return position > 0;
	}

	@SuppressWarnings("unchecked")
	public void setConverter(Converter converter)
	{
		this.converter = converter;
	}

	public void setFunction(Function function)
	{
		this.function = function;
		if(function != null)
		{
			beanProperty = false;
		}
	}

	public void setPosition(int position)
	{
		this.position = position;
	}

	public void setPropertyValue(String propertyValue)
	{
		this.propertyValue = propertyValue;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
}
