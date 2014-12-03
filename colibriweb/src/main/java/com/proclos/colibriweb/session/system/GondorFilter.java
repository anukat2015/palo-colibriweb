package com.proclos.colibriweb.session.system;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


public class GondorFilter implements Filter
{
	
	private boolean enabled = false;
	private String[] avalableRoles;

  public void init(FilterConfig filterConfig) throws ServletException
  {
	  enabled = "TRUE".equalsIgnoreCase(filterConfig.getInitParameter("enabled"));
	  String roleString = filterConfig.getInitParameter("roles");
	  avalableRoles = roleString.split(",");
	  if (enabled) {
		  ServletRegistration sreg = filterConfig.getServletContext().getServletRegistration("Richfaces");
		  if (sreg != null) {
			  Collection<String> mappings = sreg.getMappings();
			  for (String m : mappings) {
				  for (String r : avalableRoles) {
					  sreg.addMapping("/"+r+m);
				  }
			  }
		  }
		  FilterRegistration freg = filterConfig.getServletContext().getFilterRegistration(filterConfig.getFilterName());
		  for (String r : avalableRoles) {
			  freg.addMappingForUrlPatterns(null, false, "/"+r+"/*");
		  }
	  }
  }

  public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain) throws IOException,ServletException
  {
	if (enabled) {
	    HttpServletRequest httpRequest=(HttpServletRequest)request;
	    String uri = httpRequest.getRequestURI();
	    boolean needsRewrite = false;
	    for (String s : avalableRoles) {
	    	if (uri.contains(s)) {
	    		needsRewrite = true;
	    		break;
	    	}
	    }
	    if (needsRewrite) {
	    	GondorRequestWrapper wrappedRequest = new GondorRequestWrapper(httpRequest);
	    	GondorResponseWrapper wrappedResponse = new GondorResponseWrapper((HttpServletResponse)response);
	    	wrappedResponse.setRole(wrappedRequest.getRole());
	    	wrappedResponse.setAvailableRoles(avalableRoles);
	    	chain.doFilter(wrappedRequest,wrappedResponse);
	    } else
	    {
	     System.err.println("Gondor access denied: "+uri);
	    }
	} else {
		chain.doFilter(request,response);
	}
  }

  public void destroy()
  {
  }
}