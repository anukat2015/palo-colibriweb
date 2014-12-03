package com.proclos.colibriweb.session.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.faces.Validator;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;

/**
 * Utility class for url validation
 * 
 * @author Bernhard Hablesreiter
 * 
 */
@Validator
@Name("urlValidator")
@BypassInterceptors
public class UrlValidator implements javax.faces.validator.Validator
{
	/**
	 * The regex. Default is "^(https?://)?(([0-9a-zA-Z_!~*'().&=+$%-]+:
	 * )?[0-9a-zA-Z_!~*'().&=+$%-]+@)?(([0-9]{1,3}\.){3}[0-9]{1,3}|([0-9a-zA-Z_!~*'()-]+\.)*([0-9a-zA-Z][0-9a-zA-Z-]{0,61})?[0-9a-zA-Z]
	 * \.[a-zA-Z]{2,6})(:[0-9]{1,4})?((/?)|(/[0-9a-zA-Z_!~*'().;?:@&=+$,%#-]+)+/?)$"
	 */
	private static final String URL_REGEX = "^(https?://)" + "?(([0-9a-zA-Z_!~*'().&=+$%-]+: )?[0-9a-zA-Z_!~*'().&=+$%-]+@)?" // user@
			+ "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP- 199.194.52.184
			+ "|" // allows either IP or domain
			+ "([0-9a-zA-Z_!~*'()-]+\\.)*" // host names - www.
			+ "(([0-9a-zA-Z][0-9a-zA-Z-]{0,61})?[0-9a-zA-Z]\\.)*" // optional second or higher level domain
			+ "[a-zA-Z][a-zA-Z]+)" // top level domain- .com or .museum
			+ "(:[0-9]{1,4})?" // port number- :80
			+ "((/?)|" // a slash isn't required if there is no file name
			+ "(/[0-9a-zA-Z_!~*'().;?:@&=+$,%#-]+)+/?)$";

	public void validate(FacesContext context, UIComponent cmp, Object value) throws ValidatorException
	{
		if(!isValid((String)value))
		{
			throw new ValidatorException(createErrorMessage("validator.url"));
		}
	}

	private boolean isValid(String value)
	{
		if(StringUtils.isEmpty(value))
		{
			return true;
		}
		try
		{
			Pattern pattern = Pattern.compile(URL_REGEX);
			Matcher matcher = pattern.matcher(value);
			return matcher.matches();
		}
		catch(Exception e)
		{
			return false;
		}
	}

	private FacesMessage createErrorMessage(String messageId)
	{
		return FacesMessages.createFacesMessage(FacesMessage.SEVERITY_ERROR, messageId, messageId, new Object[0]);
	}
}
