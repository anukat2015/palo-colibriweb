package at.adaptive.components.searchfilter;

import java.io.Serializable;
import java.util.List;

import at.adaptive.components.hibernate.AssociationPath;
import at.adaptive.components.restriction.FulltextFilterContainer;
import at.adaptive.components.restriction.FulltextSearchTerm;
import at.adaptive.components.restriction.RestrictionType;

public class SearchFilterConfiguration<T> implements Serializable
{
	private Class<?> overrideSearchFilterClass;
	private Class<T> type;
	private String name;
	private SearchFilterType searchFilterType;
	private RestrictionType[] availableRestrictionTypes;
	private RestrictionType restrictionType;
	private AssociationPath associationPath;
	private String propertyName;
	private String idPropertyName;
	private Class<?> searchClass;
	private List<FulltextSearchTerm> fulltextSearchTerms;
	private List<FulltextFilterContainer> fulltextFilterContainers;
	private List<T> values;

	public AssociationPath getAssociationPath()
	{
		return associationPath;
	}

	public RestrictionType[] getAvailableRestrictionTypes()
	{
		return availableRestrictionTypes;
	}

	public List<FulltextFilterContainer> getFulltextFilterContainers()
	{
		return fulltextFilterContainers;
	}

	public List<FulltextSearchTerm> getFulltextSearchTerms()
	{
		return fulltextSearchTerms;
	}

	public String getIdPropertyName()
	{
		return idPropertyName;
	}

	public String getName()
	{
		return name;
	}

	public Class<?> getOverrideSearchFilterClass()
	{
		return overrideSearchFilterClass;
	}

	public String getPropertyName()
	{
		return propertyName;
	}

	public RestrictionType getRestrictionType()
	{
		return restrictionType;
	}

	public Class<?> getSearchClass()
	{
		return searchClass;
	}

	public SearchFilterType getSearchFilterType()
	{
		return searchFilterType;
	}

	public Class<T> getType()
	{
		return type;
	}

	public List<T> getValues()
	{
		return values;
	}

	public void setAssociationPath(AssociationPath associationPath)
	{
		this.associationPath = associationPath;
	}

	public void setAvailableRestrictionTypes(RestrictionType[] availableRestrictionTypes)
	{
		this.availableRestrictionTypes = availableRestrictionTypes;
	}

	public void setFulltextFilterContainers(List<FulltextFilterContainer> fulltextFilterContainers)
	{
		this.fulltextFilterContainers = fulltextFilterContainers;
	}

	public void setFulltextSearchTerms(List<FulltextSearchTerm> fulltextSearchTerms)
	{
		this.fulltextSearchTerms = fulltextSearchTerms;
	}

	public void setIdPropertyName(String idPropertyName)
	{
		this.idPropertyName = idPropertyName;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setOverrideSearchFilterClass(Class<?> overrideSearchFilterClass)
	{
		this.overrideSearchFilterClass = overrideSearchFilterClass;
	}

	public void setPropertyName(String propertyName)
	{
		this.propertyName = propertyName;
	}

	public void setRestrictionType(RestrictionType restrictionType)
	{
		this.restrictionType = restrictionType;
	}

	public void setSearchClass(Class<?> searchClass)
	{
		this.searchClass = searchClass;
	}

	public void setSearchFilterType(SearchFilterType searchFilterType)
	{
		this.searchFilterType = searchFilterType;
	}

	public void setType(Class<T> type)
	{
		this.type = type;
	}

	public void setValues(List<T> values)
	{
		this.values = values;
	}
}