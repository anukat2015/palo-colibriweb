package at.adaptive.components.session;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Events;

import at.adaptive.components.common.MessageHandler;
import at.adaptive.components.config.ConfigManager;
import at.adaptive.components.config.BaseComponentConfig;

/**
 * Base component for handling various common tasks.
 * <p>
 * The base component may be configured through the adaptive configuration-mechanism. <br>
 * The configuration manager picks up configuration files located at {jboss-server-dir}/conf/adaptive/{web-app-name} and maps them to the relevant component.<br>
 * By calling {@link #getConfig()}, the specified configuration can be accessed.
 * 
 * @author Bernhard Hablesreiter
 * 
 * @param <T>
 *            The type of the handled {@link BaseComponentConfig}
 */
public abstract class BaseComponent<T extends BaseComponentConfig> implements Serializable
{
	private static final long serialVersionUID = -443091749145359216L;

	private T config;

	@In
	private EntityManager entityManager;

	/**
	 * Returns the handled configuration
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T getConfig()
	{
		if(config == null)
		{
			config = (T)ConfigManager.instance().getConfig(getComponentName());
		}
		return config;
	}

	/**
	 * Adds a new error message to the faces messages
	 * 
	 * @param messageId
	 *            the message id to use
	 */
	protected void addErrorMessage(String messageId)
	{
		addTextErrorMessage(createMessageKey(messageId));
	}

	/**
	 * Adds a new fatal message to the faces messages
	 * 
	 * @param messageId
	 *            the message id to use
	 */
	protected void addFatalMessage(String messageId)
	{
		addTextFatalMessage(createMessageKey(messageId));
	}

	/**
	 * Adds a new info message to the faces messages
	 * 
	 * @param messageId
	 *            the message id to use
	 */
	protected void addInfoMessage(String messageId)
	{
		addTextInfoMessage(createMessageKey(messageId));
	}

	/**
	 * Adds a new error message to the faces messages
	 * 
	 * @param message
	 *            the message
	 */
	protected void addTextErrorMessage(String message)
	{
		MessageHandler.addErrorMessage(message);
	}

	/**
	 * Adds a new fatal message to the faces messages
	 * 
	 * @param message
	 *            the message
	 */
	protected void addTextFatalMessage(String message)
	{
		MessageHandler.addFatalMessage(message);
	}

	/**
	 * Adds a new info message to the faces messages
	 * 
	 * @param message
	 *            the message
	 */
	protected void addTextInfoMessage(String message)
	{
		MessageHandler.addInfoMessage(message);
	}

	/**
	 * Adds a new warn message to the faces messages
	 * 
	 * @param message
	 *            the message
	 */
	protected void addTextWarnMessage(String message)
	{
		MessageHandler.addWarnMessage(message);
	}

	/**
	 * Adds a new warn message to the faces messages
	 * 
	 * @param messageId
	 *            the message id to use
	 */
	protected void addWarnMessage(String messageId)
	{
		addTextWarnMessage(createMessageKey(messageId));
	}

	/**
	 * Creates a new criteria for the specified class
	 * 
	 * @param clazz
	 *            the class
	 * @return the created criteria
	 */
	protected Criteria createCriteria(Class<?> clazz)
	{
		return getSession().createCriteria(clazz);
	}

	/**
	 * Creates a new error faces message
	 * 
	 * @param messageId
	 *            the message id to use
	 * @return the created faces message
	 */
	protected FacesMessage createErrorMessage(String messageId)
	{
		return MessageHandler.createErrorMessage(createMessageKey(messageId));
	}

	/**
	 * Creates a new fatal faces message
	 * 
	 * @param messageId
	 *            the message id to use
	 * @return the created faces message
	 */
	protected FacesMessage createFatalMessage(String messageId)
	{
		return MessageHandler.createFatalMessage(createMessageKey(messageId));
	}

	/**
	 * Creates a new info faces message
	 * 
	 * @param messageId
	 *            the message id to use
	 * @return the created faces message
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
		return getMessageKeyPrefix() + "." + messageId;
	}

	/**
	 * Creates a new warn faces message
	 * 
	 * @param messageId
	 *            the message id to use
	 * @return the created faces message
	 */
	protected FacesMessage createWarnMessage(String messageId)
	{
		return MessageHandler.createWarnMessage(createMessageKey(messageId));
	}

	/**
	 * Returns the name of this component
	 * 
	 * @return the name of this component
	 */
	protected String getComponentName()
	{
		return getClass().getAnnotation(Name.class).value();
	}

	/**
	 * Returns the entity manager
	 * 
	 * @return the entity manager
	 */
	protected EntityManager getEntityManager()
	{
		return entityManager;
	}

	/**
	 * Returns the message key prefix to use
	 * 
	 * @return the message key prefix to use
	 */
	protected abstract String getMessageKeyPrefix();

	protected Session getSession()
	{
		return (Session)entityManager.getDelegate();
	}

	/**
	 * Raises a new event for the specified event type
	 * 
	 * @param eventType
	 *            the event type
	 */
	protected void raiseEvent(String eventType)
	{
		raiseEvent(eventType, new Object[0]);
	}

	/**
	 * Raises a new event for the specified event type and parameters
	 * 
	 * @param eventType
	 *            the event type
	 * @param parameters
	 *            the parameters
	 */
	protected void raiseEvent(String eventType, Object... parameters)
	{
		Events.instance().raiseEvent(eventType, parameters);
	}
}
