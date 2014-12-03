package at.adaptive.components.common;

/**
 * Common security constants
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public interface SecurityConstants
{
	String ROLE_SUPERADMIN = "superadmin";
	String ROLE_ADMIN = "admin";
	String ROLE_USER = "user";
	String HAS_ROLE_SUPERADMIN = "#{s:hasRole('superadmin')}";
	String HAS_ROLE_ADMIN = "#{s:hasRole('admin')}";
	String HAS_ROLE_USER = "#{s:hasRole('user')}";
	String IDENTITY_LOGGED_IN = "#{identity.loggedIn}";
	String IDENTITY_NOT_LOGGED_IN = "#{!identity.loggedIn}";
	String HAS_ROLE_ADMINS = "#{s:hasRole('superadmin') or s:hasRole('admin')}";
}
