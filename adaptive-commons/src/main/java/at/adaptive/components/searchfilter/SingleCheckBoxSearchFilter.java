package at.adaptive.components.searchfilter;


public class SingleCheckBoxSearchFilter<T> extends TextSearchFilter<T>
{
	public SearchFilterType getSearchFilterType()
	{
		return SearchFilterType.SINGLECHECKBOX;
	}
}
