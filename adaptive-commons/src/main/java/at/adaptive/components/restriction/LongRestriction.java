package at.adaptive.components.restriction;

/**
 * Implementation of a long restriction
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class LongRestriction extends AbstractNumericRestriction<Long>
{
	@Override
	protected Long getNumericValue(String value)
	{
		try
		{
			return Long.parseLong(value);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public String getAsString(Long value)
	{
		if(value != null)
		{
			value.toString();
		}
		return null;
	}

	public Class<Long> getType()
	{
		return Long.class;
	}
}
