package com.proclos.colibriweb.session.system;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.log.Log;

import at.adaptive.components.common.CollectionUtil;
import at.adaptive.components.common.ELUtil;

import com.proclos.colibriweb.common.ContextProperties;
import com.proclos.colibriweb.session.common.ColibriEvents;
import com.proclos.colibriweb.session.modules.IModule;
import com.proclos.colibriweb.session.system.config.DependencyAction;
import com.proclos.colibriweb.session.system.config.DependencyProperty;
import com.proclos.colibriweb.session.system.config.DependencyRestriction;
import com.proclos.colibriweb.session.system.config.ModuleConfig;
import com.proclos.colibriweb.session.system.config.ModuleConfigException;
import com.proclos.colibriweb.session.system.config.ModuleContainer;
import com.proclos.colibriweb.session.system.config.ModuleManagerConfig;
import com.proclos.colibriweb.session.system.config.ModuleManagerConfigHandler;

@Name("moduleManager")
@Scope(ScopeType.SESSION)
@AutoCreate
@Synchronized(timeout = Integer.MAX_VALUE)
public class ModuleManager implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3889879493673614969L;

	@Logger
	private Log logger;

	private List<IModule<?>> modules;
	private List<IModule<?>> navigableModules;
	private List<IModule<?>> administrableModules;
	private List<ModuleContainer> moduleContainers;
	private Map<String, ModuleConfig> configLookup = new HashMap<String, ModuleConfig>();
	
	@In 
	private EntityManager entityManager;

	@In
	private DirtyStateManager dirtyStateManager;

	public static ModuleManager instance()
	{
		if(!Contexts.isApplicationContextActive())
		{
			throw new IllegalStateException("No active application context");
		}
		ModuleManager instance = (ModuleManager)Component.getInstance(ModuleManager.class, ScopeType.SESSION);
		if(instance == null)
		{
			throw new IllegalStateException("No ModuleManager could be created");
		}
		return instance;
	}

	public boolean checkDependencies(IModule<?> module)
	{
		List<String> dependencies = module.getConfig().getDependencies();
		if(CollectionUtil.isNotEmpty(dependencies))
		{
			for(String dependency : dependencies)
			{
				logger.info("Checking dependency: " + dependency);
				// IModule<?> currentModule = getModuleForName(dependency);
				if(!checkDependencyProperties(module, dependency))
				{
					return false;
				}
				// check for additional restrictions
				if(!checkDependencyRestrictions(module, dependency))
				{
					return false;
				}
			}
		}
		return true;
	}

	@Create
	public void create()
	{
		ModuleManagerConfigHandler moduleManagerConfigHandler = (ModuleManagerConfigHandler)Component.getInstance("moduleManagerConfigHandler");
		
		modules = new ArrayList<IModule<?>>();
		navigableModules = new ArrayList<IModule<?>>();
		administrableModules = new ArrayList<IModule<?>>();
		moduleContainers = new ArrayList<ModuleContainer>();
		ModuleManagerConfig moduleManagerConfig = moduleManagerConfigHandler.getModuleManagerConfig();
		for(ModuleConfig moduleConfig : moduleManagerConfig.getModuleConfigs())
		{
			configLookup.put(moduleConfig.getName(), moduleConfig);
		}
		for(ModuleConfig moduleConfig : moduleManagerConfig.getModuleConfigs())
		{
			try
			{
				IModule<?> module = createModuleInstance(moduleConfig);
				if(module != null)
				{
					if(moduleConfig.isAdministrable() || moduleConfig.isNavigable())
					{
						if(module.isNavigable())
						{
							navigableModules.add(module);
						}
						else if(module.isAdministrable())
						{
							administrableModules.add(module);
						}
						addModuleToContainerList(module);
					}
					if(((Scope)module.getClass().getAnnotation(Scope.class)).value().equals(ScopeType.SESSION) && !modules.contains(module))
					{
						modules.add(module);
					}
				}
			}
			catch(ModuleConfigException e)
			{
				logger.error(e.getMessage());
			}
		}
	}

	public IModule<?> createModuleInstance(ModuleConfig moduleConfig) throws ModuleConfigException
	{
		IModule<?> module = getModuleForName(moduleConfig.getName());
		if(module == null)
		{
			module = (IModule<?>)Component.getInstance(moduleConfig.getName() + "Module", true);
			if(module == null)
			{
				logger.warn("Could not initialize module: " + moduleConfig.getName() + ". No Seam-Component with name \"" + moduleConfig.getName() + "Module\" found.");
			}
			else
			{
				// module.initModule(moduleConfig);
				modules.add(module);
			}
		}
		return module;
	}

	public Object findEntity(String module, Long entityId)
	{
		return entityManager.find(getModuleForName(module).getEntityClass(), entityId);
	}

	public List<IModule<?>> getAdministrableModules()
	{
		return administrableModules;
	}

	public EntityManager getEntityManager()
	{
		return entityManager;
	}

	public ModuleConfig getModuleConfigForName(String name)
	{
		ModuleConfig moduleConfig = configLookup.get(name);
		if(moduleConfig == null)
		{
			logger.warn("Module configuration not found for component: " + name);
		}
		return moduleConfig;
	}

	public List<ModuleContainer> getModuleContainers()
	{
		return moduleContainers;
	}
	
	public List<ModuleContainer> getAdministrableModuleContainers() {
		List<ModuleContainer> list = new ArrayList<ModuleContainer>();
		for (ModuleContainer m : moduleContainers) {
			if (m.getParent().isAdministrable()) {
				list.add(m);
				continue;
			}
			for (IModule<?> mod : m.getChildren()) if (mod.isAdministrable()) {
				list.add(m);
				continue;
			}
		}
		return list;
	}
	
	public List<ModuleContainer> getModuleContainersWithChildren() {
		List<ModuleContainer> list = new ArrayList<ModuleContainer>();
		for (ModuleContainer m : moduleContainers) {
			if (!m.getChildren().isEmpty()) list.add(m);
		}
		return list;
	}
	
	public int getActiveModuleContainerPositionByParent(String parent) {
		if (!StringUtils.isEmpty(parent)) {
			List<ModuleContainer> list = getAdministrableModuleContainers();
			for (int i=0; i<list.size();i++) {
				if (list.get(i).getParent().getConfig().getName().equalsIgnoreCase(parent)) return i;
			}
		}
		return 0;
	}

	public IModule<?> getModuleForName(String name)
	{
		for(IModule<?> module : modules)
		{
			if(module.getConfig().getName().equals(name))
			{
				return module;
			}
		}
		return null;
	}

	public List<IModule<?>> getModules()
	{
		return modules;
	}

	public List<IModule<?>> getNavigableModules()
	{
		return navigableModules;
	}

	public List<DependencyProperty> getUnsatisfiedDependencyProperties(IModule<?> module)
	{
		List<DependencyProperty> unsatisfiedDependencyProperties = new ArrayList<DependencyProperty>(1);
		List<String> dependencies = module.getConfig().getDependencies();
		if(CollectionUtil.isNotEmpty(dependencies))
		{
			for(String dependency : dependencies)
			{
				List<DependencyProperty> dependencyProperties = module.getConfig().getDependencyProperties(dependency);
				if(CollectionUtil.isNotEmpty(dependencyProperties))
				{
					for(DependencyProperty dependencyProperty : dependencyProperties)
					{

						Object value = Expressions.instance().createValueExpression(dependencyProperty.getValue()).getValue();
						if(value == null)
						{
							unsatisfiedDependencyProperties.add(dependencyProperty);
						}
					}
				}
			}
		}
		return unsatisfiedDependencyProperties;
	}

	public void handleEvent(String event, IModule<?> module)
	{
		logger.info("Handling event: " + event);
		Set<IModule<?>> moduleList = new HashSet<IModule<?>>();
		moduleList.add(module);
		// check for dependent modules
		for(IModule<?> currentModule : modules)
		{
			if(CollectionUtil.isNotEmpty(currentModule.getConfig().getDependencies()) && currentModule.getConfig().getDependencies().contains(module.getName()))
			{
				if(!checkDependencyRestrictions(currentModule, module.getName()))
				{
					continue;
				}
				moduleList.add(currentModule);
				// get actions for event
				List<DependencyAction> dependencyActions = currentModule.getConfig().getDependencyActionsForEvent(module.getName(), event);
				if(CollectionUtil.isNotEmpty(dependencyActions))
				{
					// actions found
					for(DependencyAction dependencyAction : dependencyActions)
					{
						logger.info("Creating expression and invoking action: '" + ELUtil.getText(dependencyAction.getExecuteValue()) + "'");
						// create and invoke a method expression
						Expressions.instance().createMethodExpression(dependencyAction.getExecuteValue()).invoke();
					}
				}
			}
		}
		if (!ColibriEvents.SELECT.equals(event)) {
			for(IModule<?> currentModule : moduleList)
			{
				dirtyStateManager.setDirty(currentModule, event);
			}
		}
		// raise a seam event, to let subclasses handle the event via @Observer
		Events.instance().raiseEvent(event, module.getName(), module.getId());
		updateDependentModules(module);
	}

	public void initModule(IModule<?> module)
	{
		try
		{
			module.initModule(getModuleConfigForName(module.getName()));
		}
		catch(ModuleConfigException e)
		{
			logger.error("Error initializing module", e);
		}
	}

	public void updateDependencyProperties(IModule<?> module)
	{
		List<String> dependencies = module.getConfig().getDependencies();
		if(CollectionUtil.isNotEmpty(dependencies))
		{
			for(String dependency : dependencies)
			{
				logger.info("Updating dependency properties: " + dependency);
				List<DependencyProperty> dependencyProperties = module.getConfig().getDependencyProperties(dependency);
				if(CollectionUtil.isNotEmpty(dependencyProperties))
				{
					if(!checkDependencyRestrictions(module, dependency))
					{
						continue;
					}
					for(DependencyProperty dependencyProperty : dependencyProperties)
					{
						Object value = Expressions.instance().createValueExpression(dependencyProperty.getSource()).getValue();
						Expressions.instance().createValueExpression(dependencyProperty.getValue()).setValue(value);
						logger.info("Setting '" + ELUtil.getText(dependencyProperty.getSource()) + "' (" + value + ") to '" + ELUtil.getText(dependencyProperty.getValue()) + "'");
					}
				}
			}
		}
	}

	public void updateDependentModules(IModule<?> module)
	{
		for(IModule<?> currentModule : modules)
		{
			if(CollectionUtil.isNotEmpty(currentModule.getConfig().getDependencies()) && currentModule.getConfig().getDependencies().contains(module.getName()))
			{
				currentModule.setDirty(true);
			}
		}
	}

	private void addModuleToContainerList(IModule<?> module)
	{
		if(module.getConfig().getParent() == null)
		{
			ModuleContainer moduleContainer = new ModuleContainer(module);
			moduleContainers.add(moduleContainer);
		}
		else
		{
			IModule<?> parent = getModuleForName(module.getConfig().getParent().getName());
			boolean found = false;
			for(ModuleContainer moduleContainer : moduleContainers)
			{
				if(moduleContainer.getParent().equals(parent))
				{
					moduleContainer.addChild(module);
					found = true;
					break;
				}
			}
			// CSCHW: Allow children of non navigable and non administrable modules to be navigable
			if(!found && parent != null)
			{
				ModuleContainer moduleContainer = new ModuleContainer(parent);
				moduleContainers.add(moduleContainer);
				moduleContainer.addChild(module);
			}
		}
	}

	private boolean checkDependencyProperties(IModule<?> module, String dependency)
	{
		List<DependencyProperty> dependencyProperties = module.getConfig().getDependencyProperties(dependency);
		if(CollectionUtil.isNotEmpty(dependencyProperties))
		{
			for(DependencyProperty dependencyProperty : dependencyProperties)
			{
				Object value = Expressions.instance().createValueExpression(dependencyProperty.getValue()).getValue();
				if(value == null)
				{
					logger.info("Dependency property is not set: '" + ELUtil.getText(dependencyProperty.getValue()) + "'");
					return false;
				}
			}
		}
		return true;
	}

	private boolean checkDependencyRestrictions(IModule<?> module, String dependency)
	{
		List<DependencyRestriction> dependencyRestrictions = module.getConfig().getDependencyRestrictions(dependency);
		if(CollectionUtil.isNotEmpty(dependencyRestrictions))
		{
			for(DependencyRestriction dependencyRestriction : dependencyRestrictions)
			{
				logger.info("Checking dependency restriction: '" + ELUtil.getText(dependencyRestriction.getValue()) + "'");
				boolean outcome = (Boolean)Expressions.instance().createValueExpression(dependencyRestriction.getValue()).getValue();
				if(outcome == false)
				{
					logger.info("Dependency not met: '" + ELUtil.getText(dependencyRestriction.getValue()) + "'");
					return false;
				}
			}
		}
		return true;
	}
	
	@Factory(value = ContextProperties.GONDORROLE, scope = ScopeType.SESSION)
	public String getGondorRole()
	{
		try
		{
			Object role = Contexts.getSessionContext().get(ContextProperties.GONDORROLE);
			return (role != null) ? role.toString() : ""; 
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public void clear()
	{
		modules.clear();
		navigableModules.clear();
		administrableModules.clear();
		moduleContainers.clear();
		configLookup.clear();
	}
}
