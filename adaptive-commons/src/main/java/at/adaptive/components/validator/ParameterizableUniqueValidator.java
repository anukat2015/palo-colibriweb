package at.adaptive.components.validator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

/**
 * A parmerterizable unique validator. Facelets code should look as follows: <code>
 * <h:inputText value="..." validator="validator">
 * 		<f:attribute name="validationValue" value="#{value}" />
 * </h:inputSecret>
 * </code>
 * <p>
 * This class is handy if a unqiue-validation-check must be performed on an input value.
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class ParameterizableUniqueValidator extends UniqueValidator
{
	public ParameterizableUniqueValidator(String query, String notValidMessageId, String notUniqueMessageId)
	{
		super(query, notValidMessageId, notUniqueMessageId);
	}

	public void validate(FacesContext context, UIComponent component, Object obj) throws ValidatorException
	{
		String currentValue = (String)component.getAttributes().get("validationValue");
		super.validate(currentValue, context, component, obj);
	}
}
