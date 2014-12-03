package com.jedox.etl.core.util;

import com.jedox.etl.core.component.ComponentDescriptor;

public interface IETLClassLoader
{

	public Object newInstance(ComponentDescriptor descriptor) throws Exception;

}
