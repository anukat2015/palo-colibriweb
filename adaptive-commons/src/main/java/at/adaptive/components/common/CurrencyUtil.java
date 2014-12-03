package at.adaptive.components.common;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for handling currency related tasks
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class CurrencyUtil
{
	/**
	 * Returns a formatted string from a specified big decimal value
	 * 
	 * @param value
	 *            the big decimal value
	 * @return the formatted string from the specified big decimal value
	 */
	public static String toFormattedString(BigDecimal value)
	{
		return toFormattedString(value, NumberFormat.getCurrencyInstance(Locale.GERMANY));
	}

	/**
	 * Returns a formatted string from a specified big decimal value and number format
	 * 
	 * @param value
	 *            the big decimal value
	 * @param numberFormat
	 *            the number format to use
	 * @return the formatted string from the specified big decimal value and number format
	 */
	public static String toFormattedString(BigDecimal value, NumberFormat numberFormat)
	{
		if(value == null)
		{
			return null;
		}
		return numberFormat.format(value.doubleValue());
	}
}
