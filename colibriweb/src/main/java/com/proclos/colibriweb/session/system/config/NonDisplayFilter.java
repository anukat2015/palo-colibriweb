package com.proclos.colibriweb.session.system.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import at.adaptive.components.common.CollectionUtil;

public class NonDisplayFilter<T> extends FilterGroup<T>
{
	private Set<String> enabledFilters = new HashSet<String>(1);
	private boolean userEnabledFiltersOnly = false;

	public NonDisplayFilter(String name, List<FilterDefinition> definitions)
	{
		super(name, JSFTypes.nondisplay, definitions);
	}

	public List<FilterDefinition> getEffectiveFilters()
	{
		return getEffectiveFilterValues();
	}

	public Object getValueForDefinition(String name)
	{
		return null;
	}

	public boolean isUserEnabledFiltersOnly()
	{
		return userEnabledFiltersOnly;
	}

	public void setEnabledFilters(List<String> filters)
	{
		enabledFilters.clear();
		if(CollectionUtil.isEmpty(filters))
		{
			return;
		}
		Set<String> newEnabledFilters = new HashSet<String>(1);
		newEnabledFilters.addAll(filters);
		setDirty(newEnabledFilters, enabledFilters);
		enabledFilters.addAll(filters);
	}

	public void setUserEnabledFiltersOnly(boolean userEnabledFiltersOnly)
	{
		this.userEnabledFiltersOnly = userEnabledFiltersOnly;
	}

	protected List<FilterDefinition> getEffectiveFilterValues()
	{
		if(evaluateEnabledExpression())
		{
			if(userEnabledFiltersOnly)
			{
				if(enabledFilters.isEmpty())
				{
					return new ArrayList<FilterDefinition>();
				}
				List<FilterDefinition> definitions = new ArrayList<FilterDefinition>(getDefinitions());
				for(Iterator<FilterDefinition> iterator = definitions.iterator(); iterator.hasNext();)
				{
					if(!enabledFilters.contains(iterator.next().getName()))
					{
						iterator.remove();
					}
				}
				return definitions;
			}
			else
			{
				return new ArrayList<FilterDefinition>(getDefinitions());
			}
		}
		if(isEnabledExpressionSet())
		{
			return new ArrayList<FilterDefinition>();
		}
		List<FilterDefinition> enabledFilterDefinitions = new ArrayList<FilterDefinition>(1);
		for(FilterDefinition filterDefinition : getDefinitions())
		{
			if(enabledFilters.contains(filterDefinition.getName()))
			{
				enabledFilterDefinitions.add(filterDefinition);
			}
		}
		return enabledFilterDefinitions;
	}

	protected void initAvailableValues(List<FilterDefinition> definitions)
	{}
}
