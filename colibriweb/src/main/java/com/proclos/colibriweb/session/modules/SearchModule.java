package com.proclos.colibriweb.session.modules;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import at.adaptive.components.common.StringUtil;
import at.adaptive.components.restriction.RestrictionGroup;

import com.proclos.colibriweb.entity.BaseEntity;
import com.proclos.colibriweb.session.system.config.FilterGroup;
import com.proclos.colibriweb.session.system.config.NonDisplayFilter;

public abstract class SearchModule<T extends BaseEntity> extends Module<T>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -178146876559094917L;
	private boolean enabled;
	private boolean filtering = false;

	public void checkDependencies(IModule<?> workingModule)
	{
		if(workingModule != null)
		{
			workingModule.checkDependencies();
		}
	}

	public void disable()
	{
		enabled = false;
	}

	public void enableFilters(String enabledFilters)
	{
		enabled = true;
		resetRestrictionGroups();
		info("Enabling filters: " + enabledFilters);
		if(StringUtil.isEmpty(enabledFilters))
		{
			return;
		}
		List<String> filters = new ArrayList<String>(1);
		String[] splits = enabledFilters.split("[\\,\\;]");
		for(String split : splits)
		{
			filters.add(split.trim());
		}
		for(FilterGroup<?> filterGroup : getAvailableFilterGroups())
		{
			if(filterGroup instanceof NonDisplayFilter<?>)
			{
				((NonDisplayFilter<?>)filterGroup).setEnabledFilters(filters);
			}
		}
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void resultSelected(IModule<?> workingModule)
	{
		resetRestrictions();
		for(Iterator<String> iterator = restrictionGroups.keySet().iterator(); iterator.hasNext();)
		{
			RestrictionGroup restrictionGroup = restrictionGroups.get(iterator.next());
			restrictionGroup.setValue(null);
		}
		setFirstResult(0);
		setDirty(true);
		enabled = false;
		checkDependencies(workingModule);
	}

	public void setNull(Object value)
	{
		value = null;
	}

	@Override
	protected void initialize()
	{
		super.initialize();
		for(FilterGroup<?> filterGroup : getAvailableFilterGroups())
		{
			if(filterGroup instanceof NonDisplayFilter<?>)
			{
				((NonDisplayFilter<?>)filterGroup).setUserEnabledFiltersOnly(true);
			}
		}
	}
	
	public Integer getMaxResults() {
		return filtering ? Integer.MAX_VALUE : 5;
	}
	
	public void filter() {
		filtering = true;
		super.filter();
		filtering = false;
	}

	// @Override
	// public void updateDependencies()
	// {
	// getParentModule().updateDependencies();
	// }
	//
	// protected abstract IModule<T> getParentModule();
}
