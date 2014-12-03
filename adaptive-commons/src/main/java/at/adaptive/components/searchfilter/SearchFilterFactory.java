package at.adaptive.components.searchfilter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import at.adaptive.components.common.CollectionUtil;
import at.adaptive.components.hibernate.AssociationPath;
import at.adaptive.components.restriction.BigDecimalRestriction;
import at.adaptive.components.restriction.BooleanRestriction;
import at.adaptive.components.restriction.ByteRestriction;
import at.adaptive.components.restriction.DateRestriction;
import at.adaptive.components.restriction.DoubleRestriction;
import at.adaptive.components.restriction.EnumRestriction;
import at.adaptive.components.restriction.FloatRestriction;
import at.adaptive.components.restriction.FulltextFilterContainer;
import at.adaptive.components.restriction.FulltextSearchTerm;
import at.adaptive.components.restriction.IntegerRestriction;
import at.adaptive.components.restriction.LongRestriction;
import at.adaptive.components.restriction.Restriction;
import at.adaptive.components.restriction.RestrictionType;
import at.adaptive.components.restriction.ShortRestriction;
import at.adaptive.components.restriction.StringRestriction;

public class SearchFilterFactory
{
	private static final Logger logger = Logger.getLogger(SearchFilterFactory.class);

	@SuppressWarnings("unchecked")
	public static <T> SearchFilter<T> createSearchFilter(Class<?> overrideSearchFilterClass, Class<T> type, String name, SearchFilterType searchFilterType, RestrictionType[] availableRestrictionTypes, AssociationPath associationPath, String propertyName, List<?> availableValues, String idPropertyName, Class<?> searchClass, List<FulltextSearchTerm> fulltextSearchTerms, List<FulltextFilterContainer> fulltextFilterContainers, EntityManager entityManager)
	{
		SearchFilter<T> searchFilter = null;
		if(overrideSearchFilterClass != null)
		{
			try
			{
				searchFilter = (SearchFilter<T>)overrideSearchFilterClass.newInstance();
			}
			catch(Exception e)
			{
				logger.error("Error creating search filter form class: " + overrideSearchFilterClass, e);
				return null;
			}
		}
		else
		{
			if(searchFilterType.equals(SearchFilterType.TEXT))
			{
				searchFilter = new TextSearchFilter<T>();
			}
			else if(searchFilterType.equals(SearchFilterType.CHECKBOX))
			{
				searchFilter = new CheckBoxSearchFilter<T>();
				if(availableValues != null)
				{
					((CheckBoxSearchFilter<T>)searchFilter).setAvailableValues(availableValues);
				}
			}
			else if(searchFilterType.equals(SearchFilterType.DROPDOWN))
			{
				// TODO: implement drop-down search filter
			}
			else if(searchFilterType.equals(SearchFilterType.DATE))
			{
				searchFilter = (SearchFilter<T>)new DateSearchFilter();
			}
			else if(searchFilterType.equals(SearchFilterType.SINGLECHECKBOX))
			{
				searchFilter = (SearchFilter<T>)new SingleCheckBoxSearchFilter();
			}
		}
		if(searchFilter != null)
		{
			Restriction<T> restriction = createRestriction(type);
			if(restriction == null)
			{
				return null;
			}
			if(CollectionUtil.isNotEmpty(fulltextSearchTerms))
			{
				for(FulltextSearchTerm fulltextSearchTerm : fulltextSearchTerms)
				{
					restriction.addFulltextSearchTerm(fulltextSearchTerm);
				}
			}
			if(CollectionUtil.isNotEmpty(fulltextFilterContainers))
			{
				for(FulltextFilterContainer fulltextFilterContainer : fulltextFilterContainers)
				{
					restriction.addFulltextFilterContainer(fulltextFilterContainer);
				}
			}
			searchFilter.setType(type);
			searchFilter.setName(name);
			searchFilter.setRestriction(restriction);
			searchFilter.setAvailableRestrictionTypes(availableRestrictionTypes);
			searchFilter.setAssociationPath(associationPath);
			searchFilter.setPropertyName(propertyName);
			searchFilter.setIdPropertyName(idPropertyName);
			searchFilter.setSearchClass(searchClass);
			searchFilter.setEntityManager(entityManager);
			return searchFilter;
		}
		else
		{
			return null;
		}
	}

