package com.proclos.colibriweb.session.modules.component;


import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.AuthorizationException;

import com.jedox.etl.core.component.ITypes;
import com.jedox.etl.core.component.Locator;
import com.proclos.colibriweb.common.ContextProperties;
import com.proclos.colibriweb.common.IAccessControlled;
import com.proclos.colibriweb.entity.component.Component;
import com.proclos.colibriweb.entity.component.ComponentTypes;
import com.proclos.colibriweb.entity.user.ColibriUser;
import com.proclos.colibriweb.session.system.AccessUtil;




/**
 * Renderer for all kinds of feeds
 * 
 * @author chris
 * 
 */
@Name("componentResolver")
@Scope(ScopeType.CONVERSATION)
public class ComponentResolver implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7355305274717163852L;
	private static final String defaultCharSet = "UTF-8";

	@RequestParameter
	private String locator;
	
	
	@In
	EntityManager entityManager;
	
	@Logger
	Log log;

	/**
	 * Creates this component
	 */
	@Create
	public void create()
	{
	}
	
	private ColibriUser getCurrentUser()
	{
		try
		{
			return (ColibriUser)Contexts.getSessionContext().get(ContextProperties.USER);
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	protected int getComponentType(String managerName) {
		switch (ITypes.Managers.valueOf(managerName)) {
		case connections: return  ComponentTypes.CONNECTION;
		case extracts: return ComponentTypes.EXTRACT;
		case transforms: return ComponentTypes.TRANSFORM;
		case loads: return  ComponentTypes.LOAD;
		case jobs: return  ComponentTypes.JOB;
		default: return -1;
		}
	}
	
	private boolean checkAccess(IAccessControlled object) {
		if (!AccessUtil.hasAccess(getCurrentUser(),object)) {
			throw new AuthorizationException(Expressions.instance().createValueExpression("#{messages['authorizationExceptionMessage']}").getValue().toString());
		}
		return true;
	}


	/**
	 * Resolves the component
	 */
	public void resolveComponent()
	{
		try {
			Locator l = Locator.parse(locator);
			int dType = getComponentType(l.getManager());
			Query query = entityManager.createQuery("select component from Component component join component.project project where component.dType=:dType and component.name=:name and project.name=:project");
			query.setParameter("dType", dType);
			query.setParameter("name", l.getName());
			query.setParameter("project",l.getRootName());
			Component component = (Component)query.getSingleResult();
			checkAccess(component.getProject());
			String module = l.getManager().substring(0,l.getManager().length()-1);
			Redirect.instance().setViewId("/modules/"+module+"/edit.xhtml");
			Redirect.instance().setParameter("id", component.getId());
			Redirect.instance().execute();
		} catch (Exception e) {		
			log.error("Component "+locator+"cannot be found: "+e.getMessage());
		}
	}
}
