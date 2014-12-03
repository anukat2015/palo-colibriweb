package at.adaptive.components.searchfilter;

public enum SearchFilterType
{
	TEXT("text"),
	CHECKBOX("checkbox"),
	DROPDOWN("dropdown"),
	DATE("date"),
	SINGLECHECKBOX("singlecheckbox");

	private String name;

	private SearchFilterType(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
