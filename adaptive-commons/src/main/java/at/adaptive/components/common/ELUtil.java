package at.adaptive.components.common;

public class ELUtil
{
	public static String getText(String el)
	{
		if(el == null)
		{
			return null;
		}
		return el.replaceAll("[\\{\\}\\#]", "");
	}
}
