package com.proclos.opensmc.modules;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.proclos.opensmc.session.system.ModuleManager;

public class ModuleTest extends SeamTest
{

	private EntityManagerFactory emf;

	public EntityManagerFactory getEntityManagerFactory()
	{
		return emf;
	}

	@BeforeSuite
	public void init()
	{
		emf = Persistence.createEntityManagerFactory("opensmc-test");
	}

	@AfterSuite
	public void destroy()
	{
		emf.close();
	}

	@Test
	public void testConfigurations()
	{
		ModuleManager manager = new ModuleManager();

		manager.getModules();
		/*
		 * EntityManager em = getEntityManagerFactory().createEntityManager(); em.getTransaction().begin(); em.close();
		 */
	}

}
