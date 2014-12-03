package com.proclos.colibriweb.session.system;

import static org.jboss.seam.annotations.Install.APPLICATION;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.Log;
import org.jboss.seam.navigation.Pages;

@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Name("org.jboss.seam.navigation.pages")
@Install(precedence = APPLICATION, classDependencies = "javax.faces.context.FacesContext")
@Startup
public class GondorPages extends Pages {
	

	@Logger
	Log log;

	private boolean authenticate = true;

	public boolean tryAuthenticate() {
		GondorAuthenticator auth = (GondorAuthenticator) Component.getInstance("gondorAuthenticator");
		return auth.tryAuthenticate();
	}

	public void redirectToLoginView() {
		if (authenticate) {
			if (!tryAuthenticate()) {
				super.redirectToLoginView();
			}
		} else {
			super.redirectToLoginView();
		}
	}
	
	public boolean isAuthenticate() {
		return authenticate;
	}

	public void setAuthenticate(boolean authenticate) {
		this.authenticate = authenticate;
	}
	
	@Create
	public void create() {
		super.create();
	}
	
	
	



}
