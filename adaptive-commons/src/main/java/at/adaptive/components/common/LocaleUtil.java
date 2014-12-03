package at.adaptive.components.common;

import java.util.Locale;

public class LocaleUtil
{
	public static Locale LOCALE_AT = findLocale("at");

	public static Locale findLocale(String value)
	{
		if(value != null)
		{
			Locale[] locales = Locale.getAvailableLocales();
			for(Locale locale : locales)
			{
				if(locale.getCountry().equalsIgnoreCase(value))
				{
					return locale;
				}
			}
		}
		return Locale.getDefault();
	}
}
