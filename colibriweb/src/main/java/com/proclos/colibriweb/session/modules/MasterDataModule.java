package com.proclos.colibriweb.session.modules;

import org.hibernate.criterion.Order;
import org.jboss.seam.contexts.Contexts;

import at.adaptive.components.hibernate.CriteriaWrapper;

import com.proclos.colibriweb.entity.BaseEntity;
import com.proclos.colibriweb.entity.SelectionItem;

public abstract class MasterDataModule<T extends BaseEntity> extends Module<T>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4563144849330895449L;

	@Override
	protected void applyCustomFilters(CriteriaWrapper criteriaWrapper)
	{
		super.applyCustomFilters(criteriaWrapper);
		if(SelectionItem.class.isAssignableFrom(getEntityClass()))
		{
			criteriaWrapper.clearOrders();
			criteriaWrapper.addOrder(Order.asc("sortOrder"));
			criteriaWrapper.addOrder(Order.asc("label"));
		}
	}

	protected abstract String getContextParamName();

	@Override
	protected void handleEvent(String event)
	{
		Contexts.getApplicationContext().remove(getContextParamName());
		super.handleEvent(event);
	}

	@Override
	protected void setDefaultRestrictions()
	{}

	public void updateForm()
	{
		// CSCHW: Dummy Method for a4j actions to re-render the form
	}
}
