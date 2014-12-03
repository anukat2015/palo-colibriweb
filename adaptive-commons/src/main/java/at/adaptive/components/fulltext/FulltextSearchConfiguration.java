package at.adaptive.components.fulltext;

import at.adaptive.components.common.StringUtil;

public class FulltextSearchConfiguration
{
	public static final Integer DEFAULT_MAX_RESULTS = 100;
	public static final Double DEFAULT_FUZZY_FACTOR = 0.6;
	private String indexName;
	private String[] fieldNames;
	private Integer minChars;
	private boolean addWildcardPrefix;
	private boolean addWildcardSuffix;
	private boolean addFuzzySuffix;
	private boolean sortByLength;
	private Integer maxResults;
	private Double fuzzyFactor;

	public FulltextSearchConfiguration(String indexName)
	{
		if(StringUtil.isEmpty(indexName))
		{
			throw new IllegalArgumentException("Index name must not be null");
		}
		this.indexName = indexName;
	}

	public String[] getFieldNames()
	{
		return fieldNames;
	}

	public Double getFuzzyFactor()
	{
		if(fuzzyFactor == null)
		{
			return DEFAULT_FUZZY_FACTOR;
		}
		return fuzzyFactor;
	}

	public String getIndexName()
	{
		return indexName;
	}

	public Integer getMaxResults()
	{
		if(maxResults == null)
		{
			return DEFAULT_MAX_RESULTS;
		}
		return maxResults;
	}

	public Integer getMinChars()
	{
		return minChars;
	}

	public boolean isAddFuzzySuffix()
	{
		return addFuzzySuffix;
	}

	public boolean isAddWildcardPrefix()
	{
		return addWildcardPrefix;
	}

	public boolean isAddWildcardSuffix()
	{
		return addWildcardSuffix;
	}

	public boolean isSortByLength()
	{
		return sortByLength;
	}

	public void setAddFuzzySuffix(boolean addFuzzySuffix)
	{
		this.addFuzzySuffix = addFuzzySuffix;
	}

	public void setAddWildcardPrefix(boolean addWildcardPrefix)
	{
		this.addWildcardPrefix = addWildcardPrefix;
	}

	public void setAddWildcardSuffix(boolean addWildcardSuffix)
	{
		this.addWildcardSuffix = addWildcardSuffix;
	}

	public void setFieldNames(String... fieldNames)
	{
		this.fieldNames = fieldNames;
	}

	public void setFuzzyFactor(Double fuzzyFactor)
	{
		this.fuzzyFactor = fuzzyFactor;
	}

	public void setMaxResults(Integer maxResults)
	{
		this.maxResults = maxResults;
	}

	public void setMinChars(Integer minChars)
	{
		this.minChars = minChars;
	}

	public void setSortByLength(boolean sortByLength)
	{
		this.sortByLength = sortByLength;
	}
}
