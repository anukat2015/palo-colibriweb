package com.proclos.colibriweb.session.system.config;

import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;


public class FilterDefinition
{

	private String name;

	private String defaultValue;

	private FilterCondition condition;
	
	private FilterCriteria criteria;

	public FilterCondition getCondition()
	{
		return condition;
	}

	public String getDefaultValue()
	{
		if (defaultValue != null) {
			ValueExpression<Object> valueExpression = Expressions.instance().createValueExpression(defaultValue);
			Object value = valueExpression.getValue();
			if (value != null) return value.toString();
		}
		return defaultValue;
	}

	public String getName()
	{
		return name;
	}

	public void setCondition(FilterCondition condition)
	{
		this.condition = condition;
	}

	public void setCondition(String definition, String language, boolean isShortForm)
	{
		this.condition = new FilterCondition(definition, language, isShortForm);
	}

	public void setDefaultValue(String defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setCriteria(FilterCriteria criteria) {
		this.criteria = criteria;
	}

	public FilterCriteria getCriteria() {
		return criteria;
	}
	
	public boolean isCriteriaFilter() {
		return criteria != null;
	}

}
