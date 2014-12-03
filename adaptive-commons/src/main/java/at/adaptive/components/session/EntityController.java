package at.adaptive.components.session;

import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.security.AuthorizationException;

import at.adaptive.components.common.MessageHandler;

public abstract class EntityController<T> extends EntityHome<T> implements IEntityController<T>
{
	private static final long serialVersionUID = -3856396498315783055L;

	protected ValueExpression<?> createdMessage;

	protected ValueExpression<?> deletedMessage;

	@In
	protected Map<String, String> messages;

	protected ValueExpression<?> updatedMessage;

	@In
	private EntityManager entityManager;

	private boolean instanceChanged = false;

	@Override
	public void create()
	{
		super.create();
		initialize();
	}

	public String createNewInstance()
	{
		createNewInstanceSecurityCheck();
		clearInstance();
		clear();
		setNewInstanceProperties();
		instanceCreated();
		return getCreatedOutcome();
	}

	public void delete(Object id)
	{
		T object = getEntityManager().find(getEntityClass(), id);
		setDeleteProperties(object);
		getEntityManager().remove(object);
		getEntityManager().flush();
		// setId(id);
		// remove();
		// clearInstance();
		// clear();
		if(isIdDefined() && id.equals(getId()))
		{
			clearInstance();
			clear();
		}
		instanceDeleted();
	}

	public ValueExpression<?> getCreatedMessage()
	{
		return createdMessage;
	}

	public ValueExpression<?> getDeletedMessage()
	{
		return deletedMessage;
	}

	@Override
	public EntityManager getEntityManager()
	{
		return entityManager;
	}

	public ValueExpression<?> getUpdatedMessage()
	{
		return updatedMessage;
	}

	@Override
	@Transactional
	public String persist()
	{
		setPersistProperties();
		super.persist();
		setInstanceChanged(true);
		handleInstanceChanged();
		setInstanceChanged(false);
		instancePersisted();
		return getPersistedOutcome();
	}

	@Override
	public String remove()
	{
		super.remove();
		setInstanceChanged(true);
		handleInstanceChanged();
		setInstanceChanged(false);
		instanceDeleted();
		return getRemovedOutcome();
	}

	public String select(Object id)
	{
		try
		{
			preSelect();
			preSelectSecurityCheck(id);
			clearInstance();
			clear();
			setId(id);
			postSelectSecurityCheck(getInstance());
			instanceSelected();
			return getSelectedOutcome();
		}
		catch(AuthorizationException e)
		{
			clearInstance();
			clear();
			throw e;
		}
	}

	public void setEntityId(String id)
	{
		Object idtoSet = null;
		if(id != null)
		{
			idtoSet = getIdFromString(id);
		}
		super.setId(idtoSet);
	};

	@Override
	public String update()
	{
		setPersistProperties();
		super.update();
		setInstanceChanged(true);
		handleInstanceChanged();
		setInstanceChanged(false);
		instancePersisted();
		return getUpdatedOutcome();
	}

	/**
	 * Adds a new error message to the faces messages
	 * 
	 * @param messageId
	 *            the messageId to use
	 */
	protected void addErrorMessage(String messageId)
	{
		MessageHandler.addErrorMessage(createMessageKey(messageId));
	}

	/**
	 * Adds a new fatal message to the faces messages
	 * 
	 * @param messageId
	 *            the messageId to use
	 */
	protected void addFatalMessage(String messageId)
	{
		MessageHandler.addFatalMessage(createMessageKey(messageId));
	}

	/**
	 * Adds a new info message to the faces messages
	 * 
	 * @param messageId
	 *            the messageId to use
	 */
	protected void addInfoMessage(String messageId)
	{
		MessageHandler.addInfoMessage(createMessageKey(messageId));
	}

	/**
	 * Adds a new warn message to the faces messages
	 * 
	 * @param messageId
	 *            the messageId to use
	 */
	protected void addWarnMessage(String messageId)
	{
		MessageHandler.addWarnMessage(createMessageKey(messageId));
	}

