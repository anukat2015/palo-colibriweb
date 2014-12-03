package at.adaptive.components.session;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

import at.adaptive.components.bean.annotation.QueryType;
import at.adaptive.components.common.CollectionUtil;
import at.adaptive.components.common.StringUtil;
import at.adaptive.components.hibernate.CriteriaWrapper;
import at.adaptive.components.restriction.BaseRestrictionGroup;
import at.adaptive.components.restriction.Restriction;
import at.adaptive.components.restriction.RestrictionGroup;
import at.adaptive.components.restriction.RestrictionManager;
import at.adaptive.components.restriction.RestrictionType;

public abstract class ResultSearchController<T> extends PaginationResultController<T> implements IResultSearchController<T>
{
	protected static final String ORDER_ASC = "asc";
	protected static final String ORDER_DESC = "desc";
	private static final String ORDER_NONE = "";
	private static final long serialVersionUID = 293055145848431282L;

	protected Map<String, RestrictionGroup> restrictionGroups;

	protected Map<String, Restriction<?>> restrictions;

	public void cycleRestrictionOrder(String name)
	{
		Restriction<?> restriction = getRestriction(name);
		restriction.cycleOrder();
		orderChanged(restriction);
	}

	public void filter()
	{
		getFirstPageResult();
	}

	@SuppressWarnings("unchecked")
	public List<T> getAllResults()
	{
		try
		{
			CriteriaWrapper criteriaWrapper = createCriteriaWrapper();
			applyCustomFilters(criteriaWrapper);
			applyOrders(criteriaWrapper);
			Criteria criteria = criteriaWrapper.createCriteria();
			return criteria.list();
		}
		catch(Exception e)
		{
			return Collections.EMPTY_LIST;
		}
	}

	public String getOrderValue(String restrictionName)
	{
		Restriction<?> restriction = getRestriction(restrictionName);
		if(restriction != null && restriction.isOrderEnabled())
		{
			if(restriction.isOrder())
			{
				if(restriction.isDescOrder())
				{
					return ORDER_DESC;
				}
				else
				{
					return ORDER_ASC;
				}
			}
		}
		return ORDER_NONE;
	}

	public Restriction<?> getRestriction(String name)
	{
		return restrictions.get(name);
	}

	public RestrictionGroup getRestrictionGroup(String name)
	{
		return restrictionGroups.get(name);
	}

	public RestrictionType[] getRestrictionTypes()
	{
		return RestrictionType.values();
	}

	@Override
	public Integer getResultCount()
	{
		if(isAnyRestrictionDirty() || isAnyRestrictionGroupDirty() || isDirty())
		{
			initResultCount();
			resetRestrictionsDirtyState();
			resetRestrictionGroupsDirtyState();
		}
		return super.getResultCount();
	}

	public boolean isRestrictionSet()
	{
		if(restrictions != null)
		{
			for(Iterator<String> iterator = restrictions.keySet().iterator(); iterator.hasNext();)
			{
				Restriction<?> restriction = restrictions.get(iterator.next());
				if(restriction.isValueSet())
				{
					return true;
				}
			}
		}
		return false;
	}

	public void resetRestrictionGroups()
	{
		if(restrictionGroups != null)
		{
			for(Iterator<String> iterator = restrictionGroups.keySet().iterator(); iterator.hasNext();)
			{
				RestrictionGroup restrictionGroup = restrictionGroups.get(iterator.next());
				restrictionGroup.clear();
			}
		}
	}

	public void resetRestrictions()
	{
		if(restrictions != null)
		{
			for(Iterator<String> iterator = restrictions.keySet().iterator(); iterator.hasNext();)
			{
				Restriction<?> restriction = restrictions.get(iterator.next());
				restriction.reset();
			}
		}
		setDefaultRestrictions();
	}

	@Override
	public ScrollableResults scrollResults()
	{
		try
		{
			CriteriaWrapper criteriaWrapper = createCriteriaWrapper();
			applyCustomFilters(criteriaWrapper);
			applyOrders(criteriaWrapper);
			Criteria criteria = criteriaWrapper.createCriteria();
			return criteria.scroll(ScrollMode.FORWARD_ONLY);
		}
		catch(NoResultException e)
		{
			return null;
		}
	}

