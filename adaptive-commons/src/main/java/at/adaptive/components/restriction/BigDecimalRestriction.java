package at.adaptive.components.restriction;

import java.math.BigDecimal;

/**
 * Implementation of a big decimal restriction
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class BigDecimalRestriction extends AbstractNumericRestriction<BigDecimal>
{
	public String getAsString(BigDecimal value)
	{
		if(value != null)
		{
			value.toString();
		}
		return null;
	}

	@Override
	protected BigDecimal getNumericValue(String value)
	{
		try
		{
			return new BigDecimal(Double.parseDouble(value));
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public Class<BigDecimal> getType()
	{
		return BigDecimal.class;
	}
}
