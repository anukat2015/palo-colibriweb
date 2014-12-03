package at.adaptive.components.searchfilter;

import java.util.ArrayList;
import java.util.List;

import at.adaptive.components.common.CollectionUtil;

public class TextSearchFilter<T> extends AbstractSearchFilter<T>
{
	private T value;

	public List<T> createValues()
	{
		List<T> list = new ArrayList<T>(1);
		list.add(value);
		return list;
	}

	public T getValue()
	{
		return value;
	}

	public void setValue(T value)
	{
		this.value = value;
	}

	public SearchFilterType getSearchFilterType()
	{
		return SearchFilterType.TEXT;
	}

	public void setValues(List<T> values)
	{
		if(CollectionUtil.isNotEmpty(values))
		{
			value = values.get(0);
		}
	}
}