	public void setRestrictionAscOrder(String name)
	{
		Restriction<?> restriction = getRestriction(name);
		restriction.setAscOrder();
		orderChanged(restriction);
	}

	public void setRestrictionDescOrder(String name)
	{
		Restriction<?> restriction = getRestriction(name);
		restriction.setDescOrder();
		orderChanged(restriction);
	}

	/**
	 * Applies custom restriction params to the specified restrictions. Subclasses may implement their own logic here.<br>
	 * This default implementation does nothing
	 * 
	 * @param restriction
	 *            the restriction
	 */
	protected void applyCustomRestrictionParams(Restriction<?> restriction)
	{}

	/**
	 * Applies orders to the criteria wrapper, using the handled restrictions
	 * 
	 * @param criteriaWrapper
	 *            the {@link CriteriaWrapper} to use
	 */
	protected void applyOrders(CriteriaWrapper criteriaWrapper)
	{
		if(restrictions != null)
		{
			for(Iterator<String> iterator = restrictions.keySet().iterator(); iterator.hasNext();)
			{
				Restriction<?> restriction = restrictions.get(iterator.next());
				if(restriction.isOrderEnabled() && restriction.isOrder())
				{
					Order order = restriction.createOrder();
					criteriaWrapper.addOrder(restriction.getAssociationPath(), order);
					// addOrder(criteria, restriction);
				}
			}
		}
	}

	/**
	 * Applies restriction groupts to the specified {@link CriteriaWrapper}
	 * 
	 * @param criteriaWrapper
	 *            the {@link CriteriaWrapper}
	 */
	protected void applyRestrictionGroups(CriteriaWrapper criteriaWrapper) throws NoResultException
	{
		if(restrictionGroups != null)
		{
			for(Iterator<String> iterator = restrictionGroups.keySet().iterator(); iterator.hasNext();)
			{
				RestrictionGroup restrictionGroup = restrictionGroups.get(iterator.next());
				if(restrictionGroup.isValueSet())
				{
					Criterion criterion = restrictionGroup.createCriterion(getSession(), getEntityClass());
					if(criterion != null)
					{
						criteriaWrapper.addCriterion(criterion);
					}
				}
			}
		}
	}

	/**
	 * Applies restrictions to the specified criteria, using the handled restrictions
	 * 
	 * @param criteria
	 *            the criteria
	 */
	protected void applyRestrictions(CriteriaWrapper criteriaWrapper) throws NoResultException
	{
		if(restrictions != null)
		{
			for(Iterator<String> iterator = restrictions.keySet().iterator(); iterator.hasNext();)
			{
				Restriction<?> restriction = restrictions.get(iterator.next());
				applyCustomRestrictionParams(restriction);
				if(restriction.isValueSet())
				{
					Criterion criterion = restriction.createCriterion();
					criteriaWrapper.addCriterion(restriction.getAssociationPath(), criterion);
					// addCriterion(criteria, restriction);
				}
			}
		}
	}

	/**
	 * Creates the restriction groups
	 */
	protected void createRestrictionGroups()
	{
		restrictionGroups = new HashMap<String, RestrictionGroup>();
		if(restrictions != null)
		{
			for(Iterator<String> iterator = restrictions.keySet().iterator(); iterator.hasNext();)
			{
				Restriction<?> restriction = restrictions.get(iterator.next());
				if(restriction.isGroupSet())
				{
					for(String groupName : restriction.getGroups())
					{
						RestrictionGroup restrictionGroup;
						if(restrictionGroups.containsKey(groupName))
						{
							restrictionGroup = restrictionGroups.get(groupName);
						}
						else
						{
							restrictionGroup = new BaseRestrictionGroup();
							restrictionGroup.setName(groupName);
							restrictionGroup.setQueryType(getRestrictionGroupQueryType(groupName));
							restrictionGroup.setRestrictionType(getRestrictionGroupRestrictionType(groupName));
						}
						restrictionGroup.addRestriction(restriction);
						setRestrictionGroupParameters(restrictionGroup);
						restrictionGroups.put(groupName, restrictionGroup);
					}
				}
			}
		}
	}

