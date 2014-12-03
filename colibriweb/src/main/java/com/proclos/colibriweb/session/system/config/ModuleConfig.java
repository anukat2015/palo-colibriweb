package com.proclos.colibriweb.session.system.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.faces.model.SelectItem;

import at.adaptive.components.common.CollectionUtil;
import at.adaptive.components.datahandling.common.ColumnDefinition;

public class ModuleConfig
{
	private String name;
	private String label;
	private boolean navigable = true;
	private boolean administrable = true;
	private boolean lockable = false;
	private int minSearchChars;
	private String location = "";
	private ModuleConfig parent;
	private Properties properties = new Properties();
	private List<String> dependencies = new ArrayList<String>();
	private List<FilterGroup<?>> filters = new ArrayList<FilterGroup<?>>();
	private Map<String, Map<String, List<DependencyAction>>> dependencyActionMap = new HashMap<String, Map<String, List<DependencyAction>>>();
	private Map<String, List<DependencyRestriction>> dependencyRestrictionMap = new HashMap<String, List<DependencyRestriction>>();
	private Map<String, List<DependencyProperty>> dependencyPropertyMap = new HashMap<String, List<DependencyProperty>>();
	private List<ColumnDefinition> columnDefinitions = new ArrayList<ColumnDefinition>();
	private List<SelectItem> sorterValues = new ArrayList<SelectItem>();

	public void addDependency(String dependency)
	{
		getDependencies().add(dependency);
	}

	public void addDependencyAction(String dependency, String event, DependencyAction dependencyAction)
	{
		Map<String, List<DependencyAction>> map = dependencyActionMap.get(dependency);
		if(map == null)
		{
			map = new HashMap<String, List<DependencyAction>>();
		}
		List<DependencyAction> dependencyActions = map.get(event);
		if(dependencyActions == null)
		{
			dependencyActions = new ArrayList<DependencyAction>();
		}
		dependencyActions.add(dependencyAction);
		map.put(event, dependencyActions);
		dependencyActionMap.put(dependency, map);
	}

	public void addDependencyProperty(String dependency, DependencyProperty dependencyProperty)
	{
		List<DependencyProperty> dependencyPropertys = dependencyPropertyMap.get(dependency);
		if(dependencyPropertys == null)
		{
			dependencyPropertys = new ArrayList<DependencyProperty>();
		}
		dependencyPropertys.add(dependencyProperty);
		dependencyPropertyMap.put(dependency, dependencyPropertys);
	}

	public void addDependencyRestriction(String dependency, DependencyRestriction dependencyRestriction)
	{
		List<DependencyRestriction> dependencyRestrictions = dependencyRestrictionMap.get(dependency);
		if(dependencyRestrictions == null)
		{
			dependencyRestrictions = new ArrayList<DependencyRestriction>();
		}
		dependencyRestrictions.add(dependencyRestriction);
		dependencyRestrictionMap.put(dependency, dependencyRestrictions);
	}

	public void addExportColumn(String headerText, String el)
	{
		columnDefinitions.add(new ColumnDefinition(headerText, el));
	}

	public void addProperty(String key, String value)
	{
		properties.setProperty(key, value);
	}


	public List<ColumnDefinition> getColumnDefinitions()
	{
		return columnDefinitions;
	}

	public List<String> getDependencies()
	{
		return dependencies;
	}

	public List<DependencyAction> getDependencyActionsForEvent(String dependency, String event)
	{
		Map<String, List<DependencyAction>> map = dependencyActionMap.get(dependency);
		if(map == null)
		{
			return null;
		}
		return map.get(event);
	}

	public List<DependencyProperty> getDependencyProperties(String dependency)
	{
		return dependencyPropertyMap.get(dependency);
	}

	public List<DependencyRestriction> getDependencyRestrictions(String dependency)
	{
		return dependencyRestrictionMap.get(dependency);
	}

	public List<FilterGroup<?>> getFilters()
	{
		return filters;
	}

	public String getLabel()
	{
		return label;
	}

	public String getName()
	{
		return name;
	}

	public ModuleConfig getParent()
	{
		return parent;
	}

	public String getPath()
	{
		StringBuilder sb = new StringBuilder();
		if(parent != null)
		{
			sb.append(parent.getName());
			sb.append("/");
		}
		sb.append(name);
		return sb.toString().toLowerCase();
	}

	public Properties getProperties()
	{
		return properties;
	}

	public boolean hasExportConfiguration()
	{
		return CollectionUtil.isNotEmpty(columnDefinitions);
	}

	public boolean isAdministrable()
	{
		return administrable;
	}

	public boolean isLockable()
	{
		return lockable;
	}

	public boolean isNavigable()
	{
		return navigable;
	}
	
	public String getLocation()
	{
		return location;
	}

	public void setAdministrable(boolean administrable)
	{
		this.administrable = administrable;
	}

	public void setDependencies(List<String> dependencies)
	{
		this.dependencies = dependencies;
	}

	public void setFilters(List<FilterGroup<?>> filters)
	{
		this.filters = filters;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public void setLockable(boolean lockable)
	{
		this.lockable = lockable;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setNavigable(boolean navigable)
	{
		this.navigable = navigable;
	}
	
	public void setLocation(String location)
	{
		this.location = location;
	}

	public void setParent(ModuleConfig parent)
	{
		this.parent = parent;
	}

	public void setMinSearchChars(int minSearchChars) {
		this.minSearchChars = minSearchChars;
	}

	public int getMinSearchChars() {
		return minSearchChars;
	}

	public void setSorterValues(List<SelectItem> sorterValues) {
		this.sorterValues = sorterValues;
	}

	public List<SelectItem> getSorterValues() {
		return sorterValues;
	}
}
