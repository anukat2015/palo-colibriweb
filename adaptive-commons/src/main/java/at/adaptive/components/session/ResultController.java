package at.adaptive.components.session;

import java.util.Collections;
import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import at.adaptive.components.common.CollectionUtil;
import at.adaptive.components.common.StringUtil;
import at.adaptive.components.hibernate.CriteriaWrapper;

public abstract class ResultController<T> extends EntityController<T> implements IResultController<T>
{
	private static final int DEFAULT_MAX_RESULTS = 10;

	private static final long serialVersionUID = -7043711166572411348L;

	protected boolean dirty;

	protected Integer firstResult;

	protected Integer resultCount;

	protected List<T> results;

	@Override
	public void delete(Object id)
	{
		super.delete(id);
		setDirty(true);
	}

	@SuppressWarnings("unchecked")
	public List<T> getAllResults()
	{
		try
		{
			CriteriaWrapper criteriaWrapper = createCriteriaWrapper();
			applyCustomFilters(criteriaWrapper);
			Criteria criteria = criteriaWrapper.createCriteria();
			return criteria.list();
		}
		catch(Exception e)
		{
			return Collections.EMPTY_LIST;
		}
	}

	public Integer getFirstResult()
	{
		if(firstResult == null)
		{
			firstResult = 0;
		}
		return firstResult;
	}

	public Integer getMaxResults()
	{
		return DEFAULT_MAX_RESULTS;
	}

	public Integer getResultCount()
	{
		return resultCount;
	}

	public List<T> getResults()
	{
		if(results == null || isDirty() || isInstanceChanged())
		{
			queryResults();
		}
		return results;
	}

	@Override
	public String persist()
	{
		String res = super.persist();
		setDirty(true);
		return res;
	}

	public void reloadResults()
	{
		setInstanceChanged(true);
	}

	@Override
	public String remove()
	{
		String res = super.remove();
		setDirty(true);
		return res;
	}

	public ScrollableResults scrollResults()
	{
		try
		{
			CriteriaWrapper criteriaWrapper = createCriteriaWrapper();
			applyCustomFilters(criteriaWrapper);
			Criteria criteria = criteriaWrapper.createCriteria();
			return criteria.scroll(ScrollMode.FORWARD_ONLY);
		}
		catch(NoResultException e)
		{
			return null;
		}
	}

	public void setDirty(boolean dirty)
	{
		this.dirty = dirty;
		// CSCHW: clear results if dirty to save memory
		/*
		 * if (dirty && results != null) { for (T r : results) getEntityManager().detach(r); results.clear(); }
		 */
	}

	@Override
	public String update()
	{
		String res = super.update();
		setDirty(true);
		return res;
	}

	/**
	 * Applies custom filters to the specified criteria. Subclasses can implement their own filter-logic here
	 * <p>
	 * Please note: the specified criteria should be handled with care. To avoid "duplicate association path"-errors add restrictions the following way: <blockquote>
	 * 
	 * <pre>
	 * {@link CriteriaWrapper#addCriterion(Restrictions.eq(&quot;myProperty&quot;, &quot;myValue&quot;))};
	 * </pre>
	 * 
	 * </blockquote> or <blockquote>
	 * 
	 * <pre>
	 * {@link CriteriaWrapper#addCriterion(&quot;mySubEntity&quot;, Restrictions.eq(&quot;myProperty&quot;, &quot;myValue&quot;))};
	 * </pre>
	 * 
	 * </blockquote> to create an association
	 * <p>
	 * This default implementation does nothing
	 * 
	 * @param criteriaWrapper
	 *            the {@link CriteriaWrapper} to use
	 */
	protected void applyCustomFilters(CriteriaWrapper criteriaWrapper)
	{}

	/**
	 * Creates a new criteria for the handled entity type
	 * 
	 * @return the created criteria
	 */
	protected Criteria createCriteria()
	{
		return createCriteria(getEntityClass());
	}

