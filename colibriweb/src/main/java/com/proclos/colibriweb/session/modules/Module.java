package com.proclos.colibriweb.session.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.model.SelectItem;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.hql.internal.ast.ASTQueryTranslatorFactory;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.hql.spi.QueryTranslatorFactory;
import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.security.AuthorizationException;

import at.adaptive.components.common.CollectionUtil;
import at.adaptive.components.common.StringUtil;
import at.adaptive.components.hibernate.AliasContainer;
import at.adaptive.components.hibernate.CriteriaWrapper;
import at.adaptive.components.restriction.Restriction;
import at.adaptive.components.restriction.RestrictionGroup;
import at.adaptive.components.restriction.RestrictionType;
import at.adaptive.components.session.BaseEntityHome;

import com.proclos.colibriweb.common.ContextProperties;
import com.proclos.colibriweb.common.IAccessControlled;
import com.proclos.colibriweb.common.ISelector;
import com.proclos.colibriweb.entity.BaseEntity;
import com.proclos.colibriweb.entity.user.ColibriUser;
import com.proclos.colibriweb.session.common.ColibriEvents;
import com.proclos.colibriweb.session.system.AccessUtil;
import com.proclos.colibriweb.session.system.DirtyStateManager;
import com.proclos.colibriweb.session.system.EntityUtil;
import com.proclos.colibriweb.session.system.ModuleManager;
import com.proclos.colibriweb.session.system.config.DependencyProperty;
import com.proclos.colibriweb.session.system.config.FilterCondition;
import com.proclos.colibriweb.session.system.config.FilterCriteria;
import com.proclos.colibriweb.session.system.config.FilterDefinition;
import com.proclos.colibriweb.session.system.config.FilterGroup;
import com.proclos.colibriweb.session.system.config.ModuleConfig;
import com.proclos.colibriweb.session.system.config.ModuleConfigException;
import com.proclos.colibriweb.session.system.config.FilterGroup.FilterModes;

