package at.adaptive.components.security;

public enum FilterType
{
	ROLE("Role"), USER("User");

	private String name;

	private FilterType(String name)
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
