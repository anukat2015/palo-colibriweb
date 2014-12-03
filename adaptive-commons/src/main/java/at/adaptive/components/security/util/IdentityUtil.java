package at.adaptive.components.security.util;

import org.jboss.seam.security.Identity;

import at.adaptive.components.common.CollectionUtil;

public class IdentityUtil
{
	public static boolean hasRole(String... roles)
	{
		if(CollectionUtil.isEmpty(roles))
		{
			return false;
		}
		for(String role : roles)
		{
			if(Identity.instance().hasRole(role))
			{
				return true;
			}
		}
		return false;
	}
}
