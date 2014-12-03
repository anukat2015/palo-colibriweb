package at.adaptive.components.restriction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextFilter;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;

import at.adaptive.components.common.StringUtil;
import at.adaptive.components.hibernate.AssociationPath;

/**
 * Base implementation of the Restriction interface
 * 
 * @author Bernhard Hablesreiter
 * 
 * @param <T>
 *            The restriction value type
 */
public abstract class AbstractRestriction<T> implements Restriction<T>
{
	private static final Logger logger = Logger.getLogger(Restriction.class);

	protected T bindedValue;

	protected boolean bindedValueDirty = false;

	protected boolean dirty = false;

	protected String embeddedPropertyName;

	protected boolean embeddedType;

	protected boolean fulltext;

	protected boolean fuzzy;

	protected String fulltextFieldName;

	protected List<FulltextSearchTerm> fulltextSearchTerms;

	protected List<FulltextFilterContainer> fulltextFilterContainers;

	protected String[] groups;

	protected String idPropertyName;

	protected String fulltextIdPropertyName;

	protected boolean orderDirty = false;

	protected boolean orderEnabled;

	protected OrderType orderType;

	protected Class<?> propertyClass;

	protected String propertyName;

	protected RestrictionType restrictionType;

	protected boolean restrictionTypeDirty = false;

	protected Class<?> searchClass;

	protected String value;

	protected boolean appendWildcard = false;

	private ValueExpression<EntityManager> entityManager;

	/**
	 * Creates a new instance of AbstractRestriction
	 * 
	 */
	public AbstractRestriction()
	{
		super();
		this.orderType = OrderType.NO_ORDER;
		initFulltextSearchTerms();
		initFulltextFilterContainers();
		entityManager = Expressions.instance().createValueExpression("#{entityManager}", EntityManager.class);
	}

	public void addFulltextFilterContainer(FulltextFilterContainer fulltextFilterContainer)
	{
		if(!fulltextFilterContainers.contains(fulltextFilterContainer))
		{
			fulltextFilterContainers.add(fulltextFilterContainer);
		}
	}

	public void addFulltextSearchTerm(FulltextSearchTerm searchTerm)
	{
		if(!fulltextSearchTerms.contains(searchTerm))
		{
			fulltextSearchTerms.add(searchTerm);
		}
	}

	public void clearDirty()
	{
		dirty = false;
		bindedValueDirty = false;
		restrictionTypeDirty = false;
		orderDirty = false;
	}

	public Criterion createCriterion() throws NoResultException
	{
		Criterion criterion = createCriterion(value, restrictionType);
		// if(criterion != null)
		// {
		// updateCriteria(criteria, propertyName, criterion);
		// }
		return criterion;
	}

	public Criterion createCriterion(String stringValue, RestrictionType restrictionType) throws NoResultException
	{
		if(restrictionType != null && !restrictionType.equals(RestrictionType.NO_RESTRICTION))
		{
			if(!StringUtil.isEmpty(stringValue) || bindedValue != null)
			{
				String propertyName = trimPropertyName();
				if(isFulltext() && restrictionType.equals(RestrictionType.FULLTEXT))
				{
					return createFulltextCriterion(stringValue, getSearchClass(), getIdPropertyName(), getFulltextFieldName());
				}
				Object value;
				if(bindedValue != null)
				{
					value = bindedValue;
				}
				else
				{
					restrictionType = getSupportedRestrictionType(restrictionType);
					value = getAsObject(getModifiedValue(stringValue, restrictionType), restrictionType);
				}
				if(isCreateCustomCriterion())
				{
					return createCustomCriterion(value, restrictionType);
				}
				if(value != null)
				{
					if(restrictionType.equals(RestrictionType.EQ))
					{
						if(value instanceof String)
						{
							return Restrictions.like(propertyName, value);
						}
						else
						{
							return Restrictions.eq(propertyName, value);
						}
					}
					else if(restrictionType.equals(RestrictionType.NE))
					{
						return Restrictions.ne(propertyName, value);
					}
					else if(restrictionType.equals(RestrictionType.GT))
					{
						return Restrictions.gt(propertyName, value);
					}
					else if(restrictionType.equals(RestrictionType.GTE))
					{
						return Restrictions.or(Restrictions.eq(propertyName, value), Restrictions.gt(propertyName, value));
					}
					else if(restrictionType.equals(RestrictionType.ILIKE))
					{
						return Restrictions.ilike(propertyName, value);
					}
					else if(restrictionType.equals(RestrictionType.CONTAINS) || restrictionType.equals(RestrictionType.FULLTEXT))
					{
						return Restrictions.ilike(propertyName, value);
					}
					else if(restrictionType.equals(RestrictionType.LIKE))
					{
						return Restrictions.like(propertyName, value);
					}
					else if(restrictionType.equals(RestrictionType.LT))
					{
						return Restrictions.lt(propertyName, value);
					}
					else if(restrictionType.equals(RestrictionType.LTE))
					{
						return Restrictions.or(Restrictions.eq(propertyName, value), Restrictions.lt(propertyName, value));
					}
				}
				else
				{
					return null;
				}
			}
			if(restrictionType.equals(RestrictionType.NULL))
			{
				return Restrictions.isNull(propertyName);
			}
			else if(restrictionType.equals(RestrictionType.NOT_NULL))
			{
				return Restrictions.isNotNull(propertyName);
			}
		}
		return null;
	}

