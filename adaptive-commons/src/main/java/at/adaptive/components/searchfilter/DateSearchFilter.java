package at.adaptive.components.searchfilter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.adaptive.components.common.CollectionUtil;

public class DateSearchFilter extends AbstractSearchFilter<Date>
{
	private Date value;

	public List<Date> createValues()
	{
		List<Date> list = new ArrayList<Date>(1);
		list.add(value);
		return list;
	}

	public Date getValue()
	{
		return value;
	}

	public void setValue(Date value)
	{
		this.value = value;
	}

	public SearchFilterType getSearchFilterType()
	{
		return SearchFilterType.DATE;
	}

	public void setValues(List<Date> values)
	{
		if(CollectionUtil.isNotEmpty(values))
		{
			value = values.get(0);
		}
	}
}
