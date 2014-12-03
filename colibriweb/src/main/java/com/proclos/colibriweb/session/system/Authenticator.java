package com.proclos.colibriweb.session.system;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.security.management.JpaIdentityStore;

import com.proclos.colibriweb.common.ContextProperties;
import com.proclos.colibriweb.entity.user.ColibriRole;
import com.proclos.colibriweb.entity.user.ColibriUser;

@Name("authenticator")
public class Authenticator
{
	@Logger
	private Log logger;

	@In
	private Credentials credentials;

	@In
	private EntityManager entityManager;

	@In
	private IdentityManager identityManager;

	@In
	private Identity identity;
	
	/*
	@In 
	private GondorAuthPlugin gondorAuthPlugin;
	*/

	@Create
	public void create()
	{
	}

	public boolean authenticate()
	{
		boolean success = false;
		GondorAuthenticator gondorAuthenticator = (GondorAuthenticator)Component.getInstance("gondorAuthenticator");
		GondorPages gondorPages = (GondorPages)Component.getInstance(com.proclos.colibriweb.session.system.GondorPages.class);
		boolean gondorAuth = gondorPages.isAuthenticate() && credentials.getPassword().equals(gondorAuthenticator.getFakePassword());
		ColibriUser user = null;
		if (gondorAuth) { //logged in via auth plugin
			success = true;
		} else { //conventional login
			success = identityManager.authenticate(credentials.getUsername(), credentials.getPassword());
		}
		logger.info("Authenticating: " + credentials.getUsername() + ". Success = " + success);
		if(success)
		{
			try
			{
				user = findUser();
				List<ColibriRole> roles = user.getRoles();
				if (gondorAuth) { //we are using gondor auth: raise messages otherwise raised by jpaidentitymanager
					if (Events.exists())
				    {
						 user = findUser();
				         if (Contexts.isEventContextActive())
				         {
				            Contexts.getEventContext().set(JpaIdentityStore.AUTHENTICATED_USER, user);
				         }
				         
				         Events.instance().raiseEvent(JpaIdentityStore.EVENT_USER_AUTHENTICATED, user);
				    }
					String gondorRole = (String) Contexts.getSessionContext().get(ContextProperties.GONDORROLE);
					if (gondorRole != null && roles != null) for(ColibriRole role : roles) //add only gondorRole
					{
						if (gondorRole.endsWith(role.getGondorRole())) identity.addRole(role.getName());
					}
				} else { //add all available roles
					for(ColibriRole role : roles)
					{
						identity.addRole(role.getName());
					}
				}
				Contexts.getSessionContext().set(ContextProperties.USER, user);
				// init module manager
				Component.getInstance("moduleManager", true);
				FacesMessages.instance().add(Severity.INFO, "org.jboss.seam.loginSuccessful2", null, null, null, user.getCompleteName());
			}
			catch(Exception e)
			{
				logger.error("Could not load user: " + credentials.getUsername(), e);
				return false;
			}
		}
		return success;
	}

	private ColibriUser findUser() throws Exception
	{
		return (ColibriUser)entityManager.createQuery("from ColibriUser where username=:username").setParameter("username", credentials.getUsername()).getSingleResult();
	}
}
