package com.proclos.colibriweb.session.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.annotations.Name;

import at.adaptive.components.common.MessageHandler;

@Name("timeValidator")
public class TimeValidator implements Converter
{
	private static final Pattern PATTERN = Pattern.compile("([\\d]{1,2}):([\\d]{1,2})");
	private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm");

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String time)
	{
		if(time == null)
		{
			return null;
		}
		try
		{
			return SDF.parse(time);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object obj)
	{
		if(obj == null || !(obj instanceof Date))
		{
			return null;
		}
		return SDF.format((Date)obj);
	}

	public void validate(FacesContext context, UIComponent component, Object obj) throws ValidatorException
	{
		if (obj instanceof String) {
			String value = obj.toString();
			Matcher matcher = PATTERN.matcher(value);
			if(matcher.matches())
			{
				int hours = Integer.parseInt(matcher.group(1));
				int minutes = Integer.parseInt(matcher.group(2));
				if(hours < 24 && minutes < 60)
				{
					return;
				}
			}
			throw new ValidatorException(MessageHandler.createErrorMessage("validator.time"));
		}
	}
}