	public Order createOrder()
	{
		String trimmedPropertyName = trimPropertyName();
		if(!isOrderEnabled() || !isOrder())
		{
			return null;
		}
		Order order;
		if(isDescOrder())
		{
			order = Order.desc(trimmedPropertyName);
		}
		else
		{
			order = Order.asc(trimmedPropertyName);
		}
		return order;
		// updateCriteria(criteria, propertyName, order);
	}

	public void cycleOrder()
	{
		OrderType orderType;
		if(!isOrder())
		{
			orderType = OrderType.ASC;
		}
		else
		{
			if(isDescOrder())
			{
				orderType = OrderType.NO_ORDER;
			}
			else
			{
				orderType = OrderType.DESC;
			}
		}
		setOrderDirty(this.orderType, orderType);
		this.orderType = orderType;
	}

	public AssociationPath getAssociationPath()
	{
		AssociationPath associationPath = new AssociationPath();
		int index = propertyName.lastIndexOf(".");
		if(index != -1)
		{
			List<String> embeddedPropertyNames = null;
			if(isEmbeddedType())
			{
				embeddedPropertyNames = new ArrayList<String>(1);
				embeddedPropertyNames.add(embeddedPropertyName);
			}
			associationPath.addEntries(propertyName.substring(0, index), embeddedPropertyNames);
			// if(isEmbeddedType())
			// {
			// // check for the last "."
			// int index2 = propertyName.substring(0, index).lastIndexOf(".");
			// if(index2 == -1)
			// {
			// // // found an association path
			// // return propertyName.substring(0, index);
			// // }
			// // else
			// // {
			// // // property is on the root entity class
			// return null;
			// }
			// }
			//
			// return propertyName.substring(0, index);
		}
		return associationPath;
	}

	public T getBindedValue()
	{
		return bindedValue;
	}

	public String getEmbeddedPropertyName()
	{
		return embeddedPropertyName;
	}

	public String getFulltextFieldName()
	{
		return fulltextFieldName;
	}

	public List<FulltextFilterContainer> getFulltextFilterContainers()
	{
		return fulltextFilterContainers;
	}

	public String getFulltextIdPropertyName()
	{
		return fulltextIdPropertyName;
	}

	public List<FulltextSearchTerm> getFulltextSearchTerms()
	{
		return fulltextSearchTerms;
	}

	public String[] getGroups()
	{
		return groups;
	}

	public String getIdPropertyName()
	{
		return idPropertyName;
	}

	public OrderType getOrderType()
	{
		return orderType;
	}

