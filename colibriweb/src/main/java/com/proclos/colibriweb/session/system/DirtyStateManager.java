package com.proclos.colibriweb.session.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;

import at.adaptive.components.common.CollectionUtil;

import com.proclos.colibriweb.session.common.ColibriEvents;
import com.proclos.colibriweb.session.modules.IModule;

@Name("dirtyStateManager")
@Scope(ScopeType.APPLICATION)
@Synchronized(timeout = Long.MAX_VALUE)
@AutoCreate
public class DirtyStateManager
{
	private Map<String, List<IModule<?>>> moduleMap = new HashMap<String, List<IModule<?>>>();

	private static final Logger logger = Logger.getLogger(DirtyStateManager.class);

	public synchronized void deregisterModule(IModule<?> module)
	{
		logger.debug("Deregistering: " + module);
		List<IModule<?>> modules = moduleMap.get(module.getName());
		if(!CollectionUtil.isEmpty(modules))
		{
			modules.remove(module);
		}
	}

	public synchronized void registerModule(IModule<?> module)
	{
		logger.debug("Registering: " + module);
		List<IModule<?>> modules = moduleMap.get(module.getName());
		if(CollectionUtil.isEmpty(modules))
		{
			modules = new ArrayList<IModule<?>>();
		}
		modules.add(module);
		moduleMap.put(module.getName(), modules);
	}

	public synchronized void setDirty(String moduleName)
	{
		List<IModule<?>> modules = moduleMap.get(moduleName);
		if(!CollectionUtil.isEmpty(modules))
		{
			for(IModule<?> currentModule : modules)
			{
				// // ignore the invoking (session scoped) module
				// if(module == module)
				// {
				// continue;
				// }
				logger.info("Setting dirty: " + currentModule);
				currentModule.setDirty(true);
				// currentModule.clearEntityManager();
			}
		}
	}

	public synchronized void setDirty(IModule<?> module, String event)
	{
		if(isSessionScopedEvent(event))
		{
			logger.info("Setting dirty: " + module);
			module.setDirty(true);
			return;
		}
		setDirty(module.getName());
	}

	private boolean isSessionScopedEvent(String event)
	{
		if(event == null)
		{
			return false;
		}
		if(event.equals(ColibriEvents.DELETE))
		{
			return false;
		}
		if(event.equals(ColibriEvents.PERSIST))
		{
			return false;
		}
		return true;
	}
}
