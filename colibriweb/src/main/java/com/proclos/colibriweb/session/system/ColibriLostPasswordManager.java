package com.proclos.colibriweb.session.system;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import at.adaptive.components.usermanagement.session.LostPasswordManager;

import com.proclos.colibriweb.session.common.PasswordValidator;

@Name(LostPasswordManager.COMPONENT_NAME)
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.APPLICATION)
public class ColibriLostPasswordManager extends LostPasswordManager
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3813482531258959964L;
	@In(create = true)
	private PasswordValidator passwordValidator;

	@Override
	public void validatePassword(FacesContext context, UIComponent component, Object value) throws ValidatorException
	{
		passwordValidator.validate(context, component, value);
		super.validatePassword(context, component, value);
	}
}
