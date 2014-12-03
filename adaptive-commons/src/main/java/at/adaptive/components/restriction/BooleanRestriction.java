package at.adaptive.components.restriction;

/**
 * Implementation of a boolean restriction
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class BooleanRestriction extends AbstractRestriction<Boolean>
{

	public Boolean getAsObject(String value, RestrictionType restrictionType)
	{
		try
		{
			return Boolean.parseBoolean(value);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public String getAsString(Boolean value)
	{
		if(value != null)
		{
			value.toString();
		}
		return null;
	}

	public Class<Boolean> getType()
	{
		return Boolean.class;
	}
}