	/**
	 * Creates the restrictions
	 */
	protected void createRestrictions()
	{
		restrictions = RestrictionManager.instance().createRestrictions(getEntityClass());
		if(restrictions != null)
		{
			for(Iterator<String> iterator = restrictions.keySet().iterator(); iterator.hasNext();)
			{
				Restriction<?> restriction = restrictions.get(iterator.next());
				if(restriction.getRestrictionType() == null)
				{
					restriction.setRestrictionType(getDefaultRestrictionType(restriction));
				}
			}
		}
	}

	/**
	 * Returns the default restriction type to use.
	 * <p>
	 * This implementation returns {@link RestrictionType#NO_RESTRICTION } or {@link RestrictionType#FULLTEXT} if this restriction is a fulltext-restriction<br>
	 * Subclasses may override
	 * 
	 * @return the default restriction type
	 */
	protected RestrictionType getDefaultRestrictionType(Restriction<?> restriction)
	{
		if(restriction.isFulltext())
		{
			return RestrictionType.FULLTEXT;
		}
		return RestrictionType.NO_RESTRICTION;
	}

	/**
	 * Returns the query type for a restriction group matching the specified name. Subclasses may override. <br>
	 * This default implementation always returns {@link QueryType#DISJUNCTION}
	 * 
	 * @param groupName
	 *            the name of the restriction group
	 * @return the query type for the specified restriction group
	 */
	protected QueryType getRestrictionGroupQueryType(String groupName)
	{
		return QueryType.DISJUNCTION;
	}

	/**
	 * Returns the restriction type for a restriction group matching the specified name. Subclasses may override. <br>
	 * This default implementation always returns {@link RestrictionType#ILIKE}
	 * 
	 * @param groupName
	 *            the name of the restriction group
	 * @return the restriction type for the specified restriction group
	 */
	protected RestrictionType getRestrictionGroupRestrictionType(String groupName)
	{
		return RestrictionType.ILIKE;
	}

	/**
	 * Initializes the result count
	 */
	protected void initResultCount()
	{
		try
		{
			CriteriaWrapper criteriaWrapper = createCriteriaWrapper();
			applyRestrictions(criteriaWrapper);
			applyRestrictionGroups(criteriaWrapper);
			applyCustomFilters(criteriaWrapper);
			resultCount = criteriaWrapper.countResults();
		}
		catch(NoResultException e)
		{
			resultCount = 0;
		}
	}

