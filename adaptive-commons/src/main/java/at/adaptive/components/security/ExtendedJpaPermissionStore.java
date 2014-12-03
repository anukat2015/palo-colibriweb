package at.adaptive.components.security;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.security.permission.JpaPermissionStore;
import org.jboss.seam.security.permission.Permission;

@Name("org.jboss.seam.security.jpaPermissionStore")
@Install(precedence = FRAMEWORK, value = false)
@Scope(APPLICATION)
@BypassInterceptors
public class ExtendedJpaPermissionStore extends JpaPermissionStore
{
	private static final long serialVersionUID = -5605864818168641997L;

	private Map<Object, Map<String, List<Permission>>> cache = new HashMap<Object, Map<String, List<Permission>>>(100);

	@Override
	protected List<Permission> listPermissions(Object target, Set<Object> targets, String action)
	{
		List<Permission> permissions = null;
		Map<String, List<Permission>> actionMap = null;
		if(cache.containsKey(target))
		{
			actionMap = cache.get(target);
			permissions = actionMap.get(action);
		}
		if(permissions == null)
		{
			permissions = super.listPermissions(target, targets, action);
			if(actionMap == null)
			{
				actionMap = new HashMap<String, List<Permission>>();
			}
			actionMap.put(action, permissions);
			cache.put(target, actionMap);
		}
		return permissions;
	}

	public void clearCache()
	{
		cache.clear();
	}
}
