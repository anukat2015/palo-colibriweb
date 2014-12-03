package at.adaptive.components.datahandling.dataexport.function;

import java.io.Serializable;

public interface Function extends Serializable
{
	public Object execute(Object object);
}
