package com.proclos.colibriweb.session.modules;

import org.hibernate.criterion.Restrictions;

import at.adaptive.components.hibernate.CriteriaWrapper;

import com.proclos.colibriweb.entity.BaseEntity;

public abstract class SimpleListModule<T extends BaseEntity> extends Module<T>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3388992704056774008L;
	protected boolean displayInactive = false;

	public void displayInactiveFilterSet()
	{
		setDirty(true);
	}

	public boolean isDisplayInactive()
	{
		return displayInactive;
	}

	public void setDisplayInactive(boolean displayInactive)
	{
		this.displayInactive = displayInactive;
	}
	

	@Override
	protected void applyCustomFilters(CriteriaWrapper criteriaWrapper)
	{
		super.applyCustomFilters(criteriaWrapper);
		if(!displayInactive)
		{
			criteriaWrapper.addCriterion(Restrictions.eq("active", true));
		}
	}

	@Override
	protected boolean isEvictOldResults()
	{
		return false;
	}
}
