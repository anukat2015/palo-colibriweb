package at.adaptive.components.restriction;

/**
 * Implementation of a double restriction
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class DoubleRestriction extends AbstractNumericRestriction<Double>
{
	@Override
	protected Double getNumericValue(String value)
	{
		try
		{
			return Double.parseDouble(value);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public String getAsString(Double value)
	{
		if(value != null)
		{
			value.toString();
		}
		return null;
	}

	public Class<Double> getType()
	{
		return Double.class;
	}
}
