package at.adaptive.components.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;

/**
 * A zip code validator. Default min is "1000", default max is "99999"
 * 
 * @author Bernhard Hablesreiter
 * 
 */
@Documented
@Constraint(validatedBy = ZipCodeValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface ZipCode
{
	/**
	 * The max. Default is "99999"
	 */
	long max() default 99999;

	/**
	 * The max. Default is "1000"
	 */
	long min() default 1000;

	/**
	 * The message
	 */
	String message() default "{validator.zipcode}";
}