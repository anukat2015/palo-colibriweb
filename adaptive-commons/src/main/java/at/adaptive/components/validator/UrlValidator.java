package at.adaptive.components.validator;

import java.util.regex.Matcher;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator for an url
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class UrlValidator implements ConstraintValidator<Url,Object>
{
	private java.util.regex.Pattern pattern;

	public void initialize(Url parameters)
	{
		pattern = java.util.regex.Pattern.compile(parameters.regex(), parameters.flags());
	}

	public boolean isValid(Object value)
	{
		if(value == null) return true;
		if(!(value instanceof String)) return false;
		String string = (String)value;
		Matcher m = pattern.matcher(string);
		return m.matches();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		return isValid(value);
	}
}
