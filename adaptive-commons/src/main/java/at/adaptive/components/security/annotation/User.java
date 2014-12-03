package at.adaptive.components.security.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jboss.seam.annotations.security.RoleCheck;

/**
 * Indicates that the action method requires the user to be a member of the 'user' role to invoke.
 * 
 * @author Bernhard Hablesreiter
 */
@Target({TYPE, METHOD})
@Documented
@Retention(RUNTIME)
@Inherited
@RoleCheck
public @interface User
{}
