package at.adaptive.components.validator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

/**
 * A parmerterizable unique email validator. Facelets code should look as follows: <code>
 * <h:inputText value="..." validator="validator">
 * 		<f:attribute name="validationValue" value="#{value}" />
 * </h:inputSecret>
 * </code>
 * <p>
 * This class can be used to perform both a unique-validation-check as well as an email-validation-check.
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class ParameterizableUniqueEmailValidator extends UniqueEmailValidator
{
	public ParameterizableUniqueEmailValidator(String query, String notValidMessageId, String notUniqueMessageId)
	{
		super(query, notValidMessageId, notUniqueMessageId);
	}

	public void validate(FacesContext context, UIComponent component, Object obj) throws ValidatorException
	{
		String currentValue = (String)component.getAttributes().get("validationValue");
		super.validate(currentValue, context, component, obj);
	}
}
