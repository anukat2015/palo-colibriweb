package com.proclos.colibriweb.session.modules.user;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.proclos.colibriweb.entity.user.ColibriRole;
import com.proclos.colibriweb.session.modules.MasterDataModule;

@Name("userRoleModule")
@Scope(ScopeType.SESSION)
public class UserRoleModule extends MasterDataModule<ColibriRole>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8401912648238151362L;

	@Override
	public Class<ColibriRole> getEntityClass()
	{
		return ColibriRole.class;
	}

	@Factory(value = "userroles", scope = ScopeType.APPLICATION)
	public List<ColibriRole> getUserRoles()
	{
		return getAllResults();
	}

	@Override
	protected String getContextParamName()
	{
		return "userroles";
	}
}
