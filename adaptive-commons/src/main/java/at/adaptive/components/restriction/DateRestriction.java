package at.adaptive.components.restriction;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implementation of a date restriction
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class DateRestriction extends AbstractRestriction<Date>
{
	private static final SimpleDateFormat SDF = new SimpleDateFormat("dd.MM.yyyy");

	public Date getAsObject(String value, RestrictionType restrictionType)
	{
		try
		{
			return SDF.parse(value);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public String getAsString(Date value)
	{
		try
		{
			return SDF.format(value);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public Class<Date> getType()
	{
		return Date.class;
	}
}
