package at.adaptive.components.restriction;

/**
 * Implementation of a short restriction
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class ShortRestriction extends AbstractNumericRestriction<Short>
{
	@Override
	protected Short getNumericValue(String value)
	{
		try
		{
			return Short.parseShort(value);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public String getAsString(Short value)
	{
		if(value != null)
		{
			value.toString();
		}
		return null;
	}

	public Class<Short> getType()
	{
		return Short.class;
	}
}
