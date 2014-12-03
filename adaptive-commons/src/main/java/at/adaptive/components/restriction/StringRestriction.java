package at.adaptive.components.restriction;

/**
 * Implementation of a string restriction
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class StringRestriction extends AbstractRestriction<String>
{

	@Override
	protected String getModifiedValue(String value, RestrictionType restrictionType)
	{
		if(restrictionType.equals(RestrictionType.LIKE) || restrictionType.equals(RestrictionType.ILIKE))
		{
			return value + "%";
		}
		else if(restrictionType.equals(RestrictionType.CONTAINS) || restrictionType.equals(RestrictionType.FULLTEXT))
		{
			return "%" + value + "%";
		}
		return value;
	}

	public String getAsObject(String value, RestrictionType restrictionType)
	{
		return value;
	}

	public String getAsString(String value)
	{
		return value;
	}

	public Class<String> getType()
	{
		return String.class;
	}
}