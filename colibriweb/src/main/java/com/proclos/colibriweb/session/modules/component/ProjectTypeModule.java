package com.proclos.colibriweb.session.modules.component;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.proclos.colibriweb.entity.component.ProjectType;
import com.proclos.colibriweb.session.modules.MasterDataModule;

@Name("projectTypeModule")
@Scope(ScopeType.SESSION)
public class ProjectTypeModule extends MasterDataModule<ProjectType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7795029124873131938L;

	@Override
	protected String getContextParamName() {
		return "projectTypes";
	}

	@Override
	public Class<ProjectType> getEntityClass() {
		return ProjectType.class;
	}

	@Factory(value = "projectTypes", scope = ScopeType.APPLICATION)
	public List<ProjectType> getClazzes()
	{
		return getAllResults();
	}
	
}