	/**
	 * Returns whether the handled orders are dirty
	 * 
	 * @return <code>true</code> if the handled orders are dirty; <code>false</code> otherwise
	 */
	protected boolean isAnyOrderDirty()
	{
		if(restrictions == null)
		{
			return false;
		}
		for(Iterator<String> iterator = restrictions.keySet().iterator(); iterator.hasNext();)
		{
			Restriction<?> restriction = restrictions.get(iterator.next());
			if(restriction.isOrderDirty())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether the handled restrictions are dirty
	 * 
	 * @return <code>true</code> if the handled restrictions are dirty; <code>false</code> otherwise
	 */
	protected boolean isAnyRestrictionDirty()
	{
		if(restrictions == null)
		{
			return false;
		}
		for(Iterator<String> iterator = restrictions.keySet().iterator(); iterator.hasNext();)
		{
			Restriction<?> restriction = restrictions.get(iterator.next());
			if(restriction.isDirty())
			{
				return true;
			}
		}
		return false;
	}

	protected boolean isAnyRestrictionGroupDirty()
	{
		if(restrictionGroups == null)
		{
			return false;
		}
		for(Iterator<String> iterator = restrictionGroups.keySet().iterator(); iterator.hasNext();)
		{
			RestrictionGroup restrictionGroup = restrictionGroups.get(iterator.next());
			if(restrictionGroup.isDirty())
			{
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean isDirty()
	{
		return super.isDirty() || isAnyRestrictionDirty() || isAnyRestrictionGroupDirty() || isAnyOrderDirty();
	}

	/**
	 * This method is called whenever a order has been changed. Subclasses may override.<br>
	 * This default implementation resets all other order values
	 * 
	 * @param restriction
	 *            the changed restriction
	 */
	protected void orderChanged(Restriction<?> restriction)
	{
		if(restrictions != null)
		{
			for(Iterator<String> iterator = restrictions.keySet().iterator(); iterator.hasNext();)
			{
				Restriction<?> currentRestriction = restrictions.get(iterator.next());
				if(!currentRestriction.equals(restriction))
				{
					currentRestriction.resetOrder();
				}
			}
		}
		// query first page
		getFirstPageResult();
	}

	/**
	 * Queries for results
	 */
	@SuppressWarnings("unchecked")
	protected void queryResults()
	{
		if(isQueryResults())
		{
			CriteriaWrapper criteriaWrapper = createCriteriaWrapper();
			try
			{
				if(isEvictOldResults())
				{
					evictOldResults();
				}
				applyRestrictions(criteriaWrapper);
				applyOrders(criteriaWrapper);
				applyCustomFilters(criteriaWrapper);
				applyRestrictionGroups(criteriaWrapper);
				Criteria criteria = criteriaWrapper.createCriteria();
				// page switch
				// if(results != null && results.size() == 1 && firstResult != null && firstResult > getMaxResults())
				// {
				// setFirstResult(firstResult - getMaxResults());
				// }
				if(getFirstResult() != null)
				{
					criteria.setFirstResult(getFirstResult());
				}
				if(!StringUtil.isEmpty(getMaxResults()))
				{
					criteria.setMaxResults(getMaxResults());
				}
				try {
					results = criteria.list();
				}
				catch (org.hibernate.exception.GenericJDBCException e) {
					if (e.getCause() != null && e.getCause().getMessage().startsWith("ERROR: canceling statement due to statement timeout")) {
						this.addWarnMessage("Abfrage wegen Zeitüberschreitung abgebrochen.");
						resultCount = 0;
						results = Collections.EMPTY_LIST;
					}
					else throw e;
				}
				if(CollectionUtil.isEmpty(results))
				{
					resultCount = 0;
				}
				else if(resultCount == null || isDirty() || isInstanceChanged())
				{
					initResultCount();
					// if(results.size() < getMaxResults())
					// {
					// resultCount = results.size();
					// }
					// else
					// {
					// initResultCount();
					// }
				}
			}
			catch(NoResultException e)
			{
				resultCount = 0;
				results = Collections.EMPTY_LIST;
			}
			finally
			{
				handleDirtyState();
				setInstanceChanged(false);
				setDirty(false);
				resetRestrictionsDirtyState();
				resetRestrictionGroupsDirtyState();
				handleQueryComplete();
				info("Objects in session: " + getSession().getStatistics().getEntityCount());

				if(0 != getSession().getStatistics().getCollectionCount())

				info("Collections in session: " + getSession().getStatistics().getCollectionCount());

			}
		}
	}

	/**
	 * Resets the dirty state of the handled restriction groups
	 */
	protected void resetRestrictionGroupsDirtyState()
	{
		if(restrictionGroups != null)
		{
			for(Iterator<String> iterator = restrictionGroups.keySet().iterator(); iterator.hasNext();)
			{
				RestrictionGroup restrictionGroup = restrictionGroups.get(iterator.next());
				restrictionGroup.clearDirty();
			}
		}
	}

	/**
	 * Resets the dirty state of the handled restrictions
	 */
	protected void resetRestrictionsDirtyState()
	{
		if(restrictions != null)
		{
			for(Iterator<String> iterator = restrictions.keySet().iterator(); iterator.hasNext();)
			{
				Restriction<?> restriction = restrictions.get(iterator.next());
				restriction.clearDirty();
			}
		}
	}

	/**
	 * Sets default restrictions to be used after the component has been created. This method is called at creation time and whenever {@link #resetRestrictions()} is called.
	 * <p>
	 * Subclasses my override
	 */
	protected void setDefaultRestrictions()
	{}

	/**
	 * Sets parameters on the specified {@link RestrictionGroup}. This default implementation does nothing. Subclasses may override
	 * 
	 * @param restrictionGroup
	 *            the {@link RestrictionGroup}
	 */
	protected void setRestrictionGroupParameters(RestrictionGroup restrictionGroup)
	{}
}
