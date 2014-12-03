package at.adaptive.components.validator;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.persistence.Query;

import at.adaptive.components.common.EmailUtil;
import at.adaptive.components.common.MessageHandler;

/**
 * A unique email validator.
 * <p>
 * This class can be used to perform both a unique-validation-check as well as an email-validation-check.
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class UniqueEmailValidator extends UniqueValidator
{
	public UniqueEmailValidator(String query, String notValidMessageId, String notUniqueMessageId)
	{
		super(query, notValidMessageId, notUniqueMessageId);
	}

	public void validate(String currentValue, FacesContext context, UIComponent component, Object obj) throws ValidatorException
	{
		if(obj instanceof String)
		{
			String value = (String)obj;
			if(currentValue != null && value.equals(currentValue))
			{
				return;
			}
			if(!EmailUtil.isEmailValid(value))
			{
				throw new ValidatorException(MessageHandler.createErrorMessage(notValidMessageId));
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
}
