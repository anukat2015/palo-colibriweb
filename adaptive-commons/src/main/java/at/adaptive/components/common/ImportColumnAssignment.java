package at.adaptive.components.common;

import java.io.Serializable;

public class ImportColumnAssignment implements Serializable
{
	private String name;
	private String propertyName;
	private boolean required;

	public ImportColumnAssignment(String name, String propertyName)
	{
		this(name, propertyName, false);
	}

	public ImportColumnAssignment(String name, String propertyName, boolean required)
	{
		this.name = name;
		this.propertyName = propertyName;
		this.required = required;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof ImportColumnAssignment)) return false;
		ImportColumnAssignment other = (ImportColumnAssignment)obj;
		if(name == null)
		{
			if(other.name != null) return false;
		}
		else if(!name.equals(other.name)) return false;
		if(propertyName == null)
		{
			if(other.propertyName != null) return false;
		}
		else if(!propertyName.equals(other.propertyName)) return false;
		return true;
	}

	public String getName()
	{
		return name;
	}

	public String getPropertyName()
	{
		return propertyName;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
		return result;
	}

	public boolean isRequired()
	{
		return required;
	}
}
