package at.adaptive.components.restriction;

/**
 * Implementation of a byte restriction
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class ByteRestriction extends AbstractNumericRestriction<Byte>
{
	@Override
	protected Byte getNumericValue(String value)
	{
		try
		{
			return Byte.parseByte(value);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public String getAsString(Byte value)
	{
		if(value != null)
		{
			value.toString();
		}
		return null;
	}

	public Class<Byte> getType()
	{
		return Byte.class;
	}
}
