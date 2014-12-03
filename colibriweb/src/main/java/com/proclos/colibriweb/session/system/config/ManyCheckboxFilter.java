package com.proclos.colibriweb.session.system.config;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

public class ManyCheckboxFilter<T> extends FilterGroup<T>
{
	private List<String> selectedValues;

	public ManyCheckboxFilter(String name, List<FilterDefinition> definitions)
	{
		super(name, JSFTypes.selectmanycheckbox, definitions);
		List<String> initialValues = new ArrayList<String>();
		for(FilterDefinition d : definitions)
		{
			if(d.getDefaultValue() != null && d.getDefaultValue().equalsIgnoreCase("true"))
			{
				initialValues.add(d.getName());
			}
		}
		setSelectedValues(initialValues);
	}

	public List<String> getSelectedValues()
	{
		return selectedValues;
	}

	public void reset()
	{
		super.reset();
		selectedValues = new ArrayList<String>();
	}

	public void setSelectedValues(List<String> selectedValues)
	{
		setDirty(this.selectedValues, selectedValues);
		this.selectedValues = selectedValues;
	}

	protected List<FilterDefinition> getEffectiveFilterValues()
	{
		if(evaluateEnabledExpression())
		{
			return new ArrayList<FilterDefinition>(getDefinitions());
		}
		List<FilterDefinition> result = new ArrayList<FilterDefinition>();
		if(isActive())
		{
			for(String s : getSelectedValues())
			{
				for(FilterDefinition d : getDefinitions())
				{
					if(d.getName().equals(s.toString()))
					{
						result.add(d);
					}
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
			getAvailableValues().add(i);
		}
	}

	public Object getValueForDefinition(String name)
	{
		for(String s : getSelectedValues())
		{
			if(s.toString().equals(name))
			{
				return s;
			}
		}
		return null;
	}
}
