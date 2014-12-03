package com.proclos.colibriweb.session.modules.component;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import at.adaptive.components.hibernate.CriteriaWrapper;

import com.proclos.colibriweb.entity.component.Project;
import com.proclos.colibriweb.session.modules.SearchModule;
import com.proclos.colibriweb.session.system.AccessUtil;

@Name("projectSearchModule")
@Scope(ScopeType.SESSION)
public class ProjectSearchModule extends SearchModule<Project> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5008553158180535875L;

	@Override
	public Class<Project> getEntityClass() {
		return Project.class;
	}
	
	protected void applyCustomFilters(CriteriaWrapper criteriaWrapper) {
		AccessUtil.appendAccessCriterion(getCurrentUser(),criteriaWrapper);
		criteriaWrapper.addCriterion(Restrictions.eq("active", true));
		criteriaWrapper.addOrder(Order.asc("name"));
		super.applyCustomFilters(criteriaWrapper);
	}

}
