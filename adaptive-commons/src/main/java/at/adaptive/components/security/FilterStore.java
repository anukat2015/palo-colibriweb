package at.adaptive.components.security;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.persistence.Filter;
import org.jboss.seam.persistence.PersistenceProvider;
import org.jboss.seam.util.Conversions;

@Name("at.adaptive.security.filterStore")
@Install(precedence = FRAMEWORK)
@Scope(APPLICATION)
@BypassInterceptors
public class FilterStore implements Serializable
{
	private static final long serialVersionUID = -3076084513666393167L;

	protected Map<String, List<DatabaseFilter>> cache = new HashMap<String, List<DatabaseFilter>>(100);

	private static final String QUERY = "from DatabaseFilter where filterName=:filterName";

	protected List<Filter> filters;

	protected ValueExpression<EntityManager> entityManager;

	@Create
	public void init()
	{
		if(entityManager == null)
		{
			entityManager = Expressions.instance().createValueExpression("#{entityManager}", EntityManager.class);
		}
		initFilters();
	}

	@SuppressWarnings("unchecked")
	protected void initFilters()
	{
		filters = new ArrayList<Filter>();
		Context applicationContext = Contexts.getApplicationContext();
		if(applicationContext == null)
		{
			return;
		}
		Map<String, Conversions.PropertyValue> properties = (Map<String, Conversions.PropertyValue>)applicationContext.get(Component.PROPERTIES);
		Conversions.PropertyValue propertyValue = properties.get("entityManager.filters");
		if(propertyValue != null)
		{
			String[] expressions = propertyValue.getMultiValues();
			for(String expression : expressions)
			{
				Filter filter = (Filter)Expressions.instance().createValueExpression(expression).getValue();
				if(filter != null)
				{
					filters.add(filter);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<DatabaseFilter> listFilters(String filterName)
	{
		List<DatabaseFilter> filterList;
		if(cache.containsKey(filterName))
		{
			filterList = cache.get(filterName);
		}
		else
		{
			filterList = createFilterQuery(filterName).getResultList();
			cache.put(filterName, filterList);
		}
		return filterList;
	}

	protected Query createFilterQuery(String filterName)
	{
		return lookupEntityManager().createQuery(QUERY).setParameter("filterName", filterName);
	}

	protected EntityManager lookupEntityManager()
	{
		return entityManager.getValue();
	}

	public ValueExpression<EntityManager> getEntityManager()
	{
		return entityManager;
	}

	public void setEntityManager(ValueExpression<EntityManager> expression)
	{
		this.entityManager = expression;
	}

	public void clearCache()
	{
		cache.clear();
	}

	public List<Filter> getDefinedFilters()
	{
		return filters;
	}

	public void enableDefinedFilters(EntityManager entityManager)
	{
		for(Filter filter : filters)
		{
			if(filter.isFilterEnabled())
			{
				PersistenceProvider.instance().enableFilter(filter, entityManager);
			}
		}
	}
}