	/**
	 * Creates a criteria for a specified class
	 * 
	 * @param clazz
	 *            the class
	 * @return the created criteria
	 */
	protected Criteria createCriteria(Class<?> clazz)
	{
		Session session = (Session)getEntityManager().getDelegate();
		return session.createCriteria(clazz);
	}

	/**
	 * Creates a new {@link CriteriaWrapper}
	 * 
	 * @return a created {@link CriteriaWrapper}
	 */
	protected CriteriaWrapper createCriteriaWrapper()
	{
		CriteriaWrapper criteriaWrapper = new CriteriaWrapper(getSession(), getEntityClass());
		setCriteriaWrapperProperties(criteriaWrapper);
		return criteriaWrapper;
	}

	/**
	 * This method is called before the {@link #queryResults()} method execution. All previously loaded results are being evicted from the session cache
	 */
	protected void evictOldResults()
	{
		if(CollectionUtil.isNotEmpty(results))
		{
			Session session = getSession();
			for(T result : results)
			{
				session.evict(result);
			}
		}
	}

	/**
	 * Returns the hibernate session
	 * 
	 * @return the hibernate session
	 */
	protected Session getSession()
	{
		return (Session)getEntityManager().getDelegate();
	}

	/**
	 * Handles the dirty state of this entity home
	 */
	protected void handleDirtyState()
	{}

	@Override
	protected void handleInstanceChanged()
	{
	// queryResults();
	}

	/**
	 * This method is called after the {@link #queryResults()} method is called. Subclasses may implement their own logic here.
	 */
	protected void handleQueryComplete()
	{}

	/**
	 * Initializes the result count
	 */
	protected void initResultCount()
	{
		try
		{
			CriteriaWrapper criteriaWrapper = createCriteriaWrapper();
			applyCustomFilters(criteriaWrapper);
			// Criteria criteria = criteriaWrapper.createCriteria(true);
			resultCount = criteriaWrapper.countResults();
		}
		catch(NoResultException e)
		{
			resultCount = 0;
		}
	}

	/**
	 * Returns whether this result controller is dirty
	 * 
	 * @return <code>true</code> if this result controller is dirty; <code>false</code> otherwise
	 */
	protected boolean isDirty()
	{
		return dirty;
	}

	protected boolean isEvictOldResults()
	{
		return false;
	}

	/**
	 * Returns whether the results should be queried. Subclasses may implement their own logic here
	 * <p>
	 * This default implementation always returns <code>true</code>
	 * 
	 * @return <code>true</code> if the results should be queried; <code>false</code> otherwise
	 */
	protected boolean isQueryResults()
	{
		return true;
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
				applyCustomFilters(criteriaWrapper);
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
				results = criteria.list();
				if(CollectionUtil.isEmpty(results))
				{
					resultCount = 0;
				}
				else if(resultCount == null || isDirty() || isInstanceChanged())
				{
					// if(results.size() < getMaxResults())
					// {
					// resultCount = results.size();
					// }
					// else
					// {
					initResultCount();
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
				handleQueryComplete();
			}
		}
	}

	/**
	 * Sets custom properties on a newly created criteria wrapper. Subclasses should implement their own logic here.<br>
	 * This method is called whenever a new criteria wrapper is created using the {@link #createCriteriaWrapper()} factory method.<br>
	 * A good reason to hook into this method is to enable the id property sub-select fetching. In this case, the id property name and the sub-select fetching flag must be set.<br>
	 * This default implementation does nothing
	 * 
	 * @param criteriaWrapper
	 * @see CriteriaWrapper#setUseSubSelectIdFetching(boolean)
	 * @see CriteriaWrapper#setIdPropertyName(String)
	 */
	protected void setCriteriaWrapperProperties(CriteriaWrapper criteriaWrapper)
	{}

	/**
	 * Sets the first result
	 * 
	 * @param firstResult
	 *            the first result to set
	 */
	protected void setFirstResult(Integer firstResult)
	{
		this.firstResult = firstResult;
	}
}
