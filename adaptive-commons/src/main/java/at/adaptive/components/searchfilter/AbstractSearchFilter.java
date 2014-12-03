package at.adaptive.components.searchfilter;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Restrictions;

import at.adaptive.components.common.CollectionUtil;
import at.adaptive.components.hibernate.AssociationPath;
import at.adaptive.components.restriction.EnumRestriction;
import at.adaptive.components.restriction.Restriction;
import at.adaptive.components.restriction.RestrictionType;

public abstract class AbstractSearchFilter<T> implements SearchFilter<T>
{
	private String name;

	private RestrictionType[] availableRestrictionTypes;

	private RestrictionType restrictionType;

	private AssociationPath associationPath;

	private Restriction<T> restriction;

	private String propertyName;

	private String idPropertyName;

	private Class<?> searchClass;

	private String fulltextFieldName;

	private EntityManager entityManager;

	private Class<T> type;

	public SearchFilterConfiguration<T> createConfiguration()
	{
		SearchFilterConfiguration<T> searchFilterConfiguration = new SearchFilterConfiguration<T>();
		searchFilterConfiguration.setAssociationPath(getAssociationPath());
		searchFilterConfiguration.setAvailableRestrictionTypes(getAvailableRestrictionTypes());
		searchFilterConfiguration.setRestrictionType(getRestrictionType());
		searchFilterConfiguration.setFulltextSearchTerms(restriction.getFulltextSearchTerms());
		searchFilterConfiguration.setFulltextFilterContainers(restriction.getFulltextFilterContainers());
		searchFilterConfiguration.setIdPropertyName(getIdPropertyName());
		searchFilterConfiguration.setName(getName());
		searchFilterConfiguration.setOverrideSearchFilterClass(getClass());
		searchFilterConfiguration.setPropertyName(getPropertyName());
		searchFilterConfiguration.setSearchClass(getSearchClass());
		searchFilterConfiguration.setSearchFilterType(getSearchFilterType());
		if(isEnumType())
		{

		}
		searchFilterConfiguration.setType(getType());
		searchFilterConfiguration.setValues(createValues());
		return searchFilterConfiguration;
	}

	public Criterion createCriterion()
	{
		restriction.setRestrictionType(restrictionType);
		if(restrictionType.equals(RestrictionType.FULLTEXT))
		{
			restriction.setFulltext(true);
			restriction.setSearchClass(searchClass);
			restriction.setIdPropertyName(idPropertyName);
			// restriction.setEntityManager(entityManager);
			restriction.setAppendWildcard(true);
			String fulltextFieldNameToSet;
			if(fulltextFieldName == null)
			{
				fulltextFieldNameToSet = propertyName;
			}
			else
			{
				fulltextFieldNameToSet = fulltextFieldName;
			}
			restriction.setFulltextFieldName(fulltextFieldNameToSet);
		}
		restriction.setPropertyName(propertyName);
		List<T> values = createValues();
		if(CollectionUtil.isEmpty(values))
		{
			return null;
		}
		Junction junction = null;
		if(values.size() > 1)
		{
			int criterionsAdded = 0;
			junction = Restrictions.disjunction();
			for(T value : values)
			{
				if(value instanceof String)
				{
					restriction.setValue((String)value);
				}
				else
				{
					restriction.setBindedValue(value);
				}
				Criterion criterion = restriction.createCriterion();
				if(criterion != null)
				{
					junction.add(criterion);
					criterionsAdded++;
				}
			}
			if(criterionsAdded > 0)
			{
				return junction;
			}
			else
			{
				return null;
			}
		}
		else
		{
			T value = values.get(0);
			if(value instanceof String)
			{
				restriction.setValue((String)value);
			}
			else
			{
				restriction.setBindedValue(value);
			}
			Criterion criterion = restriction.createCriterion();
			if(criterion != null)
			{
				return criterion;
			}
			else
			{
				return null;
			}
		}
	}

	public SearchFilterValues<T> createSearchFilterValues()
	{
		SearchFilterValues<T> searchFilterValues = new SearchFilterValues<T>();
		searchFilterValues.setValues(createValues());
		return searchFilterValues;
	}

	public AssociationPath getAssociationPath()
	{
		return associationPath;
	}

	public RestrictionType[] getAvailableRestrictionTypes()
	{
		return availableRestrictionTypes;
	}

	public EntityManager getEntityManager()
	{
		return entityManager;
	}

	public String getFulltextFieldName()
	{
		return fulltextFieldName;
	}

	public String getIdPropertyName()
	{
		return idPropertyName;
	}

	public String getName()
	{
		return name;
	}

	public String getPropertyName()
	{
		return propertyName;
	}

	public Restriction<T> getRestriction()
	{
		return restriction;
	}

	public RestrictionType getRestrictionType()
	{
		return restrictionType;
	}

	public Class<?> getSearchClass()
	{
		return searchClass;
	}

	public Class<T> getType()
	{
		return type;
	}

	public boolean hasMultipleRestrictionTypes()
	{
		return availableRestrictionTypes != null && availableRestrictionTypes.length > 1;
	}

	public boolean isEnumType()
	{
		return getRestriction() instanceof EnumRestriction;
	}

	public void setAssociationPath(AssociationPath associationPath)
	{
		this.associationPath = associationPath;
	}

	public void setAvailableRestrictionTypes(RestrictionType[] availableRestrictionTypes)
	{
		this.availableRestrictionTypes = availableRestrictionTypes;
		// set default restriction type
		if(availableRestrictionTypes != null && availableRestrictionTypes.length > 0)
		{
			setRestrictionType(availableRestrictionTypes[0]);
		}
	}

	public void setEntityManager(EntityManager entityManager)
	{
		this.entityManager = entityManager;
	}

	public void setFulltextFieldName(String fulltextFieldName)
	{
		this.fulltextFieldName = fulltextFieldName;
	}

	public void setIdPropertyName(String idPropertyName)
	{
		this.idPropertyName = idPropertyName;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setPropertyName(String propertyName)
	{
		this.propertyName = propertyName;
	}

	public void setRestriction(Restriction<T> restriction)
	{
		this.restriction = restriction;
	}

	public void setRestrictionType(RestrictionType restrictionType)
	{
		this.restrictionType = restrictionType;
	}

	public void setSearchClass(Class<?> searchClass)
	{
		this.searchClass = searchClass;
	}

	public void setType(Class<T> type)
	{
		this.type = type;
	}
}
