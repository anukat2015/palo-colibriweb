package at.adaptive.components.config;

/**
 * Base class for all component specific configurations
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public abstract class BaseComponentConfig
{
	private String componentName;

	public String getComponentName()
	{
		return componentName;
	}

	public void setComponentName(String componentName)
	{
		this.componentName = componentName;
	}
}
