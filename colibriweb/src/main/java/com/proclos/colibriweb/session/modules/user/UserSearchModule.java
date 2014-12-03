package com.proclos.colibriweb.session.modules.user;

import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import at.adaptive.components.hibernate.CriteriaWrapper;

import com.proclos.colibriweb.entity.user.ColibriUser;
import com.proclos.colibriweb.session.modules.SearchModule;

@Name("userSearchModule")
@Scope(ScopeType.SESSION)
public class UserSearchModule extends SearchModule<ColibriUser>
{
	// @In
	// private ClientModule clientModule;

	/**
	 * 
	 */
	private static final long serialVersionUID = 4550440343039967972L;

	@Override
	public Class<ColibriUser> getEntityClass()
	{
		return ColibriUser.class;
	}
	
	@Override
	protected void applyCustomFilters(CriteriaWrapper criteriaWrapper)
	{
		super.applyCustomFilters(criteriaWrapper);
		criteriaWrapper.addCriterion(Restrictions.eq("enabled", true));
		criteriaWrapper.addCriterion(Restrictions.eq("active",true));
	}

	// @Override
	// protected IModule<Client> getParentModule()
	// {
	// return clientModule;
	// }
}
