package com.proclos.colibriweb.session.system;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.lang.StringUtils;

public class GondorResponseWrapper extends HttpServletResponseWrapper {

	private String role;
	private Set<String> availableRoles = new HashSet<String>();
	
	private String rewriteWithRole(String incomingUrl) {
		if (!StringUtils.isEmpty(role) ) {
			incomingUrl = incomingUrl.replace("//", "/");
		    StringBuffer result = new StringBuffer();
		    if (!incomingUrl.isEmpty()) {
		    	if (incomingUrl.startsWith("/")) incomingUrl = incomingUrl.substring(1);
			    String[] pathElements = incomingUrl.split("/");
			    for (int i=0; i<pathElements.length;i++) {
			    	if (i == 0) {
			    		result.append("/");
			    		result.append(pathElements[i]);
			    		if (pathElements.length > 1 && !availableRoles.contains(pathElements[1])) {
				    		result.append("/");
				    		result.append(role);
			    		}
			    	}
			    	if (i > 0) { //ignore servlet and role path element
			    		result.append("/");
			    		result.append(pathElements[i]);
			    	}
			    }
			    if (incomingUrl.endsWith("/")) {
			    	result.append("/");
			    }
		    }
		    return result.toString();
		}
		return incomingUrl;
	}
	
	public GondorResponseWrapper(HttpServletResponse response) {
		super(response);
	}
	
	public void setRole(String role) {
		this.role = role;
		availableRoles.add(role);
	}
	
	public void setAvailableRoles(String[] roles) {
		if (roles != null) for (String r : roles) {
			availableRoles.add(r);
		}
	}
	
	public String encodeUrl(String url) {
		return rewriteWithRole(super.encodeUrl(url));
	}
	
	public String encodeURL(String url) {
		return rewriteWithRole(super.encodeURL(url));
	}
	
	public String encodeRedirectURL(String url) {
		return rewriteWithRole(super.encodeRedirectURL(url));
	}
	
	public String encodeRedirectUrl(String url) {
		return rewriteWithRole(super.encodeRedirectUrl(url));
	}
	
	 public void sendRedirect(String location) throws IOException {
		 super.sendRedirect(location);
		 System.err.println("Redirect to: "+location);
	 }
	

}
