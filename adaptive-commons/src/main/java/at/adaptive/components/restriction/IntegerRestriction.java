package at.adaptive.components.restriction;

/**
 * Implementation of an integer restriction
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class IntegerRestriction extends AbstractNumericRestriction<Integer>
{
	@Override
	protected Integer getNumericValue(String value)
	{
		try
		{
			return Integer.parseInt(value);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public String getAsString(Integer value)
	{
		if(value != null)
		{
			value.toString();
		}
		return null;
	}

	public Class<Integer> getType()
	{
		return Integer.class;
	}
}
