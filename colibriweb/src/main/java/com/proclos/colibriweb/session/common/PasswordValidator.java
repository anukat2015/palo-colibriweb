package com.proclos.colibriweb.session.common;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.faces.Validator;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import at.adaptive.components.common.MessageHandler;

@Name("passwordValidator")
@Validator
@BypassInterceptors
public class PasswordValidator implements javax.faces.validator.Validator
{
	@Override
	public void validate(FacesContext context, UIComponent component, Object obj) throws ValidatorException
	{
		String value = (String)obj;
		if (!StringUtils.isEmpty(value)) {
		if(!PasswordPolicy.DEFAULT.isValid(value))
			{
				throw new ValidatorException(MessageHandler.createErrorMessage(PasswordPolicy.DEFAULT.getErrorMessage()));
			}
		}	
	}
}
