package at.adaptive.components.restriction;

/**
 * Implementation of a float restriction
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class FloatRestriction extends AbstractNumericRestriction<Float>
{
	@Override
	protected Float getNumericValue(String value)
	{
		try
		{
			return Float.parseFloat(value);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public String getAsString(Float value)
	{
		if(value != null)
		{
			value.toString();
		}
		return null;
	}

	public Class<Float> getType()
	{
		return Float.class;
	}
}
