package com.proclos.colibriweb.session.system;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;

public class GondorRequestWrapper extends HttpServletRequestWrapper {
	
	private StringBuffer appUrl;
	private StringBuffer servletPath;
	private String role;

	public GondorRequestWrapper(HttpServletRequest request) {
		super(request);
		String incomingUrl=request.getRequestURI(); //TODO we deliver the URI as new URL. it seems to work, but better recheck.
	    appUrl = new StringBuffer();
	    servletPath = new StringBuffer();
	    if (!incomingUrl.isEmpty()) {
	    	if (incomingUrl.startsWith("/")) incomingUrl = incomingUrl.substring(1);
		    String[] pathElements = incomingUrl.split("/");
		    for (int i=0; i<pathElements.length;i++) {
		    	if (i == 1) {
		    		role = pathElements[i];
		    	}
		    	if (i != 1) { //ignore role path element
			    	appUrl.append("/");
			    	appUrl.append(pathElements[i]);
		    	}
		    	if (i > 1) { //ignore servlet and role path element
		    		servletPath.append("/");
		    		servletPath.append(pathElements[i]);
		    	}
		    }
		    if (incomingUrl.endsWith("/")) {
		    	appUrl.append("/index.html");
		    	servletPath.append("/index.html");
		    }
	    }
	}
	
	public StringBuffer getRequestURL() {
		StringBuffer requestUrl = super.getRequestURL();
		try {
			URL url = new URL(requestUrl.toString());
			String path = url.getPath();
			requestUrl.delete(requestUrl.indexOf(path), requestUrl.length());
			requestUrl.append(appUrl);
			return requestUrl;
		}
		catch (MalformedURLException e) {}
		return appUrl;
	}
	
	public String getPathInfo() {
		return super.getPathInfo();
	}
	
	public String getServletPath() {
		return servletPath.toString();
	}
	
	public String getPathTranslated() {
		return super.getPathTranslated();
	}
	
	public String getRole() {
		return role;
	}
	
	public String getContextPath() {
		if (StringUtils.isEmpty(role)) {
			return super.getContextPath();
		}
		else {
			return super.getContextPath()+"/"+role;
		}
	}

}
