package at.adaptive.components.usermanagement.config;

import at.adaptive.components.config.BaseComponentConfig;
import at.adaptive.components.usermanagement.session.LostPasswordManager;

/**
 * The configuration for the lost password manager
 * 
 * @author Bernhard Hablesreiter
 * @see LostPasswordManager
 * 
 */
public class LostPasswordConfig extends BaseComponentConfig
{
	private int expiryTime;
	private String serverUrl;
	private String resetPasswordView;
	private String messageRenderView;
	private String userClass;
	private String lostPasswordClass;

	public int getExpiryTime()
	{
		return expiryTime;
	}

	public void setExpiryTime(int expiryTime)
	{
		this.expiryTime = expiryTime;
	}

	public String getServerUrl()
	{
		return serverUrl;
	}

	public void setServerUrl(String serverUrl)
	{
		this.serverUrl = serverUrl;
	}

	public String getResetPasswordView()
	{
		return resetPasswordView;
	}

	public void setResetPasswordView(String resetPasswordView)
	{
		this.resetPasswordView = resetPasswordView;
	}

	public String getMessageRenderView()
	{
		return messageRenderView;
	}

	public void setMessageRenderView(String messageRenderView)
	{
		this.messageRenderView = messageRenderView;
	}

	public String getUserClass()
	{
		return userClass;
	}

	public void setUserClass(String userClass)
	{
		this.userClass = userClass;
	}

	public String getLostPasswordClass()
	{
		return lostPasswordClass;
	}

	public void setLostPasswordClass(String lostPasswordClass)
	{
		this.lostPasswordClass = lostPasswordClass;
	}
}
