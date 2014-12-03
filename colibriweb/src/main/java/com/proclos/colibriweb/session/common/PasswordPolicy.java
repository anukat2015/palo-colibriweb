package com.proclos.colibriweb.session.common;

import org.jboss.seam.international.Messages;

import at.adaptive.components.common.StringUtil;

public class PasswordPolicy
{
	public static final PasswordPolicy DEFAULT = new PasswordPolicy(8, 1, 1, 1, Messages.instance().get("at.adaptive.components.usermanagement.LostPasswordManager.passwordNotValid"));

	private int minLength;
	private int minSpecialChars;
	private int minUpperCaseChars;
	private int minNumericChars;
	private String errorMessage;

	public PasswordPolicy(int minLength, int minSpecialChars, int minUpperCaseChars, int minNumericChars, String errorMessage)
	{
		super();
		this.minLength = minLength;
		this.minSpecialChars = minSpecialChars;
		this.minUpperCaseChars = minUpperCaseChars;
		this.minNumericChars = minNumericChars;
		this.errorMessage = errorMessage;
	}

	public static PasswordPolicy getDefault()
	{
		return DEFAULT;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public int getMinLength()
	{
		return minLength;
	}

	public int getMinNumericChars()
	{
		return minNumericChars;
	}

	public int getMinSpecialChars()
	{
		return minSpecialChars;
	}

	public int getMinUpperCaseChars()
	{
		return minUpperCaseChars;
	}

	public boolean isValid(String password)
	{
		if(StringUtil.isEmpty(password))
		{
			return false;
		}
		int length = password.length();
		if(length < minLength)
		{
			return false;
		}
		int specialChars = 0;
		int upperCaseChars = 0;
		int numericChars = 0;
		for(int i = 0; i < length; i++)
		{
			int c = password.charAt(i);
			if(!Character.isLetterOrDigit(c) && !Character.isWhitespace(c))
			{
				specialChars++;
			}
			else if(Character.isUpperCase(c))
			{
				upperCaseChars++;
			}
			else if(Character.isDigit(c))
			{
				numericChars++;
			}
		}
		if(specialChars < minSpecialChars)
		{
			return false;
		}
		if(upperCaseChars < minUpperCaseChars)
		{
			return false;
		}
		if(numericChars < minNumericChars)
		{
			return false;
		}
		return true;
	}
}
