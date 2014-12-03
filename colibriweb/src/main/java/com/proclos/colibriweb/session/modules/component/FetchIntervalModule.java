package com.proclos.colibriweb.session.modules.component;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.proclos.colibriweb.entity.component.FetchInterval;
import com.proclos.colibriweb.session.modules.MasterDataModule;

@Name("fetchIntervalModule")
@Scope(ScopeType.SESSION)
public class FetchIntervalModule extends MasterDataModule<FetchInterval> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2294454147047953434L;

	@Override
	protected String getContextParamName() {
		return "fetchIntervals";
	}

	@Override
	public Class<FetchInterval> getEntityClass() {
		return FetchInterval.class;
	}
	
	@Factory(value = "fetchIntervals", scope = ScopeType.APPLICATION)
	public List<FetchInterval> getClazzes()
	{
		return getAllResults();
	}

}
