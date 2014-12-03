package at.adaptive.components.searchfilter;

import java.util.List;

import at.adaptive.components.common.StringUtil;

public class SearchFilterSelection
{
	private String selectedSearchFilter;

	private SearchFilter<?> searchFilter;
	private List<SearchFilter<?>> availableSearchFilters;

	public SearchFilterSelection(List<SearchFilter<?>> availableSearchFilters)
	{
		super();
		this.availableSearchFilters = availableSearchFilters;
	}

	public List<SearchFilter<?>> getAvailableSearchFilters()
	{
		return availableSearchFilters;
	}

	public SearchFilter<?> getSearchFilter()
	{
		return searchFilter;
	}

	public String getSelectedSearchFilter()
	{
		return selectedSearchFilter;
	}

	public void setActualSelectedSearchFilter(SearchFilter<?> searchFilter)
	{
		this.selectedSearchFilter = searchFilter.getName();
		this.searchFilter = searchFilter;
	}

	public void setSearchFilter(SearchFilter<?> searchFilter)
	{
		this.searchFilter = searchFilter;
	}

	public void setSelectedSearchFilter(String selectedSearchFilter)
	{
		this.selectedSearchFilter = selectedSearchFilter;
		this.searchFilter = findSearchFilter();
	}

	private SearchFilter<?> findSearchFilter()
	{
		if(!StringUtil.isEmpty(selectedSearchFilter))
		{
			for(SearchFilter<?> searchFilter : availableSearchFilters)
			{
				if(searchFilter.getName().equals(selectedSearchFilter))
				{
					return searchFilter;
				}
			}
		}
		return null;
	}
}