	public Class<?> getPropertyClass()
	{
		return propertyClass;
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

	public String getValue()
	{
		return value;
	}

	public boolean isAppendWildcard()
	{
		return appendWildcard;
	}

	public boolean isDescOrder()
	{
		return orderType.equals(OrderType.DESC);
	}

	public boolean isDirty()
	{
		return dirty || bindedValueDirty || restrictionTypeDirty;
	}

	public boolean isEmbeddedType()
	{
		return embeddedType;
	}

	public boolean isFulltext()
	{
		return fulltext;
	}

	public boolean isFuzzy()
	{
		return fuzzy;
	}

	public boolean isGroupSet()
	{
		if(groups == null || groups.length == 0)
		{
			return false;
		}
		for(String group : groups)
		{
			if(group != null && group.length() > 0)
			{
				return true;
			}
		}
		return false;
	}

	public boolean isOrder()
	{
		return !orderType.equals(OrderType.NO_ORDER);
	}

	public boolean isOrderDirty()
	{
		return orderDirty;
	}

	public boolean isOrderEnabled()
	{
		return orderEnabled;
	}

	public boolean isValueSet()
	{
		return !StringUtil.isEmpty(value) || bindedValue != null;
	}

	public void reset()
	{
		resetValue();
		resetBindedValue();
		resetOrder();
	}

	public void resetBindedValue()
	{
		setBindedValueDirty(bindedValue, null);
		setBindedValue(null);
	}

	public void resetOrder()
	{
		setOrderDirty(orderType, OrderType.NO_ORDER);
		orderType = OrderType.NO_ORDER;
	}

	public void resetValue()
	{
		setDirty(value, null);
		setValue(null);
	}

	public void setAppendWildcard(boolean appendWildcard)
	{
		this.appendWildcard = appendWildcard;
	}

	public void setAscOrder()
	{
		setOrderDirty(orderType, OrderType.ASC);
		orderType = OrderType.ASC;
	}

	public void setBindedValue(T bindedValue)
	{
		setBindedValueDirty(this.bindedValue, bindedValue);
		this.bindedValue = bindedValue;
	}

	public void setDescOrder()
	{
		setOrderDirty(orderType, OrderType.DESC);
		orderType = OrderType.DESC;
	}

	public void setEmbeddedPropertyName(String embeddedPropertyName)
	{
		this.embeddedPropertyName = embeddedPropertyName;
	}

	public void setEmbeddedType(boolean embeddedType)
	{
		this.embeddedType = embeddedType;
	}

	public void setFulltext(boolean fulltext)
	{
		this.fulltext = fulltext;
	}

	public void setFulltextFieldName(String fulltextFieldName)
	{
		this.fulltextFieldName = fulltextFieldName;
	}

	// public void setEntityManager(EntityManager entityManager)
	// {
	// this.entityManager = entityManager;
	// }

	public void setFulltextIdPropertyName(String fulltextIdPropertyName)
	{
		this.fulltextIdPropertyName = fulltextIdPropertyName;
	}

	public void setFuzzy(boolean fuzzy)
	{
		this.fuzzy = fuzzy;
	}

	public void setGroups(String[] groups)
	{
		this.groups = groups;
	}

	public void setIdPropertyName(String idPropertyName)
	{
		this.idPropertyName = idPropertyName;
	}

	public void setOrderEnabled(boolean orderEnabled)
	{
		this.orderEnabled = orderEnabled;
	}

	public void setPropertyClass(Class<?> propertyClass)
	{
		this.propertyClass = propertyClass;
	}

	public void setPropertyName(String propertyName)
	{
		this.propertyName = propertyName;
	}

	public void setRestrictionType(RestrictionType restrictionType)
	{
		setRestrictionTypeDirty(this.restrictionType, restrictionType);
		this.restrictionType = restrictionType;
	}

	// protected void updateCriteria(Criteria criteria, String propertyName, Criterion criterion)
	// {
	// if(propertyName.indexOf('.') > 0)
	// {
	// String currentPropertyName = propertyName.substring(0, propertyName.indexOf('.'));
	// criteria = criteria.createCriteria(currentPropertyName);
	// propertyName = propertyName.substring(propertyName.indexOf('.') + 1);
	// updateCriteria(criteria, propertyName, criterion);
	// }
	// else
	// {
	// criteria.add(criterion);
	// }
	// }

	// protected void updateCriteria(Criteria criteria, String propertyName, Order order)
	// {
	// if(propertyName.indexOf('.') > 0)
	// {
	// String currentPropertyName = propertyName.substring(0, propertyName.indexOf('.'));
	// criteria = criteria.createCriteria(currentPropertyName, Criteria.LEFT_JOIN);
	// propertyName = propertyName.substring(propertyName.indexOf('.') + 1);
	// updateCriteria(criteria, propertyName, order);
	// }
	// else
	// {
	// criteria.addOrder(order);
	// }
	// }

	public void setSearchClass(Class<?> searchClass)
	{
		this.searchClass = searchClass;
	}

	public void setValue(String value)
	{
		setDirty(this.value, value);
		this.value = value;
	}

	@Override
	public String toString()
	{
		return getPropertyName();
	}

	/**
	 * Creates a custom criterion for the specified object and restrictionType. Subclasses may override
	 * <p>
	 * This method may be extended in order to intercept the generic creation of the criterion
	 * 
	 * @param value
	 *            the object
	 * @param restrictionType
	 *            the restrictionType
	 * @return the created criterion or <code>null</code> if no custom criterion should be used
	 */
	protected Criterion createCustomCriterion(Object value, RestrictionType restrictionType)
	{
		return null;
	}

	protected Criterion createFulltextCriterion(String value, Class<?> searchClass, String idColumnName, String propertyName) throws NoResultException
	{
		Object[] fulltextIds = getFulltextIds(value, searchClass, idColumnName, propertyName);
		if(fulltextIds == null || fulltextIds.length == 0)
		{
			throw new NoResultException();
		}
		return Restrictions.in(idColumnName, fulltextIds);
	}

	protected String createQueryString(String fieldName, String value, List<FulltextSearchTerm> fulltextSearchTerms)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(fieldName);
		sb.append(":");
		sb.append(value);
		if(appendWildcard && !value.endsWith("*") && !fuzzy)
		{
			sb.append("*");
		}
		if(fuzzy)
		{
			sb.append("~0.6");
		}
		for(FulltextSearchTerm searchTerm : fulltextSearchTerms)
		{
			sb.append(" " + getDefaultOperator() + " ");
			if(searchTerm.isApplyCurrentFieldName())
			{
				String trimmedFieldName = trimFieldName(fieldName);
				if(trimmedFieldName != null)
				{
					sb.append(trimmedFieldName);
					sb.append(".");
				}
			}
			sb.append(searchTerm.getLuceneQueryString());
		}
		return sb.toString();
	}

