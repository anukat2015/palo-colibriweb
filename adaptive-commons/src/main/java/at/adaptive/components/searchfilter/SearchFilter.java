package at.adaptive.components.searchfilter;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.criterion.Criterion;

import at.adaptive.components.hibernate.AssociationPath;
import at.adaptive.components.restriction.Restriction;
import at.adaptive.components.restriction.RestrictionType;

public interface SearchFilter<T>
{
	public SearchFilterConfiguration<T> createConfiguration();

	Criterion createCriterion();

	public SearchFilterValues<T> createSearchFilterValues();

	List<T> createValues();

	AssociationPath getAssociationPath();

	RestrictionType[] getAvailableRestrictionTypes();

	EntityManager getEntityManager();

	String getFulltextFieldName();

	String getIdPropertyName();

	String getName();

	String getPropertyName();

	Restriction<T> getRestriction();

	RestrictionType getRestrictionType();

	Class<?> getSearchClass();

	SearchFilterType getSearchFilterType();

	Class<T> getType();

	boolean hasMultipleRestrictionTypes();

	void setAssociationPath(AssociationPath associationPath);

	void setAvailableRestrictionTypes(RestrictionType[] availableRestrictionTypes);

	void setEntityManager(EntityManager entityManager);

	void setFulltextFieldName(String fulltextFieldName);

	void setIdPropertyName(String idPropertyName);

	void setName(String name);

	public void setPropertyName(String propertyName);

	void setRestriction(Restriction<T> restriction);

	void setRestrictionType(RestrictionType restrictionType);

	void setSearchClass(Class<?> searchClass);

	void setType(Class<T> type);

	void setValues(List<T> values);
}