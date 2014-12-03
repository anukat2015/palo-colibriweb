package at.adaptive.components.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;

/**
 * Validator for an url
 * 
 * @author Bernhard Hablesreiter
 * 
 */
@Documented
@Constraint(validatedBy = UrlValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Url
{
	/**
	 * The regex. Default is "^(https?://)?(([0-9a-zA-ZäöüÄÖÜß_!~*'().&=+$%-]+:
	 * )?[0-9a-zA-ZäöüÄÖÜß_!~*'().&=+$%-]+@)?(([0-9]{1,3}\.){3}[0-9]{1,3}|([0-9a-zA-ZäöüÄÖÜß_!~*'()-]+\.)*([0-9a-zA-ZäöüÄÖÜß][0-9a-zA-ZäöüÄÖÜß-]{0,61})?[0-9a-zA-ZäöüÄÖÜß]
	 * \.[a-zA-ZäöüÄÖÜß]{2,6})(:[0-9]{1,4})?((/?)|(/[0-9a-zA-ZäöüÄÖÜß_!~*'().;?:@&=+$,%#-]+)+/?)$"
	 */
	String regex() default "^(https?://)" + "?(([0-9a-zA-ZäöüÄÖÜß_!~*'().&=+$%-]+: )?[0-9a-zA-ZäöüÄÖÜß_!~*'().&=+$%-]+@)?" // user@
			+ "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP- 199.194.52.184
			+ "|" // allows either IP or domain
			+ "([0-9a-zA-ZäöüÄÖÜß_!~*'()-]+\\.)*" // tertiary domain(s)- www.
			+ "([0-9a-zA-ZäöüÄÖÜß][0-9a-zA-ZäöüÄÖÜß-]{0,61})?[0-9a-zA-ZäöüÄÖÜß]\\." // second level domain
			+ "[a-zA-ZäöüÄÖÜß]{2,6})" // first level domain- .com or .museum
			+ "(:[0-9]{1,4})?" // port number- :80
			+ "((/?)|" // a slash isn't required if there is no file name
			+ "(/[0-9a-zA-ZäöüÄÖÜß_!~*'().;?:@&=+$,%#-]+)+/?)$";

	/**
	 * Regex processing flags
	 */
	int flags() default 0;

	/**
	 * The message
	 */
	String message() default "{validator.url}";
}
