package at.adaptive.components.bean.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import at.adaptive.components.restriction.Restriction;
import at.adaptive.components.restriction.RestrictionGroup;
import at.adaptive.components.session.IResultSearchController;

/**
 * This annotation is used to declare a field as a search field which provides functionality to search, filter and order by this field
 * 
 * @author Bernhard Hablesreiter
 * @see Restriction
 * @see RestrictionGroup
 * @see IResultSearchController
 */
@Documented
@Target({METHOD, FIELD})
@Retention(RUNTIME)
@Inherited
public @interface SearchField
{
	/**
	 * The property name to access this search field. Default is the methods handling property name
	 */
	String propertyName() default "";

	/**
	 * Flag indicating the possibility to order by this field. Default is "true"
	 */
	boolean orderEnabled() default true;

	/**
	 * The restriction implementation class. If not set, a default restriction will be used according to the property type
	 */
	Class<?> impl() default void.class;

	/**
	 * The list of the groups this search field should be contained in. This parameter is optional. If set, this search field will be assigned to the specified search field group which allows the
	 * searching over multiple fields with a single input value (e.g. a html input field)
	 */
	String[] groups() default "";
}
