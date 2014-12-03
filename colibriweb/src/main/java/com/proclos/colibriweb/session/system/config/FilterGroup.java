package com.proclos.colibriweb.session.system.config;

import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;

public abstract class FilterGroup<T>
{

	public enum FilterModes
	{
		and, or
	}

	private JSFTypes jsfType;

	private String name;
	private ValueExpression<Boolean> enabledExpression;
	private boolean dirty = false;
	private boolean active = true;
	private List<FilterDefinition> definitions;
	private List<SelectItem> availableValues;
	private boolean display = true;
	private FilterModes mode;

	public FilterGroup(String name, JSFTypes type, List<FilterDefinition> definitions)
	{
		setName(name);
		setJsfType(type);
		setDefinitions(definitions);
	}

	public void clearDirty()
	{
		dirty = false;
	}

	public boolean evaluateEnabledExpression()
	{
		if(!isEnabledExpressionSet())
		{
			return false;
		}
		return enabledExpression.getValue();
	}

	public List<SelectItem> getAvailableValues()
	{
		return availableValues;
	}

	public List<FilterDefinition> getDefinitions()
	{
		return definitions;
	}

	public List<FilterDefinition> getEffectiveFilters()
	{
		if(evaluateEnabledExpression())
		{
			return getDefinitions();
		}
		return getEffectiveFilterValues();
	}

	public JSFTypes getJsfType()
	{
		return jsfType;
	}

	public String getName()
	{
		return name;
	}

	public abstract Object getValueForDefinition(String name);

	public boolean isActive()
	{
		return active;
	}

	public boolean isDirty()
	{
		return dirty;
	}

	public boolean isDisplay()
	{
		return display;
	}

	public boolean isEnabledExpressionSet()
	{
		return enabledExpression != null;
	}

	public void reset()
	{
		dirty = false;
	}

	public void setActive(boolean active)
	{
		dirty = (this.active != active);
		this.active = active;
	}

	public void setAvailableValues(List<SelectItem> availableValues)
	{
		this.availableValues = availableValues;
	}

	public void setDefinitions(List<FilterDefinition> definitions)
	{
		this.definitions = definitions;
		initAvailableValues(definitions);
	}

	public void setDisplay(boolean display)
	{
		this.display = display;
	}

	public void setEnabledExpression(String enabledExpression)
	{
		if(enabledExpression != null)
		{
			this.enabledExpression = Expressions.instance().createValueExpression(enabledExpression, Boolean.class);
		}
	}

	public void setJsfType(JSFTypes jsfType)
	{
		this.jsfType = jsfType;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public FilterModes getMode()
	{
		return mode;
	}

	public void setMode(FilterModes mode)
	{
		this.mode = mode;
	}

	protected <U> boolean checkDirty(U oldValue, U newValue)
	{
		if(oldValue == null && newValue == null)
		{
			return false;
		}
		if(oldValue == null || newValue == null)
		{
			return true;
		}
		return !oldValue.equals(newValue);
	}

	protected abstract List<FilterDefinition> getEffectiveFilterValues();

	protected abstract void initAvailableValues(List<FilterDefinition> definitions);

	protected <U> void setDirty(U oldValue, U newValue)
	{
		dirty = checkDirty(oldValue, newValue);
	}

	public enum JSFTypes
	{
		selectmanycheckbox, selectonemenu, selectoneradio, selectmanydate, nondisplay;
	}
}
