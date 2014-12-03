package at.adaptive.components.hibernate;

public class AliasContainer
{
	private String path;
	private String alias;
	private int joinType;

	public AliasContainer(String path, String alias, int joinType)
	{
		super();
		this.path = path;
		this.alias = alias;
		this.joinType = joinType;
	}

	public String getAlias()
	{
		return alias;
	}

	public int getJoinType()
	{
		return joinType;
	}

	public String getPath()
	{
		return path;
	}
}
