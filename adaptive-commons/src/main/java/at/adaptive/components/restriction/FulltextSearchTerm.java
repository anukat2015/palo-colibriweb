package at.adaptive.components.restriction;

import java.io.Serializable;

public class FulltextSearchTerm implements Serializable
{
	private String name;
	private String value;
	private boolean applyCurrentFieldName;

	public FulltextSearchTerm(String name, String value)
	{
		super();
		this.name = name;
		this.value = value;
	}

	public String getName()
	{
		return name;
	}

	public String getValue()
	{
		return value;
	}

	public String getLuceneQueryString()
	{
		return new StringBuilder().append(name).append(":").append(value).toString();
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof FulltextSearchTerm)
		{
			return getLuceneQueryString().equals(((FulltextSearchTerm)obj).getLuceneQueryString());
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return getLuceneQueryString().hashCode();
	}

	public boolean isApplyCurrentFieldName()
	{
		return applyCurrentFieldName;
	}

	public void setApplyCurrentFieldName(boolean applyCurrentFieldName)
	{
		this.applyCurrentFieldName = applyCurrentFieldName;
	}
}