	public static <T> SearchFilter<T> createSearchFilter(Class<T> type, String name, SearchFilterType searchFilterType, RestrictionType[] availableRestrictionTypes, AssociationPath associationPath, String propertyName, List<?> availableValues, String idPropertyName, Class<?> searchClass, List<FulltextSearchTerm> fulltextSearchTerms, List<FulltextFilterContainer> fulltextFilterContainers, EntityManager entityManager)
	{
		return createSearchFilter(null, type, name, searchFilterType, availableRestrictionTypes, associationPath, propertyName, availableValues, idPropertyName, searchClass, fulltextSearchTerms, fulltextFilterContainers, entityManager);
	}

	public static <T> SearchFilter<T> createSearchFilter(Class<T> type, String name, SearchFilterType searchFilterType, RestrictionType[] availableRestrictionTypes, AssociationPath associationPath, String propertyName, String idPropertyName, Class<?> searchClass, List<FulltextSearchTerm> fulltextSearchTerms, List<FulltextFilterContainer> fulltextFilterContainers, EntityManager entityManager)
	{
		return createSearchFilter(type, name, searchFilterType, availableRestrictionTypes, associationPath, propertyName, null, idPropertyName, searchClass, fulltextSearchTerms, fulltextFilterContainers, entityManager);
	}

	public static <T> SearchFilter<T> createSearchFilter(SearchFilterConfiguration<T> searchFilterConfiguration, EntityManager entityManager)
	{
		return createSearchFilter(searchFilterConfiguration, null, entityManager);
	}

	public static <T> SearchFilter<T> createSearchFilter(SearchFilterConfiguration<T> searchFilterConfiguration, List<?> availableValues, EntityManager entityManager)
	{
		SearchFilter<T> searchFilter = createSearchFilter(searchFilterConfiguration.getType(), searchFilterConfiguration.getName(), searchFilterConfiguration.getSearchFilterType(), searchFilterConfiguration.getAvailableRestrictionTypes(), searchFilterConfiguration.getAssociationPath(), searchFilterConfiguration.getPropertyName(), availableValues, searchFilterConfiguration.getIdPropertyName(), searchFilterConfiguration.getSearchClass(), searchFilterConfiguration.getFulltextSearchTerms(), searchFilterConfiguration.getFulltextFilterContainers(), entityManager);
		searchFilter.setRestrictionType(searchFilterConfiguration.getRestrictionType());
		searchFilter.setValues(searchFilterConfiguration.getValues());
		return searchFilter;
	}

	@SuppressWarnings("unchecked")
	private static <T> Restriction<T> createRestriction(Class<T> type)
	{
		Class<?> clazz = null;
		if(String.class.isAssignableFrom(type))
		{
			clazz = StringRestriction.class;
		}
		else if(Byte.class.isAssignableFrom(type) || byte.class.isAssignableFrom(type))
		{
			clazz = ByteRestriction.class;
		}
		else if(Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type))
		{
			clazz = ShortRestriction.class;
		}
		else if(Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type))
		{
			clazz = IntegerRestriction.class;
		}
		else if(Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type))
		{
			clazz = LongRestriction.class;
		}
		else if(Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type))
		{
			clazz = FloatRestriction.class;
		}
		else if(Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type))
		{
			clazz = DoubleRestriction.class;
		}
		else if(BigDecimal.class.isAssignableFrom(type))
		{
			clazz = BigDecimalRestriction.class;
		}
		else if(Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type))
		{
			clazz = BooleanRestriction.class;
		}
		else if(Enum.class.isAssignableFrom(type))
		{
			clazz = EnumRestriction.class;
		}
		else if(Date.class.isAssignableFrom(type))
		{
			clazz = DateRestriction.class;
		}
		if(clazz == null)
		{
			return null;
		}
		else
		{
			try
			{
				return (Restriction<T>)clazz.newInstance();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
	}
}
