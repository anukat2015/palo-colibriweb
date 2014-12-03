package com.proclos.colibriweb.session.modules.component;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import at.adaptive.components.hibernate.CriteriaWrapper;

import com.proclos.colibriweb.entity.component.Component;
import com.proclos.colibriweb.session.modules.SearchModule;
import com.proclos.colibriweb.session.system.AccessUtil;

@Name("componentSearchModule")
@Scope(ScopeType.SESSION)
public class ComponentSearchModule extends SearchModule<Component> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8891437052188944605L;

	@Override
	public Class<Component> getEntityClass() {
		return Component.class;
	}
	
	protected void applyCustomFilters(CriteriaWrapper criteriaWrapper) {
		AccessUtil.appendAccessCriterion(getCurrentUser(),criteriaWrapper);
		criteriaWrapper.addCriterion(Restrictions.eq("active", true));
		criteriaWrapper.addOrder(Order.asc("type"));
		criteriaWrapper.addOrder(Order.asc("name"));
		super.applyCustomFilters(criteriaWrapper);
	}

}
