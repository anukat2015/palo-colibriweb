package at.adaptive.components.common;

public class StringUtil
{
	/**
	 * Returns the number of matching chars in a string value
	 * 
	 * @param value
	 *            the value
	 * @param c
	 *            the char to check
	 * @return the number of matching chars in the specified string value
	 */
	public static int countChars(String value, char c)
	{
		int count = 0;
		if(isEmpty(value))
		{
			return count;
		}
		for(int i = 0; i < value.length(); i++)
		{
			if(value.charAt(i) == c)
			{
				count++;
			}
		}
		return count;
	}

	public static boolean isEmpty(Number number)
	{
		return number == null || number.doubleValue() == 0;
	}

	/**
	 * Returns whether the specified string is empty
	 * 
	 * @param value
	 *            the value
	 * @return <code>true</code> if the specified string is empty; <code>false</code> otherwise
	 */
	public static boolean isEmpty(String value)
	{
		if(value == null)
		{
			return true;
		}
		return value.trim().length() == 0;
	}

	/**
	 * Returns whether the specified string is not empty
	 * 
	 * @param value
	 *            the value
	 * @return <code>true</code> if the specified string is not empty; <code>false</code> otherwise
	 */
	public static boolean isNotEmpty(String value)
	{
		return !isEmpty(value);
	}
}
