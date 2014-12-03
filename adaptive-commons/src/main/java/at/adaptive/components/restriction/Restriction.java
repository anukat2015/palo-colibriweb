package at.adaptive.components.restriction;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

import at.adaptive.components.hibernate.AssociationPath;
import at.adaptive.components.session.BaseEntityHome;

/**
 * A Restriction is used to configure a criteria for sorting and filtering for a specified property
 * 
 * @author Bernhard Hablesreiter
 * 
 * @param <T>
 *            The type of the value
 * @see BaseEntityHome
 */
public interface Restriction<T>
{
	void addFulltextFilterContainer(FulltextFilterContainer fulltextFilterContainer);

	void addFulltextSearchTerm(FulltextSearchTerm searchTerm);

	/**
	 * Clears the dirty states of this restriction
	 */
	void clearDirty();

	/**
	 * Adds a criterion to the specified criteria
	 * 
	 * @param criteria
	 *            the criteria
	 */
	Criterion createCriterion() throws NoResultException;

	Criterion createCriterion(String value, RestrictionType restrictionType) throws NoResultException;

	/**
	 * Adds an order to the specified criteria
	 * 
	 * @param criteria
	 *            the criteria
	 */
	Order createOrder();

	/**
	 * Cycles ordering (ASC, DESC, NO_ORDER)
	 */
	void cycleOrder();

	/**
	 * Returns the handled object from a specified string value
	 * 
	 * @param value
	 *            the string value
	 * @param restrictionType
	 *            the restriction type
	 * @return the parsed object on success or <code>null</code> on error
	 */
	Object getAsObject(String value, RestrictionType restrictionType);

	/**
	 * Returns the association path of this restriction or <code>null</code> if the handled property is defined on the root entity class.<br>
	 * The association path is usually the "trimmed" property name (e.g. entity.field --> entity).
	 * 
	 * @return the association path of this restriction or <code>null</code> if the handled property is defined on the root entity class
	 */
	AssociationPath getAssociationPath();

	/**
	 * Returns a string representation of the specified object
	 * 
	 * @param value
	 *            the object
	 * @return a string representation of the specified object or <code>null</code> if the given value is null
	 */
	String getAsString(T value);

	T getBindedValue();

	String getEmbeddedPropertyName();

	String getFulltextFieldName();

	List<FulltextFilterContainer> getFulltextFilterContainers();

	String getFulltextIdPropertyName();

	List<FulltextSearchTerm> getFulltextSearchTerms();

	String[] getGroups();

	String getIdPropertyName();

	/**
	 * Returns the order type
	 * 
	 * @return the order type
	 */
	OrderType getOrderType();

	Class<?> getPropertyClass();

	/**
	 * Returns the property name
	 * 
	 * @return the property name
	 */
	String getPropertyName();

	/**
	 * Returns the restriction type
	 * 
	 * @return the restriction type
	 */
	RestrictionType getRestrictionType();

	Class<?> getSearchClass();

	Class<T> getType();

	/**
	 * Returns the value of this restriction
	 * 
	 * @return the value of this restriction
	 */
	String getValue();

	boolean isAppendWildcard();

	/**
	 * Returns whether current ordering is descending
	 * 
	 * @return <code>true</code> if current ordering is descending; <code>false</code> otherwise
	 */
	boolean isDescOrder();

	/**
	 * Returns whether the restriction is dirty
	 * 
	 * @return <code>true</code> if the restriction is dirty; <code>false</code> otherwise
	 */
	boolean isDirty();

	boolean isEmbeddedType();

	boolean isFulltext();

	boolean isFuzzy();

	boolean isGroupSet();

	/**
	 * Returns whether an order value is set
	 * 
	 * @return <code>true</code> if an order value is set; <code>false</code> otherwise
	 */
	boolean isOrder();

	/**
	 * Returns whether the order is dirty
	 * 
	 * @return <code>true</code> if the order is dirty; <code>false</code> otherwise
	 */
	boolean isOrderDirty();

	/**
	 * Returns whether ordering is enabled for this restriction
	 * 
	 * @return <code>true</code> if ordering is enabled for this restriction; <code>false</code> otherwise
	 */
	boolean isOrderEnabled();

	/**
	 * Returns whether a value is set
	 * 
	 * @return <code>true</code> if a value is set (i.e. is not empty); <code>false</code> otherwise
	 */
	boolean isValueSet();

	/**
	 * Resets the value, the bindedValue and the order of this restriction
	 */
	void reset();

	void resetBindedValue();

	/**
	 * Resets the order of this restriction
	 */
	void resetOrder();

	/**
	 * Resets the value of this restriction
	 */
	void resetValue();

	void setAppendWildcard(boolean appendWildcard);

	/**
	 * Sets ascending ordering
	 */
	void setAscOrder();

	void setBindedValue(T bindedValue);

	/**
	 * sets descending ordering
	 */
	void setDescOrder();

	void setEmbeddedPropertyName(String embeddedPropertyName);

	void setEmbeddedType(boolean embeddedType);

	void setFulltext(boolean fulltext);

	// void setEntityManager(EntityManager entityManager);

	void setFulltextFieldName(String fulltextFieldName);

	void setFulltextIdPropertyName(String fulltextIdPropertyName);

	void setFuzzy(boolean fuzzy);

	void setGroups(String[] groups);

	void setIdPropertyName(String idPropertyName);

	/**
	 * Sets the order enabled flag
	 * 
	 * @param orderEnabled
	 *            the order enabled flag
	 */
	void setOrderEnabled(boolean orderEnabled);

	void setPropertyClass(Class<?> propertyClass);

	/**
	 * Sets the property name
	 * 
	 * @param propertyName
	 *            the property name
	 */
	void setPropertyName(String propertyName);

	/**
	 * Sets the restriction type
	 * 
	 * @param restrictionType
	 *            the restriction type to set
	 */
	void setRestrictionType(RestrictionType restrictionType);

	/**
	 * Sets the search class to use
	 * 
	 * @param searchClass
	 *            the search class to use
	 */
	void setSearchClass(Class<?> searchClass);

	/**
	 * Sets the value of this restriction
	 * 
	 * @param value
	 *            the value to set
	 */
	void setValue(String value);
}
