package at.adaptive.components.hibernate;

import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;

public class HibernateUtil
{
	public static Criteria createCriteria(EntityManager entityManager, Class<?> entityClass)
	{
		return getSession(entityManager).createCriteria(entityClass);
	}

	public static Session getSession(EntityManager entityManager)
	{
		return (Session)entityManager.getDelegate();
	}
}
