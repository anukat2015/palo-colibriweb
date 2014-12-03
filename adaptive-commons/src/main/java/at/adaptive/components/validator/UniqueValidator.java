package at.adaptive.components.validator;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.persistence.Query;

import org.jboss.seam.faces.FacesMessages;

/**
 * A unique validator.
 * <p>
 * This class is handy if a unqiue-validation-check must be performed on an input value.
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class UniqueValidator extends AbstractValidator
{
	protected String notUniqueMessageId;

	public UniqueValidator(String query, String notValidMessageId, String notUniqueMessageId)
	{
		super(query, notValidMessageId);
		this.notUniqueMessageId = notUniqueMessageId;
	}

	public void validate(String currentValue, FacesContext context, UIComponent component, Object obj) throws ValidatorException
	{
		if(obj instanceof String)
		{
			String value = (String)obj;
			if(currentValue != null && value.equalsIgnoreCase(currentValue))
			{
				return;
			}
			Query q = getEntityManager().createQuery(query);
			q.setParameter("value", value);
			List<?> list = q.getResultList();
			if(list.isEmpty())
			{
				return;
			}
			throw new ValidatorException(createErrorMessage(notUniqueMessageId));
		}
	}

	protected FacesMessage createErrorMessage(String messageId)
	{
		return FacesMessages.createFacesMessage(FacesMessage.SEVERITY_ERROR, messageId, messageId, new Object[0]);
	}
}
