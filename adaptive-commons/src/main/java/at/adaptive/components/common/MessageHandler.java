package at.adaptive.components.common;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;

import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessages;

/**
 * Helper class for handling faces messages
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class MessageHandler
{
	/**
	 * Adds a new error message to the faces messages
	 * 
	 * @param messageId
	 *            the message id to use
	 */
	public static void addErrorMessage(String messageId)
	{
		addMessage(org.jboss.seam.international.StatusMessage.Severity.ERROR, messageId);
	}

	/**
	 * Adds a new error message to the faces messages
	 * 
	 * @param clientId
	 *            the client id to use
	 * @param messageId
	 *            the message id to use
	 */
	public static void addClientErrorMessage(String clientId, String messageId)
	{
		addMessage(clientId, FacesMessage.SEVERITY_ERROR, messageId);
	}

	/**
	 * Adds a new error message to the faces messages
	 * 
	 * @param messageId
	 *            the message id to use
	 * @param params
	 *            the message parameters to use
	 */
	public static void addErrorMessage(String messageId, Object... params)
	{
		addMessage(org.jboss.seam.international.StatusMessage.Severity.ERROR, messageId, params);
	}

	/**
	 * Adds a new fatal message to the faces messages
	 * 
	 * @param messageId
	 *            the message id to use
	 */
	public static void addFatalMessage(String messageId)
	{
		addMessage(org.jboss.seam.international.StatusMessage.Severity.FATAL, messageId);
	}

	/**
	 * Adds a new fatal message to the faces messages
	 * 
	 * @param clientId
	 *            the client id to use
	 * @param messageId
	 *            the message id to use
	 */
	public static void addClientFatalMessage(String clientId, String messageId)
	{
		addMessage(clientId, FacesMessage.SEVERITY_FATAL, messageId);
	}

	/**
	 * Adds a new fatal message to the faces messages
	 * 
	 * @param messageId
	 *            the message id to use
	 * @param params
	 *            the message parameters to use
	 */
	public static void addFatalMessage(String messageId, Object... params)
	{
		addMessage(org.jboss.seam.international.StatusMessage.Severity.FATAL, messageId, params);
	}

	/**
	 * Adds a new info message to the faces messages
	 * 
	 * @param messageId
	 *            the message id to use
	 */
	public static void addInfoMessage(String messageId)
	{
		addMessage(org.jboss.seam.international.StatusMessage.Severity.INFO, messageId);
	}

	/**
	 * Adds a new info message to the faces messages
	 * 
	 * @param clientId
	 *            the client id to use
	 * @param messageId
	 *            the message id to use
	 */
	public static void addClientInfoMessage(String clientId, String messageId)
	{
		addMessage(clientId, FacesMessage.SEVERITY_INFO, messageId);
	}

	/**
	 * Adds a new info message to the faces messages
	 * 
	 * @param messageId
	 *            the message id to use
	 * @param params
	 *            the message parameters to use
	 */
	public static void addInfoMessage(String messageId, Object... params)
	{
		addMessage(org.jboss.seam.international.StatusMessage.Severity.INFO, messageId, params);
	}

	/**
	 * Adds a new warn message to the faces messages
	 * 
	 * @param messageId
	 *            the message id to use
	 */
	public static void addWarnMessage(String messageId)
	{
		addMessage(org.jboss.seam.international.StatusMessage.Severity.WARN, messageId);
	}

	/**
	 * Adds a new warn message to the faces messages
	 * 
	 * @param clientId
	 *            the client id to use
	 * @param messageId
	 *            the message id to use
	 */
	public static void addClientWarnMessage(String clientId, String messageId)
	{
		addMessage(clientId, FacesMessage.SEVERITY_WARN, messageId);
	}

	/**
	 * Adds a new warn message to the faces messages
	 * 
	 * @param messageId
	 *            the message id to use
	 * @param params
	 *            the message parameters to use
	 */
	public static void addWarnMessage(String messageId, Object... params)
	{
		addMessage(org.jboss.seam.international.StatusMessage.Severity.WARN, messageId, params);
	}

	/**
	 * Creats a new error faces message
	 * 
	 * @param messageId
	 *            the message id to use
	 * @return the created faces message
	 */
	public static FacesMessage createErrorMessage(String messageId)
	{
		return createMessage(FacesMessage.SEVERITY_ERROR, messageId);
	}

	/**
	 * Creats a new fatal faces message
	 * 
	 * @param messageId
	 *            the message id to use
	 * @return the created faces message
	 */
	public static FacesMessage createFatalMessage(String messageId)
	{
		return createMessage(FacesMessage.SEVERITY_FATAL, messageId);
	}

	/**
	 * Creats a new info faces message
	 * 
	 * @param messageId
	 *            the message id to use
	 * @return the created faces message
	 */
	public static FacesMessage createInfoMessage(String messageId)
	{
		return createMessage(FacesMessage.SEVERITY_INFO, messageId);
	}

	/**
	 * Creats a new warn faces message
	 * 
	 * @param messageId
	 *            the message id to use
	 * @return the created faces message
	 */
	public static FacesMessage createWarnMessage(String messageId)
	{
		return createMessage(FacesMessage.SEVERITY_WARN, messageId);
	}

	/**
	 * Adds a message to the faces messages
	 * 
	 * @param clientId
	 *            the client id to use
	 * @param severity
	 *            the severity
	 * @param messageId
	 *            the message id to use
	 */
	private static void addMessage(String clientId, Severity severity, String messageId)
	{
		FacesContext.getCurrentInstance().addMessage(clientId, createMessage(severity, messageId));
	}

	/**
	 * Creates a new faces message
	 * 
	 * @param severity
	 *            the severity
	 * @param messageId
	 *            the message id to use
	 * @return the created faces message
	 */
	private static FacesMessage createMessage(Severity severity, String messageId)
	{
		return new FacesMessage(severity, Messages.instance().get(messageId), null);
	}

	/**
	 * Adds a new message to the faces messages
	 * 
	 * @param severity
	 *            the severity
	 * @param messageId
	 *            the message id to use
	 */
	private static void addMessage(org.jboss.seam.international.StatusMessage.Severity severity, String messageId)
	{
		addMessage(severity, messageId, new Object[0]);
	}

	/**
	 * Adds a new message to the faces messages
	 * 
	 * @param severity
	 *            the severity
	 * @param messageId
	 *            the message id to use
	 * @param params
	 *            the message parameters to use
	 */
	private static void addMessage(org.jboss.seam.international.StatusMessage.Severity severity, String messageId, Object[] params)
	{
		StatusMessages.instance().addFromResourceBundleOrDefault(severity, messageId, messageId, params);
	}
}