	/**
	 * Clears this component
	 * <p>
	 * This default implementation does nothing. Sublcasses should implement their own logic here
	 */
	protected void clear()
	{}

	/**
	 * Creats a new error message
	 * 
	 * @param messageId
	 *            the message id
	 * @return the created error message
	 */
	protected FacesMessage createErrorMessage(String messageId)
	{
		return MessageHandler.createErrorMessage(createMessageKey(messageId));
	}

	/**
	 * Creats a new fatal message
	 * 
	 * @param messageId
	 *            the message id
	 * @return the created fatal message
	 */
	protected FacesMessage createFatalMessage(String messageId)
	{
		return MessageHandler.createFatalMessage(createMessageKey(messageId));
	}

	/**
	 * Creats a new info message
	 * 
	 * @param messageId
	 *            the message id
	 * @return the created info message
	 */
	protected FacesMessage createInfoMessage(String messageId)
	{
		return MessageHandler.createInfoMessage(createMessageKey(messageId));
	}

	/**
	 * Creates a message key
	 * 
	 * @param messageId
	 *            the message id to use
	 * @return the created message key
	 */
	protected String createMessageKey(String messageId)
	{
		if(getMessageKeyPrefixValue() != null)
		{
			return getMessageKeyPrefixValue() + "." + messageId;
		}
		return messageId;
	}

	/**
	 * Performs a security check for the creation of a new instance
	 * 
	 * @throws AuthorizationException
	 *             if the logged-in user is not authorized to create a new instance
	 */
	protected void createNewInstanceSecurityCheck() throws AuthorizationException
	{}

	/**
	 * Creats a new warn message
	 * 
	 * @param messageId
	 *            the message id
	 * @return the created warn message
	 */
	protected FacesMessage createWarnMessage(String messageId)
	{
		return MessageHandler.createWarnMessage(createMessageKey(messageId));
	}

	/**
	 * Returns the default created message value
	 * 
	 * @return the default created message value
	 */
	protected String getCreatedMessageValue()
	{
		return "successfullyCreated";
	}

	/**
	 * Returns a string value after a "created event". This value is intended to be passed to the navigation handler.<br>
	 * Subclasses may override
	 * 
	 * @return the string value of the "created event"
	 */
	protected String getCreatedOutcome()
	{
		return "created";
	}

	/**
	 * Returns the default deleted message value
	 * 
	 * @return the default deleted message value
	 */
	protected String getDeletedMessageValue()
	{
		return "successfullyDeleted";
	}

	/**
	 * Returns the corresponding object for the specfied id string value
	 * 
	 * @param id
	 *            the id
	 * @return the corresponding object for the specfied id string value
	 */
	protected abstract Object getIdFromString(String id);

	/**
	 * Returns the message key prefix value for this entity home class. This default implementation returns <code>null</code>. Subclasses may override
	 * 
	 * @return the message key prefix value for this entity home class
	 */
	protected String getMessageKeyPrefixValue()
	{
		return null;
	}

	/**
	 * Returns a string value after a "persisted event". This value is intended to be passed to the navigation handler.<br>
	 * Subclasses may override
	 * 
	 * @return the string value of the "persisted event"
	 */
	protected String getPersistedOutcome()
	{
		return "persisted";
	}

	/**
	 * Returns a string value after a "removed event". This value is intended to be passed to the navigation handler.<br>
	 * Subclasses may override
	 * 
	 * @return the string value of the "removed event"
	 */
	protected String getRemovedOutcome()
	{
		return "removed";
	}

	/**
	 * Returns a string value after a "selected event". This value is intended to be passed to the navigation handler.<br>
	 * Subclasses may override
	 * 
	 * @return the string value of the "selected event"
	 */
	protected String getSelectedOutcome()
	{
		return "selected";
	}

