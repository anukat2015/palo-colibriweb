package com.proclos.colibriweb.session.system;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;



/**
 * This class simply wraps the default resource handler. 
 * It is registered in faces-config.xml 
 * This causes this resource handler not to pass by the gondor filter as the default handler did before.
 * Strange. But obviously working...
 * @author chris
 *
 */
public class GondorResourceHandler extends ResourceHandlerWrapper {
	
	private final ResourceHandler wrapped;

	  public GondorResourceHandler(final ResourceHandler wrapped)
	  {
	    this.wrapped = wrapped;
	  }

	@Override
	public ResourceHandler getWrapped() {
		return wrapped;
	}
	
	/*
	public Resource createResource(String resourceName,
            String libraryName,
            String contentType) {
		Resource r = super.createResource(resourceName, libraryName, contentType);
		System.out.println(r.getRequestPath());
		return r;
	}
	
	public Resource createResource(String resourceName,
            String libraryName) {
		Resource r = super.createResource(resourceName, libraryName);
		System.out.println(r.getRequestPath());
		return r;
	}
	
	public Resource createResource(String resourceName) {
		Resource r = super.createResource(resourceName);
		System.out.println(r.getRequestPath());
		return r;
	}
	
	public boolean libraryExists(final String libraryName) {
		boolean result = super.libraryExists(libraryName);
		System.out.println("Library reuested "+libraryName+" "+result);
		return result;
	}
	*/

}
