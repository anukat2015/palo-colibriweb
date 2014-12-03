package at.adaptive.components.common;

import java.util.Collection;
import java.util.List;

import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.impl.EmailValidator;

/**
 * Utility class for validating various objects
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class Validator
{
	private static EmailValidator emailValidator = new EmailValidator();
	static
	{
		emailValidator.initialize(null);
	}

	public static boolean isBooleanTrue(Boolean value)
	{
		return value != null && value.booleanValue();
	}

	/**
	 * Returns whether an email value is valid
	 * 
	 * @param email
	 *            the email
	 * @return <code>true</code> if the specified email value is valid; <code>false</code> otherwise
	 */
	public static boolean isEmailValid(String email)
	{
		return emailValidator.isValid(email,null);
	}

	/**
	 * Returns whether the specified list is empty (i.e. null or an empty list)
	 * 
	 * @param list
	 *            the list
	 * @return <code>true</code> if the specified list is empty; <code>false</code> otherwise
	 */
	public static boolean isEmpty(List<?> list)
	{
		return list == null || list.size() == 0;
	}

	/**
	 * Returns whether the specified object is empty
	 * 
	 * @param value
	 *            the value
	 * @return <code>true</code> if the specified object is empty; <code>false</code> otherwise
	 */
	public static boolean isEmpty(Object value)
	{
		if(value == null)
		{
			return true;
		}
		if(value instanceof String)
		{
			return isEmpty((String)value);
		}
		else if(value instanceof Number)
		{
			return ((Number)value).doubleValue() == 0;
		}
		else if(value instanceof Collection<?>)
		{
			return ((Collection<?>)value).isEmpty();
		}
		return false;
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
}