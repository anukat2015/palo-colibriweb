package at.adaptive.components.restriction;

/**
 * Abstract base restriction for all {@link Number} members
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public abstract class AbstractNumericRestriction<T extends Number> extends AbstractRestriction<T>
{
	public Object getAsObject(String value, RestrictionType restrictionType)
	{
		try
		{
			if(restrictionType.equals(RestrictionType.LIKE))
			{
				return value;
			}
			else if(restrictionType.equals(RestrictionType.ILIKE) || restrictionType.equals(RestrictionType.CONTAINS) || (restrictionType.equals(RestrictionType.FULLTEXT) && !isFulltext()))
			{
				return null;
			}
			return getNumericValue(value);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	protected RestrictionType getSupportedRestrictionType(RestrictionType restrictionType)
	{
		if(restrictionType.equals(RestrictionType.ILIKE) || restrictionType.equals(RestrictionType.CONTAINS) || (restrictionType.equals(RestrictionType.FULLTEXT) && !isFulltext()))
		{
			return RestrictionType.EQ;
		}
		return restrictionType;
	}

	/**
	 * Returns the numeric value of a string representation
	 * 
	 * @param value
	 *            the string value
	 * @return the numeric value or <code>null</code> if the value could not be parsed
	 */
	protected abstract T getNumericValue(String value);
}
