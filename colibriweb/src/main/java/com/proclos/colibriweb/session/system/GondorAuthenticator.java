package com.proclos.colibriweb.session.system;

import java.io.Serializable;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;

import com.proclos.colibriweb.common.ContextProperties;
import com.proclos.colibriweb.entity.user.ColibriRole;
import com.proclos.colibriweb.entity.user.ColibriUser;

@Name("gondorAuthenticator")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class GondorAuthenticator implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1911898101632170026L;
	private static final String gondorPassword = String.valueOf(serialVersionUID);

	@Logger
	Log log;
	
	private static String fakePassword = gondorPassword;
	
	public boolean tryAuthenticate() {
		//we cannot inject directly, since we are a startup component!
		EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		Credentials credentials = (Credentials) Component.getInstance("org.jboss.seam.security.credentials");
		Identity identity = (Identity) Component.getInstance("org.jboss.seam.security.identity");
		Map<String, String> headerMap = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap();
		String role = headerMap.get("X-Portal-Role");
		String user = headerMap.get("X-Portal-User");
		String mail = headerMap.get("x-authenticate-mail");

		//TODO comment following 2 lines!!
		role = "administration";
		user = "admin";

		String gondorRole = role;
		log.debug("X-Portal-Role:" + role);
		log.debug("X-Portal-User:" + user);
		if (gondorRole != null) {
			Contexts.getSessionContext().set(ContextProperties.GONDORROLE, "/" + gondorRole);
		}
		if ("administration".equalsIgnoreCase(role)) {
			role = "admin";
		}

		if ("admin".equalsIgnoreCase(role) || "user".equalsIgnoreCase(role)) {
			ColibriUser cpUser = null;
			boolean needsTransaction = !entityManager.getTransaction().isActive();
			if (needsTransaction) entityManager.getTransaction().begin();
			try {
				cpUser = (ColibriUser) entityManager.createQuery("from ColibriUser user where username = :name").setParameter("name", user).getSingleResult();
				if (!StringUtils.isEmpty(mail)) {
					cpUser.setEmail(mail);
					entityManager.persist(cpUser);
					entityManager.flush();
				}
			} catch (NoResultException e) {
				cpUser = new ColibriUser();
				cpUser.setUsername(user);
				cpUser.setLastname(user);
				cpUser.setEnabled(true);
				cpUser.setEmail(mail);
				// cpUser.setPassword(at.adaptive.components.common.SecurityUtil.generateHashedPassword(fakePassword,
				// cpUser.getUsername()));
				entityManager.persist(cpUser);
				entityManager.flush();
			}
			if (!cpUser.hasRole(role)) {
				ColibriRole cpRole = null;
				try {
					cpRole = (ColibriRole) entityManager.createQuery("from ColibriRole role where description = :name").setParameter("name", gondorRole).getSingleResult();
					cpUser.getRoles().add(cpRole);
					entityManager.persist(cpUser);
					entityManager.flush();
				} catch (NoResultException e) {
					log.error("Role " + role + " does not exist.");
				}
			}
			if (needsTransaction) entityManager.getTransaction().commit();
			credentials.setUsername(cpUser.getUsername());
			credentials.setPassword(fakePassword);

			identity.login();
			user = cpUser.getCompleteName();
		}
		if (!identity.isLoggedIn()) { // try conventional log in
			// MessageHandler.addInfoMessage(Expressions.instance().createValueExpression("#{messages['org.jboss.seam.NotLoggedIn']}").getValue().toString());
			return false;
		} else {
			// clear all status message (including not logged in warning raised
			// by FacesSecurityEvents)
			StatusMessages.instance().clear();
			// readd welcome message
			StatusMessages.instance().addFromResourceBundle(Severity.INFO, "org.jboss.seam.loginSuccessful2", user);
			return true;
		}
	}
	
	public String getFakePassword() {
		return fakePassword;
	}

}
