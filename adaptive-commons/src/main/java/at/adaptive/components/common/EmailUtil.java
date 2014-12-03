package at.adaptive.components.common;

import org.hibernate.validator.constraints.impl.EmailValidator;

public class EmailUtil
{
	private static EmailValidator emailValidator = new EmailValidator();
	static
	{
		emailValidator.initialize(null);
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
}
