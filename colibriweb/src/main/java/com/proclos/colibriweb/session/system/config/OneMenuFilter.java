package com.proclos.colibriweb.session.system.config;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

public class OneMenuFilter<T> extends FilterGroup<T>
{
	public OneMenuFilter(String name, List<FilterDefinition> definitions)
	{
		super(name, JSFTypes.selectonemenu, definitions);
	}

	private String value;

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		setDirty(this.value, value);
		this.value = value;
	}

	protected List<FilterDefinition> getEffectiveFilterValues()
	{
		if(evaluateEnabledExpression())
		{
			return new ArrayList<FilterDefinition>(getDefinitions());
		}
		List<FilterDefinition> result = new ArrayList<FilterDefinition>();
		if(isActive() && value != null)
		{
			for(FilterDefinition d : getDefinitions())
			{
				if(d.getName().equals(value.toString()))
				{
					result.add(d);
				}
			}
		}
		return result;
	}

	protected void initAvailableValues(List<FilterDefinition> definitions)
	{
		setAvailableValues(new ArrayList<SelectItem>());
		for(FilterDefinition d : definitions)
		{
			SelectItem i = new SelectItem(d.getName());
			if(d.getDefaultValue() != null && d.getDefaultValue().equalsIgnoreCase("true")) value = d.getName();
			getAvailableValues().add(i);
		}
	}

	public void reset()
	{
		super.reset();
		value = null;
	}

	public Object getValueForDefinition(String name)
	{
		if(value.toString().equals(name))
		{
			return value;
		}
		return null;
	}

}
