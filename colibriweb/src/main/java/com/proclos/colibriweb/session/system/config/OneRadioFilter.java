package com.proclos.colibriweb.session.system.config;

import java.util.List;

public class OneRadioFilter<T> extends OneMenuFilter<T>
{

	public OneRadioFilter(String name, List<FilterDefinition> definitions)
	{
		super(name, definitions);
		setJsfType(JSFTypes.selectoneradio);
	}

}
