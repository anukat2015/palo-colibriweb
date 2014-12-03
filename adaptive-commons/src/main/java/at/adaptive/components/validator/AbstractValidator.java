package at.adaptive.components.validator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.persistence.EntityManager;

import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;

/**
 * Abstract base class for validators
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public abstract class AbstractValidator
{
	protected String query;
	protected String notValidMessageId;
	protected ValueExpression<EntityManager> entityManagerValueExpression;

	public AbstractValidator(String query, String notValidMessageId)
	{
		super();
		this.query = query;
		this.notValidMessageId = notValidMessageId;
		this.entityManagerValueExpression = Expressions.instance().createValueExpression("#{entityManager}", EntityManager.class);
	}

	protected EntityManager getEntityManager()
	{
		return entityManagerValueExpression.getValue();
	}

	public abstract void validate(String currentValue, FacesContext context, UIComponent component, Object obj) throws ValidatorException;
}
