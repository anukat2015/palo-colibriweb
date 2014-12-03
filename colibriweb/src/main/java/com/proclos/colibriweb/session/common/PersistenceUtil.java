package com.proclos.colibriweb.session.common;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Hibernate;
import org.jboss.seam.core.Expressions;

public class PersistenceUtil
{
	public static EntityManager getEntityManager()
	{
		return Expressions.instance().createValueExpression("#{entityManager}", EntityManager.class).getValue();
	}

	public static void initializeCollection(List<?> list)
	{
		if(list != null)
		{
			for(Object o : list)
			{
				Hibernate.initialize(o);
			}
		}
	}
}