	/**
	 * Returns the default analyzer for this fulltext field converter<br>
	 * This default implementation always returns a {@link StandardAnalyzer}. Subclasses may overwrite
	 * 
	 * @return the default analyzer for this fulltext field descriptor
	 */
	protected Analyzer getDefaultAnalyzer()
	{
		return new StandardAnalyzer(Version.LUCENE_36);
	}

	/**
	 * Returns the default operator for this fulltext field converter<br>
	 * This default implementation always returns {@link Operator#AND}. Subclasses may overwrite
	 * 
	 * @return the default operator for this fulltext field descriptor
	 */
	protected Operator getDefaultOperator()
	{
		return Operator.AND;
	}

	protected EntityManager getEntityManager()
	{
		return entityManager.getValue();
	}

	protected FullTextEntityManager getFulltextEntityManager()
	{
		return Search.getFullTextEntityManager(getEntityManager());
	}

	protected Object[] getFulltextIds(String value, Class<?> searchClass, String idColumnName, String fieldName)
	{
		try
		{
			String queryString = createQueryString(fieldName, value, fulltextSearchTerms);
			if(queryString.length() == 0)
			{
				return null;
			}
			QueryParser queryParser = getQueryParser(fieldName);
			queryParser.setAllowLeadingWildcard(true);
			queryParser.setDefaultOperator(getDefaultOperator());
			Query luceneQuery = queryParser.parse(queryString);
			FullTextEntityManager fullTextEntityManager = getFulltextEntityManager();
			FullTextQuery fulltextQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, searchClass);
			for(FulltextFilterContainer fulltextFilterContainer : fulltextFilterContainers)
			{
				FullTextFilter fullTextFilter = fulltextQuery.enableFullTextFilter(fulltextFilterContainer.getFulltextFilter());
				for(Iterator<String> iterator = fulltextFilterContainer.getParameters().keySet().iterator(); iterator.hasNext();)
				{
					String parameterName = iterator.next();
					Object parameterValue = fulltextFilterContainer.getParameters().get(parameterName);
					fullTextFilter = fullTextFilter.setParameter(parameterName, parameterValue);
				}
			}
			BooleanQuery.setMaxClauseCount(32768);
			Object[] fulltextIds = getIdsFromFulltextQueryResult(idColumnName, fulltextQuery);
			return fulltextIds;
		}
		catch(ParseException pe)
		{
			if(logger.isInfoEnabled())
			{
				logger.info("Encountered lucene parse exception. User input is not parsable. ", pe);
			}
			else
			{
				logger.warn("Encountered lucene parse exception. User input is not parsable. Swallowing exception");
			}
			return null;
		}
		catch(Exception e)
		{
			logger.error("Error getting fulltext ids", e);
			return null;
		}
	}

	/**
	 * Returns a list of objects from a specified fulltext query
	 * 
	 * @param idColumnName
	 *            the column name of the id value
	 * @param fulltextQuery
	 *            the fulltext query
	 * @return a list of objects from the specified fulltext query
	 * @throws Exception
	 *             on error
	 */
	@SuppressWarnings("unchecked")
	protected Object[] getIdsFromFulltextQueryResult(String idColumnName, FullTextQuery fulltextQuery) throws Exception
	{
		fulltextQuery.setProjection(idColumnName);
		List<Object[]> searchResults = fulltextQuery.getResultList();
		List<Object> fulltextResults = new ArrayList<Object>(searchResults.size());
		for(Object[] searchResult : searchResults)
		{
			fulltextResults.add(searchResult[0]);
		}
		return fulltextResults.toArray(new Object[fulltextResults.size()]);
	}

	/**
	 * Returns the modified value according to the specified restriction type
	 * <p>
	 * This implementation returns the unmodified value. Subclasses may implement their own logic here
	 * 
	 * @param value
	 *            the input value
	 * @param restrictionType
	 *            the restriction type
	 * @return the modified value
	 */
	protected String getModifiedValue(String value, RestrictionType restrictionType)
	{
		return value;
	}

	/**
	 * Returns a query parser for specified query fields
	 * 
	 * @param queryFields
	 *            the query fields
	 * @return a query parser for the specified query fields
	 */
	protected QueryParser getQueryParser(String queryField)
	{
		return new QueryParser(Version.LUCENE_36,queryField, getDefaultAnalyzer());
	}

	protected RestrictionType getSupportedRestrictionType(RestrictionType restrictionType)
	{
		return restrictionType;
	}

	protected void initFulltextFilterContainers()
	{
		fulltextFilterContainers = new ArrayList<FulltextFilterContainer>(1);
	}

	protected void initFulltextSearchTerms()
	{
		fulltextSearchTerms = new ArrayList<FulltextSearchTerm>(1);
	}

	/**
	 * Returns whether this restriction is creating a custom criterion
	 * 
	 * @return <code>true</code> if this restriction is creating a custom criterion; <code>false</code> otherwise
	 */
	protected boolean isCreateCustomCriterion()
	{
		return false;
	}

	/**
	 * Returns whether the specified values are equal
	 * 
	 * @param <U>
	 *            the paremeter type
	 * @param oldValue
	 *            the old value
	 * @param newValue
	 *            the new value
	 * @return <code>true</code> if the values are not equal; <code>false</code> otherwise
	 */
	protected <U> boolean isDirty(U oldValue, U newValue)
	{
		if(oldValue == null && newValue == null)
		{
			return false;
		}
		if(oldValue == null || newValue == null)
		{
			return true;
		}
		return !oldValue.equals(newValue);
	}

	/**
	 * Sets the dirty flag for the binded value
	 * 
	 * @param oldValue
	 *            the old value
	 * @param newValue
	 *            the new value
	 */
	protected <U> void setBindedValueDirty(U oldValue, U newValue)
	{
		bindedValueDirty = isDirty(oldValue, newValue);
	}

	/**
	 * Sets the dirty flag for the value
	 * 
	 * @param oldValue
	 *            the old value
	 * @param newValue
	 *            the new value
	 */
	protected void setDirty(String oldValue, String newValue)
	{
		dirty = isDirty(oldValue, newValue);
	}

	/**
	 * Sets the dirty flag for the order value
	 * 
	 * @param oldValue
	 *            the old value
	 * @param newValue
	 *            the new value
	 */
	protected void setOrderDirty(OrderType oldValue, OrderType newValue)
	{
		orderDirty = isDirty(oldValue, newValue);
	}

	/**
	 * Sets the dirty flag for the restriction type
	 * 
	 * @param oldValue
	 *            the old value
	 * @param newValue
	 *            the new value
	 */
	protected void setRestrictionTypeDirty(RestrictionType oldValue, RestrictionType newValue)
	{
		restrictionTypeDirty = isDirty(oldValue, newValue);
	}

	protected String trimFieldName(String fieldName)
	{
		int index = fieldName.lastIndexOf('.');
		if(index > 0)
		{
			return fieldName.substring(0, index);
		}
		else
		{
			return null;
		}
	}

	private String trimPropertyName()
	{
		if(isEmbeddedType() && StringUtil.countChars(propertyName, '.') == 1)
		{
			return propertyName;
		}
		int index = propertyName.lastIndexOf('.');
		if(index > 0)
		{
			return propertyName.substring(index + 1);
		}
		else
		{
			return propertyName;
		}
	}
}