@Synchronized(timeout = Integer.MAX_VALUE)
public abstract class Module<T extends BaseEntity> extends BaseEntityHome<T, T> implements IModule<T>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1725579975739405311L;


	@RequestParameter
	private String id;

	@In
	protected DirtyStateManager dirtyStateManager;

	@In
	protected ModuleManager moduleManager;

	private String handledId = null;

	protected boolean inCreation = false;

	private boolean dependenciesMet = true;
	private ModuleConfig config;
	private List<DependencyProperty> unatisfiedDependencyProperties;
	protected T lockedInstance;
	private boolean downloadInProgress;
	private boolean clearEntityManager = false;
	private String sortValue;
	private String sortMode = ORDER_DESC;
	private Integer maxEntityResults = 25;
	private ISelector selector;

	public void setDirty(boolean dirty)
	{
		super.setDirty(dirty);
		/*
		if (dirty) {
			setFirstResult(null);
		}
		*/
	}
	
	public void activate(T instance)
	{
		if(instance.getId() == null)
		{
			return;
		}
		instance = getEntityManager().merge(instance);
		EntityUtil.getInstance().setField(instance, "active", Boolean.TRUE, getActivateFieldNames());
	}

	public void activateAndPersist(T instance)
	{
		if(instance.getId() == null)
		{
			return;
		}
		instance = getEntityManager().merge(instance);
		activate(instance);
		getEntityManager().persist(instance);
		getEntityManager().flush();
		setDirty(true);
		handleLastElementRemoved();
	}

	@Observer(ColibriEvents.PERSIST)
	public void anyInstancePersisted()
	{
		//nothing to do
	}

	public void cancelCreation()
	{
		dependenciesMet = true;
		inCreation = false;
	}

	public void checkDependencies()
	{
		dependenciesMet = moduleManager.checkDependencies(this);
	}

	public void checkInstance()
	{
		if(id == null && handledId == null && inCreation)
		{
			return;
		}
		if(id == null)
		{
			if(handledId != null)
			{
				return;
			}
			if(isManaged())
			{
				id = getId().toString();
				handledId = id;
				return;
			}
			// throw new AuthorizationException(messages.get("notAuthorized"));
		}
		else if(!isLongType(id))
		{
			throw new AuthorizationException(messages.get("notAuthorized"));
		}
		if(id != null)
		{
			// if(hasIdChanged())
			// {
			// load instance
			select(Long.parseLong(id));
			// set handled id
			handledId = id;
			// }
		}
		else
		{
			createNewInstance();
		}
	}
	
	protected void postSelectSecurityCheck(T object) throws AuthorizationException {
		if (object instanceof IAccessControlled) {
			if (!AccessUtil.hasAccess(getCurrentUser(), (IAccessControlled)object)) {
				throw new AuthorizationException(messages.get("notAuthorized"));
			}
		}
	}
	
	public boolean isAuthorized(T object) {
		if (object instanceof IAccessControlled) {
			return AccessUtil.hasAccess(getCurrentUser(), (IAccessControlled)object);
		}
		return true;
	}
	
	public boolean maySetInstancePrivate() {
		if (getInstance() instanceof IAccessControlled) {
			return AccessUtil.maySetPrivate(getCurrentUser(), (IAccessControlled)getInstance());
		}
		return false;
	}

	public void clearAll()
	{
		clearInCreation();
		clearInstance();
		// TODO CSCHW: clear locked instance without updating dependent modules, since this may result in a locking timeout. Check if this leads to OK results.
		lockedInstance = null;
		handledId = null;
		// reset search
		resetRestrictionGroups();
		// clearLockedInstance();
		// getEntityManager().clear();

	}

	public void clearEntityManager()
	{
	// clearEntityManager = true;
	}

	public void clearInCreation()
	{
		inCreation = false;
	}

	@Override
	public void clearLockedInstance()
	{
		lockedInstance = null;
		// von MK hinzugefügt
		handledId = null;
		updateDependentModules();
	}

	@Override
	public String createNewInstance()
	{
		return createNewInstance(false);
	}

	public String createNewInstanceCheckInCreation()
	{
		return createNewInstance(true);
	}

	public void deactivate(T instance)
	{
		if(instance.getId() == null)
		{
			return;
		}
		instance = getEntityManager().merge(instance);
		EntityUtil.getInstance().setField(instance, "active", Boolean.FALSE, getDeactivateFieldNames());
	}

	public void deactivateAndPersist(T instance)
	{
		if(instance.getId() == null)
		{
			return;
		}
		if(!getEntityManager().contains(instance))
		{
			instance = getEntityManager().merge(instance);
		}
		deactivate(instance);
		getEntityManager().persist(instance);
		getEntityManager().flush();
		setDirty(true);
		handleLastElementRemoved();
	}

	@Destroy
	public void destroy()
	{
		deregister();
	}

	public void downloadResultsStarted()
	{
		downloadInProgress = false;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof Module<?>)) return false;
		Module<?> other = (Module<?>)obj;
		if(getName() == null)
		{
			if(other.getName() != null) return false;
		}
		else if(!getName().equals(other.getName())) return false;
		return true;
	}
	
	protected boolean equalsEntity(BaseEntity e1, BaseEntity e2) {
		if (e1 == null && e2 == null) return true;
		if (e1 == null && e2 != null) return false;
		if (e1 != null && e2 == null) return false;
		if (e1.getId() != null && e2.getId() != null) return e1.getId().longValue() == e2.getId().longValue();
		return e1.equals(e2);
	}

	public List<FilterGroup<?>> getAvailableFilterGroups()
	{
		return config.getFilters();
	}

	@Override
	public ModuleConfig getConfig()
	{
		return config;
	}

	public abstract Class<T> getEntityClass();

	@Override
	public String getExportFileName()
	{
		return getConfig().getLabel();
	}

	public T getInstance()
	{
		T instance = super.getInstance();
		/*
		 * if(instance != null && isIdDefined() && !getEntityManager().contains(instance)) { instance = getEntityManager().merge(instance); setInstance(instance); setId(instance.getId()); }
		 */
		return instance;
	}

	public String getLabel()
	{
		return config.getLabel();
	}

	@SuppressWarnings("unchecked")
	public T getLockedInstance()
	{
		if(lockedInstance != null && lockedInstance.getId() != null && !getEntityManager().contains(lockedInstance))
		{
			try
			{
				lockedInstance = getEntityManager().merge(lockedInstance);
				lockedInstance.initialize();
			}
			catch(OptimisticLockException ole)
			{
				try
				{
					lockedInstance = (T)getEntityManager().find(lockedInstance.getClass(), lockedInstance.getId());

				}
				catch(ClassCastException cce)
				{
					cce.printStackTrace();

				}

				getEntityManager().refresh(lockedInstance);
				setLockedInstance(lockedInstance);
				lockedInstance.initialize();

			}
		}
		return lockedInstance;
	}

	public String getName()
	{
		try
		{
			if(getConfig() != null)
			{
				return getConfig().getName();
			}
			String name = getClass().getAnnotation(Name.class).value();
			name = name.substring(0, name.lastIndexOf("Module"));
			return name;
		}
		catch(Exception e)
		{
			error("Could not get component name", e);
			return null;
		}
	}

	public List<DependencyProperty> getUnsatisfiedDependencyProperties()
	{
		return unatisfiedDependencyProperties;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	public void initModule(ModuleConfig config) throws ModuleConfigException
	{
		this.config = config;
		if (config != null && config.getSorterValues() != null && !config.getSorterValues().isEmpty()) {
			sortValue = config.getSorterValues().get(0).getValue().toString();
		}
	}

	public boolean isAdministrable()
	{
		return config.isAdministrable();
	}

	public boolean isDependenciesMet()
	{
		return dependenciesMet;
	}

	public boolean isDownloadInProgress()
	{
		return downloadInProgress;
	}

	public boolean isInCreation()
	{
		return inCreation;
	}

	@Override
	public boolean isItemSelected()
	{
		return isManaged();
	}

	public boolean isManaged()
	{
		return getInstance() != null && isIdDefined();
	}

	public boolean isNavigable()
	{
		return config.isNavigable();
	}

	public boolean isSelectable()
	{
		List<String> dependencies = config.getDependencies();
		for(String d : dependencies)
		{
			IModule<?> m = moduleManager.getModuleForName(d);
			if(m == null || !m.isItemSelected()) return false;
		}
		return true;
	}

	@Override
	public String persist()
	{
		boolean managed = isManaged();
		if(managed && !getEntityManager().contains(getInstance()))
		{
			setInstance(getEntityManager().merge(getInstance()));
		}
		String result = super.persist();
		if(!managed && isRefreshAfterPersist())
		{
			refreshInstance();
			raiseEvent(ColibriEvents.SELECT, getConfig().getName(), getId());
		}
		return result;
	}

	public String persistAndClear()
	{
		String result = persist();
		clear();
		return result;
	}

	public <S extends BaseEntity> void persistEntity(S entity)
	{
		persistEntity(entity, true, true);
	}

	public <S extends BaseEntity> void persistEntity(S entity, boolean addMessage, boolean flush)
	{
		if(isManaged() && !getEntityManager().contains(entity))
		{
			entity = getEntityManager().merge(entity);
		}
		getEntityManager().persist(entity);
		if(flush)
		{
			getEntityManager().flush();
		}
		if(addMessage)
		{
			updatedMessage();
		}
	}
	
	public String remove()
	{
		String res = super.remove();
		clearInstance();
		clearLockedInstance();
		return res;
	}

	public String reselect()
	{
		if(!isManaged())
		{
			return "failed";
		}
		return select(getId());
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
		for(FilterGroup<?> filterGroup : getAvailableFilterGroups())
		{
			filterGroup.reset();
		}
		setDefaultRestrictions();
	}

	@Override
	public void setDirty()
	{
		super.setDirty();
		setDirty(true);
	}

	public void setLockedInstance(T lockedInstance)
	{
		this.lockedInstance = lockedInstance;
	}

	@Override
	public String update()
	{
		setInstance(getEntityManager().merge(getInstance()));
		return super.update();
	}

	@Override
	protected void applyCustomFilters(CriteriaWrapper criteriaWrapper)
	{
		super.applyCustomFilters(criteriaWrapper);
		// add filter by conditions
		List<Criterion> filterRestrictions = getEffectiveFilterRestrictions();
		for(Criterion criterion : filterRestrictions)
		{
			criteriaWrapper.addCriterion(criterion);
		}
		// add filter by criteria
		for(FilterGroup<?> filterGroup : getAvailableFilterGroups())
		{
			List<FilterDefinition> filters = filterGroup.getEffectiveFilters();
			List<Criterion> groupCriteria = new ArrayList<Criterion>();
			boolean noCriteriaSpecified = false;
			Map<String, AliasContainer> filterAliases = new HashMap<String, AliasContainer>();
			for(FilterDefinition filterDefinition : filters)
			{
				FilterCriteria criteria = filterDefinition.getCriteria();
				if(criteria != null)
				{
					criteria.replacePlaceholder(filterGroup.getValueForDefinition(filterDefinition.getName()));
					groupCriteria.add(criteria.getCriterion(this.getEntityClass()));
					filterAliases.putAll(criteria.getAllJoinAliases());
				}
				else noCriteriaSpecified = true;
			}
			if(!(FilterModes.or.equals(filterGroup.getMode()) && noCriteriaSpecified) && !groupCriteria.isEmpty()) // add a junction for this group, if needed
			{
				Junction junction = getJunction(filterGroup);
				for(Criterion c : groupCriteria)
				{
					junction.add(c);
				}
				criteriaWrapper.addAliasMap(filterAliases);
				criteriaWrapper.addCriterion(junction);
			}
		}
		if (sortValue != null) {
			criteriaWrapper.clearOrders();
			if (ORDER_ASC.equals(sortMode)) {
				criteriaWrapper.addOrder(Order.asc(sortValue));
			}
			else {
				criteriaWrapper.addOrder(Order.desc(sortValue));
			}
		}
	}

	@Override
	protected void applyCustomRestrictionParams(Restriction<?> restriction)
	{
		restriction.setAppendWildcard(true);
	}

	protected void clear()
	{
		clearInstance();
	}

	protected T convertExportResult(T object)
	{
		return object;
	}

	protected String createNewInstance(boolean checkInCreation)
	{
		if(!inCreation || !checkInCreation)
		{
			inCreation = true;
			createNewInstanceSecurityCheck();
			clearInstance();
			clearLockedInstance();
			clear();
			updateDependencyProperties();
		}
		unatisfiedDependencyProperties = moduleManager.getUnsatisfiedDependencyProperties(this);
		checkDependencies();
		handleEvent(ColibriEvents.CREATE);
		if(!isDependenciesMet())
		{
			return "dependenciesNotMet";
		}
		setNewInstanceProperties();
		instanceCreated();
		return getCreatedOutcome();
	}

	protected void deregister()
	{
		dirtyStateManager.deregisterModule(this);
	}

	@Override
	protected void evictOldResults()
	{
		if(CollectionUtil.isNotEmpty(results))
		{
			Session session = getSession();
			for(T result : results)
			{
				if(lockedInstance != null)
				{
					if(result.equals(lockedInstance))
					{
						continue;
					}
				}
				if(instance != null)
				{
					if(result.equals(instance))
					{
						continue;
					}
				}
				session.evict(result);
			}
		}
	}

	@Override
	protected void exportStarted()
	{
		downloadInProgress = true;
	}

	protected Set<String> getActivateFieldNames()
	{
		return new HashSet<String>();
	}

	protected String getConfigProperty(String key)
	{
		return getConfig().getProperties().getProperty(key);
	}

	// private int countCharacter(String string, String character)
	// {
	// int result = 0;
	// int start = string.indexOf(character, 0);
	// while(start != -1)
	// {
	// result++;
	// start = string.indexOf(character, start);
	// }
	// return result;
	// }

	// @Override
	// protected String getPersistedOutcome()
	// {
	// return null;
	// }

	protected String getConfigProperty(String key, String defaultValue)
	{
		return getConfig().getProperties().getProperty(key, defaultValue);
	}

	protected ColibriUser getCurrentUser()
	{
		try
		{
			return (ColibriUser)Contexts.getSessionContext().get(ContextProperties.USER);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	protected Set<String> getDeactivateFieldNames()
	{
		return new HashSet<String>();
	}

	@Override
	protected RestrictionType getDefaultRestrictionType(Restriction<?> restriction)
	{
		return RestrictionType.FULLTEXT;
	}

	protected List<Criterion> getEffectiveFilterRestrictions()
	{
		List<Criterion> result = new ArrayList<Criterion>();
		for(FilterGroup<?> filterGroup : getAvailableFilterGroups())
		{
			List<Criterion> groupCriteria = new ArrayList<Criterion>();
			List<FilterDefinition> filters = filterGroup.getEffectiveFilters();
			for(FilterDefinition filterDefinition : filters)
			{
				FilterCondition condition = filterDefinition.getCondition();
				if(condition != null && !condition.getDefinition().isEmpty())
				{
					groupCriteria.add(getSQLRestriction(getEffectiveQuery(condition), filterGroup, filterDefinition));
				}
			}
			if(!groupCriteria.isEmpty())
			{
				Junction junction = getJunction(filterGroup);
				for(Criterion c : groupCriteria)
				{
					junction.add(c);
				}
				result.add(junction);
			}
		}
		return result;
	}

	@Override
	protected Object getIdFromString(String id)
	{
		try
		{
			return Long.parseLong(id);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	protected Junction getJunction(FilterGroup<?> filterGroup)
	{
		switch(filterGroup.getJsfType())
		{
			case selectmanycheckbox:
			{
				if(FilterModes.or.equals(filterGroup.getMode())) return Restrictions.disjunction();
				else return Restrictions.conjunction();
			}
			case selectmanydate:
			{
				if(FilterModes.or.equals(filterGroup.getMode())) return Restrictions.disjunction();
				else return Restrictions.conjunction();
			}
			default:
				return Restrictions.conjunction();
		}
	}

	@Override
	protected RestrictionType getRestrictionGroupRestrictionType(String groupName)
	{
		return RestrictionType.FULLTEXT;
	}

	@SuppressWarnings("unchecked")
	protected <E> E getSingleResult(String query)
	{
		try
		{
			return (E)getEntityManager().createQuery(query).getSingleResult();
		}
		catch(NoResultException nre)
		{
			return null;
		}
	}

	protected void handleEvent(String event)
	{
		// let the module manager handle the event
		moduleManager.handleEvent(event, this);
	}

	@Override
	protected T handleNotFound()
	{
		clearInstance();
		return super.handleNotFound();
	}

	protected void initialize()
	{
		super.initialize();
		moduleManager.initModule(this);
		dirtyStateManager.registerModule(this);
	}

	@Override
	protected void initializeColumnDefinitionContainer()
	{
		if(getConfig() != null && getConfig().hasExportConfiguration())
		{
			super.initializeColumnDefinitionContainer();
			getColumnDefinitionContainer().setColumnDefinitions(getConfig().getColumnDefinitions());
		}
	}

	@Override
	protected void instanceCreated()
	{
		super.instanceCreated();
		if(getConfig().isLockable())
		{
			lockInstance(getInstance());
		}
		checkDependencies();
		updateDependentModules();
		handleEvent(ColibriEvents.CREATED);
	}

	@Override
	protected void instanceDeleted()
	{
		super.instanceDeleted();
		updateDependentModules();
		handleEvent(ColibriEvents.DELETE);
	}

	@Override
	protected void instancePersisted()
	{
		super.instancePersisted();
		inCreation = false;
		// refreshInstance();
		updateDependentModules();
		handleEvent(ColibriEvents.PERSIST);
	}

	@Override
	protected void instanceSelected()
	{
		super.instanceSelected();
		getInstance().initialize();
		// clearInCreation();
		inCreation = false;
		if(getConfig().isLockable())
		{
			lockInstance(instance);
		}
		dependenciesMet = true;
		unatisfiedDependencyProperties = null;
		handleEvent(ColibriEvents.SELECT);
	}

	protected boolean isAnyRestrictionDirty()
	{
		boolean restrictionDirty = super.isAnyRestrictionDirty();
		if(!restrictionDirty)
		{
			for(FilterGroup<?> filterGroup : getAvailableFilterGroups())
			{
				if(filterGroup.isDirty())
				{
					return true;
				}
			}
		}
		return restrictionDirty;
	}

	@Override
	protected boolean isEvictOldResults()
	{
		// XXX - PERFORMANCE
		return false;
	}

	@Override
	protected boolean isImmediateExport()
	{
		return false;
	}

	protected boolean isRefreshAfterPersist()
	{
		return false;
	}

	protected void lockInstance(T instance)
	{
		lockedInstance = instance;
	}

	@Override
	protected void preSelect()
	{

	// XXX - PERFORMANCE
	// if(lockedInstance != null)
	// {
	// getSession().evict(lockedInstance);
	// }
	// if(instance != null)
	// {
	// getSession().evict(instance);
	// }
	}

	protected void queryResults()
	{
		if(clearEntityManager)
		{
			getEntityManager().clear();
			clearEntityManager = false;
		}
		super.queryResults();
		for(T result : results)
		{
			result.initialize();
		}
	}

	protected void raiseAfterTransactionSuccessEvent()
	{
		try
		{
			super.raiseAfterTransactionSuccessEvent();
		}
		catch(Exception e)
		{
			debug(e.getMessage());
		}
	}

	protected void resetRestrictionsDirtyState()
	{
		super.resetRestrictionsDirtyState();
		for(FilterGroup<?> filterGroup : getAvailableFilterGroups())
		{
			filterGroup.clearDirty();
		}
	}

	@Override
	protected void setDefaultRestrictions()
	{
		super.setDefaultRestrictions();
		getRestriction("modificationDate").setDescOrder();
	}

	@Override
	protected void setRestrictionGroupParameters(RestrictionGroup restrictionGroup)
	{
		super.setRestrictionGroupParameters(restrictionGroup);
		restrictionGroup.setUseSubSelectIdFetching(true);
		if (getConfig() != null)
			restrictionGroup.setMinInputChars(getConfig().getMinSearchChars());
	}

	protected String toSql(String hqlQueryText)
	{
		if(hqlQueryText != null && hqlQueryText.trim().length() > 0)
		{
			final QueryTranslatorFactory translatorFactory = new ASTQueryTranslatorFactory();
			final SessionFactoryImplementor factory = (SessionFactoryImplementor)getSession().getSessionFactory();
			final QueryTranslator translator = translatorFactory.createQueryTranslator(hqlQueryText, hqlQueryText, Collections.EMPTY_MAP, factory);
			translator.compile(Collections.EMPTY_MAP, false);
			return translator.getSQLString();
		}
		return null;
	}

	protected void updateDependencyProperties()
	{
		moduleManager.updateDependencyProperties(this);
	}

	protected void updateDependentModules()
	{
		moduleManager.updateDependentModules(this);
	}

	private String getEffectiveQuery(FilterCondition condition)
	{
		String prefix = "";
		switch(condition.getLanguage())
		{
			case hql:
				prefix = (condition.isShortNotation() ? "select id from " + getEntityClass().getSimpleName() + " where " : "");
				break;
			case sql:
				prefix = (condition.isShortNotation() ? "select id from " : "");
				break;
			default:
				break;
		}
		String queryString = prefix + condition.getDefinition();
		queryString = resolveExpressions(queryString);
		switch(condition.getLanguage())
		{
			case hql:
				queryString = toSql(queryString);
				break;
			default:
				break;
		}
		return queryString;
	}

	private Criterion getSQLRestriction(String sqlString, FilterGroup<?> g, FilterDefinition f)
	{
		if(!sqlString.contains("?"))
		{
			return Restrictions.sqlRestriction("{alias}.id IN (" + sqlString + ")");
		}
		else
		{
			int size = StringUtil.countChars(sqlString, '?');
			Object value = g.getValueForDefinition(f.getName());
			Type type = new TypeResolver().heuristicType(value.getClass().getCanonicalName());
			Object[] values = new Object[size];
			Type[] types = new Type[size];
			for(int i = 0; i < size; i++)
			{
				values[i] = value;
				types[i] = type;
			}
			return Restrictions.sqlRestriction("{alias}.id IN (" + sqlString + ")", values, types);
		}
	}


	private boolean isLongType(String id)
	{
		try
		{
			Long.parseLong(id);
			return true;
		}
		catch(NumberFormatException nfe)
		{
			return false;
		}
	}

	private String resolveExpressions(String value)
	{
		ValueExpression<Object> valueExpression = Expressions.instance().createValueExpression(value);
		return (String)valueExpression.getValue();
	}
	
	public void filter(String searchValue) {
		int minDigits = (getConfig() != null) ? getConfig().getMinSearchChars() : 2;
		if (searchValue != null && searchValue.length() >= minDigits) filter();
	}
	
	public void updateForm() {
		//do nothing by default
	}

	public void setSortValue(String sortValue) {
		this.sortValue = sortValue;
		for (SelectItem s : getConfig().getSorterValues()) { //check for preset mode
			if (sortValue != null && sortValue.equals(s.getValue()) && s.getDescription() != null) {
				this.sortMode = s.getDescription();
				break;
			}
		}
	}

	public String getSortValue() {
		return sortValue;
	}

	public void setSortMode(String sortMode) {
		this.sortMode = sortMode;
	}

	public String getSortMode() {
		return sortMode;
	}
	
	public void sort() {
		results = null;
		filter();
	}

	public void setMaxEntityResults(Integer maxResults) {
		this.maxEntityResults = maxResults;
	}

	public Integer getMaxEntityResults() {
		return maxEntityResults;
	}
	
	public Integer getMaxResults() {
		return (maxEntityResults == null || maxEntityResults == 0) ? Integer.MAX_VALUE : maxEntityResults;
	}
	
	public void setSelectorName(String selectorName) {
	}

	public String getSelectorName() {
		return (selector != null) ? selector.getName() : null;
	}
	
	public ISelector getSelector() {
		return selector;
	}

	public void setSelector(ISelector selector) {
		this.selector = selector;
		setDirty();
	}
	
	public void close() {
		Redirect.instance().setViewId("/modules/"+config.getPath()+"/list.xhtml");
		Redirect.instance().execute();
	}
	
	public String getListIcon(T instance) {
		return "/img/modules/"+getConfig().getPath()+"/icon.png";
	}

}