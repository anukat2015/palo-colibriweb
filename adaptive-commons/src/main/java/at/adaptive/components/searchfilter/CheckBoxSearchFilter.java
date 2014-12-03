package at.adaptive.components.searchfilter;

import java.util.List;

public class CheckBoxSearchFilter<T> extends AbstractSearchFilter<T>
{
	private List<T> values;
	private List<?> availableValues;

	public List<?> getAvailableValues()
	{
		return availableValues;
	}

	public void setAvailableValues(List<?> availableValues)
	{
		this.availableValues = availableValues;
	}

	public List<T> createValues()
	{
		return values;
	}

	public List<T> getValues()
	{
		return values;
	}

	public void setValues(List<T> values)
	{
		this.values = values;
	}

	public SearchFilterType getSearchFilterType()
	{
		return SearchFilterType.CHECKBOX;
	}
}
