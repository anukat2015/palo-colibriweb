package com.proclos.colibriweb.session.modules;

import javax.persistence.EntityManager;

import at.adaptive.components.session.IBaseEntityHome;

import com.proclos.colibriweb.session.system.config.ModuleConfig;
import com.proclos.colibriweb.session.system.config.ModuleConfigException;

public interface IModule<T> extends IBaseEntityHome<T, T>
{
	public void clearLockedInstance();

	public Class<T> getEntityClass();

	public String getName();

	public boolean isAdministrable();

	public boolean isSelectable();

	void checkDependencies();

	void clearEntityManager();

	void clearInCreation();

	void clearInstance();

	ModuleConfig getConfig();

	EntityManager getEntityManager();

	String getLabel();

	T getLockedInstance();

	void initModule(ModuleConfig moduleConfig) throws ModuleConfigException;

	boolean isItemSelected();

	boolean isNavigable();
}
