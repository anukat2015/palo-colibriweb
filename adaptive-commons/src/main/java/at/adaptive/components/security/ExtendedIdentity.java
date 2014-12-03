package at.adaptive.components.security;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.security.Identity;

import at.adaptive.components.common.StringUtil;

@Name("org.jboss.seam.security.identity")
@Scope(SESSION)
@Install(precedence = FRAMEWORK)
@BypassInterceptors
@Startup
public class ExtendedIdentity extends Identity
{
	private static final long serialVersionUID = -6878585094733050366L;

	protected FilterStore filterStore;

	private static final Logger logger = Logger.getLogger(ExtendedIdentity.class);

	@Create
	public void create()
	{
		super.create();
		if(Contexts.isApplicationContextActive())
		{
			filterStore = (FilterStore)Component.getInstance(FilterStore.class, true);
		}
	}

	public boolean hasFilter(String filterName)
	{
		if(!isLoggedIn())
		{
			return false;
		}
		List<DatabaseFilter> filters = filterStore.listFilters(filterName);
		for(DatabaseFilter filter : filters)
		{
			if(StringUtil.isEmpty(filter.getEnabled()) || isFilterEnabled(filter.getEnabled()))
			{
				if(filter.isRoleFilter())
				{
					List<String> roles = parseRoles(filter.getRecipient());
					for(String role : roles)
					{
						if(Identity.instance().hasRole(role))
						{
							return true;
						}
					}

				}
				else
				{
					if(Identity.instance().getCredentials().getUsername().equals(filter.getRecipient()))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	protected List<String> parseRoles(String recipient)
	{
		List<String> roles = new ArrayList<String>();
		String[] splits = recipient.split(",");
		for(String split : splits)
		{
			roles.add(split.trim());
		}
		return roles;
	}

	protected boolean isFilterEnabled(String expression)
	{
		try
		{
			ValueExpression<?> valueExpression = Expressions.instance().createValueExpression(expression);
			Boolean value = (Boolean)valueExpression.getValue();
			return value.booleanValue();
		}
		catch(Exception e)
		{
			logger.error("Error evaluating filter expression: " + expression);
			return false;
		}
	}
}
