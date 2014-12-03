package com.proclos.colibriweb.session.system.config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;

import at.adaptive.components.common.DateUtil;

public class ManyDateFilter<T> extends FilterGroup<T>
{
	public class DateWrapper
	{
		private Date date;
		private Date defaultDate;
		private String label;

		public String toString()
		{
			return label;
		}

		public void setDate(Date date)
		{
			setDirty(this.date, date);
			this.date = date;
		}

		public Date getDate()
		{
			return (date == null) ? defaultDate : date;
		}

		public void setDefaultDate(Date defaultDate)
		{
			this.defaultDate = defaultDate;
		}

		public Date getDefaultDate()
		{
			return defaultDate;
		}

		public void setLabel(String label)
		{
			this.label = label;
		}

		public String getLabel()
		{
			return label;
		}
	}

	public ManyDateFilter(String name, List<FilterDefinition> definitions)
	{
		super(name, JSFTypes.selectmanydate, definitions);
		// TODO set this by User Preferences
		setActive(false);
	}

	protected void initAvailableValues(List<FilterDefinition> definitions)
	{
		setAvailableValues(new ArrayList<SelectItem>());
		for(FilterDefinition d : definitions)
		{
			DateWrapper dw = new DateWrapper();
			dw.setLabel(d.getName());
			Calendar c = DateUtil.getTrimmedToDayCalendar(new Date());
			if(d.getDefaultValue() != null)
			{
				c.add(Calendar.DAY_OF_YEAR, Integer.parseInt(d.getDefaultValue()));
			}
			dw.setDefaultDate(c.getTime());
			SelectItem i = new SelectItem(dw, dw.getLabel());
			getAvailableValues().add(i);
		}
	}

	@SuppressWarnings("rawtypes")
	public Object getValueForDefinition(String name)
	{
		for(SelectItem s : getAvailableValues())
		{
			if(s.getLabel().equals(name))
			{
				if(s.getValue() instanceof ManyDateFilter.DateWrapper) return ((ManyDateFilter.DateWrapper)s.getValue()).getDate();
				else return s.getValue();
			}
		}
		return null;
	}

	protected List<FilterDefinition> getEffectiveFilterValues()
	{
		if(evaluateEnabledExpression())
		{
			return new ArrayList<FilterDefinition>(getDefinitions());
		}
		if(isActive()) return getDefinitions();
		else return new ArrayList<FilterDefinition>();
	}

}
