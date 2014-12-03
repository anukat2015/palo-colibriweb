package com.proclos.colibriweb.session.modules.dashboard;

import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import at.adaptive.components.hibernate.CriteriaWrapper;

import com.proclos.colibriweb.entity.dashbord.Notification;
import com.proclos.colibriweb.session.modules.Module;

@Name("dashboardModule")
@Scope(ScopeType.SESSION)
public class DashbordModule extends Module<Notification>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1417990750190756431L;

	@Override
	public void delete(Object id)
	{
		Notification object = getEntityManager().find(getEntityClass(), id);
		object.setRead(true);
		getEntityManager().persist(object);
		getEntityManager().flush();
		setDirty(true);
	}

	/*
	 * wird benutzt, um das Gelesen rückgängig im Dashboard zu realisieren
	 */
	public void undelete(Object id)
	{
		Notification object = getEntityManager().find(getEntityClass(), id);
		object.setRead(false);
		getEntityManager().persist(object);
		getEntityManager().flush();
		setDirty(true);
	}

	@Override
	public Class<Notification> getEntityClass()
	{
		return Notification.class;
	}

	@Override
	protected void applyCustomFilters(CriteriaWrapper criteriaWrapper)
	{
		try
		{
			super.applyCustomFilters(criteriaWrapper);

			Disjunction disjunction = Restrictions.disjunction();

			// Eingeloggter User ist zugewiesener User!
			disjunction.add(Restrictions.eq("targetUser", this.getCurrentUser()));
			criteriaWrapper.addCriterion(disjunction);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
