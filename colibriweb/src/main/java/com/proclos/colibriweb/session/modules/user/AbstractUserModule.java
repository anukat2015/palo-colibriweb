package com.proclos.colibriweb.session.modules.user;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.validation.constraints.Size;


import at.adaptive.components.common.SecurityUtil;
import at.adaptive.components.common.StringUtil;
import at.adaptive.components.validator.ParameterizableUniqueValidator;
import at.adaptive.components.validator.UniqueValidator;

import com.proclos.colibriweb.entity.user.ColibriUser;
import com.proclos.colibriweb.session.modules.Module;

public abstract class AbstractUserModule extends Module<ColibriUser>
{
	private static final long serialVersionUID = 1L;

	private String password;

	private UniqueValidator usernameValidator;

	@Override
	public Class<ColibriUser> getEntityClass()
	{
		return ColibriUser.class;
	}

	@Size(max = 20)
	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void validateUsername(FacesContext context, UIComponent component, Object obj) throws ValidatorException
	{
		String username = (String)obj;
		if(username != null)
		{
			username = username.toLowerCase();
		}
		usernameValidator.validate(getInstance().getUsername(), context, component, username);
	}

	@Override
	protected void initialize()
	{
		super.initialize();
		usernameValidator = new ParameterizableUniqueValidator("from ColibriUser where lower(username) like :value", org.jboss.seam.international.Messages.instance().get("user.invalid"), org.jboss.seam.international.Messages.instance().get("user.exists"));
	}

	@Override
	protected void instanceSelected()
	{
		super.instanceSelected();
		password = null;
	}

	@Override
	protected void setNewInstanceProperties()
	{
		super.setNewInstanceProperties();
		getInstance().setEnabled(true);
	}

	@Override
	protected void setPersistProperties()
	{
		super.setPersistProperties();
		if(!StringUtil.isEmpty(password))
		{
			getInstance().setPassword(SecurityUtil.generateHashedPassword(password, getInstance().getUsername()));
		}
	}
}