	/**
	 * Returns the default updated message value
	 * 
	 * @return the default updated message value
	 */
	protected String getUpdatedMessageValue()
	{
		return "successfullyUpdated";
	}

	/**
	 * Returns a string value after a "updated event". This value is intended to be passed to the navigation handler.<br>
	 * Subclasses may override
	 * 
	 * @return the string value of the "updated event"
	 */
	protected String getUpdatedOutcome()
	{
		return "updated";
	}

	/**
	 * Handles the change of the current instance. This method is called on persist, update and remove
	 */
	protected void handleInstanceChanged()
	{}

	protected void initDefaultMessages()
	{
		Expressions expressions = new Expressions();
		if(createdMessage == null)
		{
			createdMessage = expressions.createValueExpression(messages.get(getCreatedMessageValue()));
		}
		if(updatedMessage == null)
		{
			updatedMessage = expressions.createValueExpression(messages.get(getUpdatedMessageValue()));
		}
		if(deletedMessage == null)
		{
			deletedMessage = expressions.createValueExpression(messages.get(getDeletedMessageValue()));
		}
	}

	/**
	 * Initializes this component. This method is called when the component is created
	 */
	protected void initialize()
	{}

	/**
	 * This method is called after an new instance has been created. Subclasses may implement their own logic here.<br>
	 * This default implementation does nothing
	 * 
	 * @see #createNewInstance()
	 */
	protected void instanceCreated()
	{}

	/**
	 * This method is called after the current instance has been deleted. Subclasses may implement their own logic here.<br>
	 * This default implementation does nothing
	 * 
	 * @see #remove()
	 * @see #delete(Object)
	 */
	protected void instanceDeleted()
	{}

	/**
	 * This method is fired after the current instance has been persisted or updated. Subclasses may override
	 */
	protected void instancePersisted()
	{}

	/**
	 * This method is called after an item has been selected. Subclasses may implement their own logic here.<br>
	 * This default implementation does nothing
	 * 
	 * @see #select(Object)
	 */
	protected void instanceSelected()
	{}

	/**
	 * Returns the instance changed value
	 * 
	 * @return the instance changed value
	 */
	protected boolean isInstanceChanged()
	{
		return instanceChanged;
	}

	/**
	 * Performs a security check for a specified entity object
	 * 
	 * @param id
	 *            the entity object to check
	 * @throws AuthorizationException
	 *             if the logged-in user is not authorized to select the specified entity object
	 */
	protected void postSelectSecurityCheck(T object) throws AuthorizationException
	{}

	protected void preSelect()
	{}

	/**
	 * Performs a security check for a specified id
	 * 
	 * @param id
	 *            the id to check
	 * @throws AuthorizationException
	 *             if the logged-in user is not authorized to select the specified entity object
	 */
	protected void preSelectSecurityCheck(Object id) throws AuthorizationException
	{}

	/**
	 * Refreshs the current instance, if it is managed
	 */
	protected void refreshInstance()
	{
		if(isManaged())
		{
			getEntityManager().refresh(getInstance());
		}
	}

	/**
	 * Sets properties to the specified object before it is deleted. Sublcasses may override
	 * 
	 * @param object
	 *            the object
	 */
	protected void setDeleteProperties(T object)
	{}

	/**
	 * Sets the instance changed parameter to the specified value
	 * 
	 * @param instanceChanged
	 *            the instnace changed value
	 */
	protected void setInstanceChanged(boolean instanceChanged)
	{
		this.instanceChanged = instanceChanged;
	}

	/**
	 * Sets properties on a newly created instance. This method is called during the {@link #createNewInstance()} process. Subclasses may implement their own logic here
	 * <p>
	 * This default implementation does nothing
	 */
	protected void setNewInstanceProperties()
	{
		return;
	}

	/**
	 * Sets properties to the current instance before it is persisted or updated. Sublcasses may override
	 */
	protected void setPersistProperties()
	{}
}
