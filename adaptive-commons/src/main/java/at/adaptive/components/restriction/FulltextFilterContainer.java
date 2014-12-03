package at.adaptive.components.restriction;

import java.io.Serializable;
import java.util.Map;

public class FulltextFilterContainer implements Serializable
{
	private static final long serialVersionUID = 1550633018951953698L;

	private String fulltextFilter;
	private Map<String, Object> parameters;

	public FulltextFilterContainer(String fulltextFilter, Map<String, Object> parameters)
	{
		this.fulltextFilter = fulltextFilter;
		this.parameters = parameters;
	}

	public String getFulltextFilter()
	{
		return fulltextFilter;
	}

	public Map<String, Object> getParameters()
	{
		return parameters;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fulltextFilter == null) ? 0 : fulltextFilter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof FulltextFilterContainer)) return false;
		FulltextFilterContainer other = (FulltextFilterContainer)obj;
		if(fulltextFilter == null)
		{
			if(other.fulltextFilter != null) return false;
		}
		else if(!fulltextFilter.equals(other.fulltextFilter)) return false;
		return true;
	}
}
