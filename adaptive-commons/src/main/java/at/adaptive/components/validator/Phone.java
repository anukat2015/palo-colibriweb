package at.adaptive.components.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;

/**
 * Validator for a phone number. The default regex is "(\\+?[\\d\\s]{0,4}\\s?(\\(0\\))?[\\d\\s\\/\\-]{5,})?"
 * 
 * @author Bernhard Hablesreiter
 * 
 */
@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Phone
{
	/**
	 * The regex. Default is "(\\+?[\\d\\s]{0,4}\\s?(\\(0\\))?[\\d\\s\\/\\-]{5,})?"
	 */
	String regex() default "(\\+?[\\d\\s]{0,4}\\s?(\\(0\\))?[\\d\\s\\/\\-]{5,})?";

	/**
	 * Regex processing flags
	 */
	int flags() default 0;

	/**
	 * The message
	 */
	String message() default "{validator.phone}";
}
