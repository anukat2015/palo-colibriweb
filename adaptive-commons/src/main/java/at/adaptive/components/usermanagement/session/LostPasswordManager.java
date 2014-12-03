package at.adaptive.components.usermanagement.session;

import java.lang.reflect.Type;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.persistence.Id;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.management.UserPassword;
import org.jboss.seam.annotations.security.management.UserPrincipal;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.IdentityManager;
import org.jboss.seam.util.AnnotatedBeanProperty;

import at.adaptive.components.common.EmailUtil;
import at.adaptive.components.common.SecurityUtil;
import at.adaptive.components.session.BaseComponent;
import at.adaptive.components.usermanagement.annotation.LostPasswordTimestamp;
import at.adaptive.components.usermanagement.annotation.LostPasswordToken;
import at.adaptive.components.usermanagement.annotation.LostPasswordUser;
import at.adaptive.components.usermanagement.annotation.UserEmail;
import at.adaptive.components.usermanagement.config.LostPasswordConfig;

/**
 * Manager class for lost password handling. This class is mostly generic and can operate with different user and/or lost password entities.
 * <p>
 * The lost password manager must be properly configured through the {@link BaseComponent}-configuration-mechanism (please see the javadoc for more information on how to configure an adaptive
 * component).
 * <p>
 * Here is a sample configuration:
 * <p>
 * <code> <?xml version="1.0" encoding="UTF-8"?> <java version="1.5.0_15" class="java.beans.XMLDecoder"> <object class="at.adaptive.components.usermanagement.config.LostPasswordConfig"> <void
 * property="componentName"> <string>lostPasswordManager</string> </void> <void property="expiryTime"> <int>1800000</int> </void> <void property="serverUrl">
 * <string>http://localhost:8080/mdonline/</string> </void> <void property="resetPasswordView"> <string>resetpassword</string> </void> <void property="messageRenderView">
 * <string>lostpasswordmessage.xhtml</string> </void> <void property="messageRenderView"> <string>/components/lostpasswordmessage.xhtml</string> </void> <void property="userClass">
 * <string>at.adaptive.mdonline.entity.MDOnlineUser</string> </void> <void property="lostPasswordClass"> <string>at.adaptive.components.usermanagement.entity.LostPassword</string> </void> </object>
 * </java> </code>
 * 
 * @author Bernhard Hablesreiter
 * 
 */
@Name(LostPasswordManager.COMPONENT_NAME)
@Scope(ScopeType.CONVERSATION)
@Install(precedence = Install.FRAMEWORK)
public class LostPasswordManager extends BaseComponent<LostPasswordConfig>
{
	protected static final String MESSAGE_KEY_PREFIX = "at.adaptive.components.usermanagement.LostPasswordManager";

	protected static final long serialVersionUID = 6601687955405422471L;

	public static final String COMPONENT_NAME = "lostPasswordManager";

	protected String checkedUserId;

	protected String confirmPassword;

	@In
	protected IdentityManager identityManager;

	protected String link;

	@Logger
	protected Log logger;

	protected Object lostPassword;

	protected Class<?> lostPasswordClass;

	protected String nameOrEmail;
	protected String password;

	@In(create = true)
	protected Renderer renderer;

	@RequestParameter
	protected String token;
	protected boolean tokenChecked = false;

	protected Object user;

	protected Class<?> userClass;

	@RequestParameter
	protected String userId;

	protected AnnotatedBeanProperty<UserPassword> userPasswordProperty;
	protected AnnotatedBeanProperty<Id> userIdProperty;
	protected AnnotatedBeanProperty<UserPrincipal> userNameProperty;
	protected AnnotatedBeanProperty<UserEmail> userEmailProperty;
	protected AnnotatedBeanProperty<LostPasswordUser> lostPasswordUserProperty;
	protected AnnotatedBeanProperty<LostPasswordToken> lostPasswordTokenProperty;
	protected AnnotatedBeanProperty<LostPasswordTimestamp> lostPasswordTimestampProperty;

	@Create
	public void create()
	{
		try
		{
			userClass = Class.forName(getConfig().getUserClass());
			lostPasswordClass = Class.forName(getConfig().getLostPasswordClass());
		}
		catch(ClassNotFoundException e)
		{
			logger.error("Error initializing lost password manager", e);
		}
		userPasswordProperty = new AnnotatedBeanProperty<UserPassword>(userClass, UserPassword.class);
		userIdProperty = new AnnotatedBeanProperty<Id>(userClass, Id.class);
		userNameProperty = new AnnotatedBeanProperty<UserPrincipal>(userClass, UserPrincipal.class);
		userEmailProperty = new AnnotatedBeanProperty<UserEmail>(userClass, UserEmail.class);
		lostPasswordUserProperty = new AnnotatedBeanProperty<LostPasswordUser>(lostPasswordClass, LostPasswordUser.class);
		lostPasswordTokenProperty = new AnnotatedBeanProperty<LostPasswordToken>(lostPasswordClass, LostPasswordToken.class);
		lostPasswordTimestampProperty = new AnnotatedBeanProperty<LostPasswordTimestamp>(lostPasswordClass, LostPasswordTimestamp.class);
	}

	public String getConfirmPassword()
	{
		return confirmPassword;
	}

	public String getLink()
	{
		return link;
	}

	public String getNameOrEmail()
	{
		return nameOrEmail;
	}

	public String getPassword()
	{
		return password;
	}

	public Object getUser()
	{
		return user;
	}

	@End
	public boolean resetPassword()
	{
		try
		{
			if(!tokenChecked || lostPassword == null)
			{
				return false;
			}
			boolean verified = password != null && confirmPassword != null && password.equals(confirmPassword);
			if(!verified)
			{
				addErrorMessage("passwordsNotEqual");
				return false;
			}
			Object user = findUserById(checkedUserId);
			identityManager.getIdentityStore().changePassword((String)userNameProperty.getValue(user), password);
			getEntityManager().remove(lostPassword);
			getEntityManager().flush();
			addInfoMessage("passwordResetted");
			lostPassword = null;
			return true;
		}
		catch(Exception e)
		{
			addErrorMessage("passwordResetFailed");
			logger.error("Error resetting password", e);
			return false;
		}
	}

	@Override
	protected String getComponentName()
	{
		return COMPONENT_NAME;
	}

	public void setConfirmPassword(String confirmPassword)
	{
		this.confirmPassword = confirmPassword;
	}

	public void setNameOrEmail(String nameOrEmail)
	{
		this.nameOrEmail = nameOrEmail;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	@End
	public boolean startLostPasswordProcess()
	{
		try
		{
			// get user
			user = findUser(nameOrEmail);
			logger.info("Got user: " + user);
			if(user == null)
			{
				addErrorMessage("userNotFound");
				return false;
			}
			// check for request in the last 30 minutes
			long lostPasswordCount = getLostPasswordCount(userIdProperty.getValue(user));
			if(lostPasswordCount > 0)
			{
				addErrorMessage("lostPasswordProcessAlreadyStarted");
				return false;
			}
			// create token
			String token = SecurityUtil.generatePassword(24);
			Object lostPassword = lostPasswordClass.newInstance();
			lostPasswordTokenProperty.setValue(lostPassword, token);
			lostPasswordUserProperty.setValue(lostPassword, user);
			// create email
			link = createLink(token);
			sendMail();
			addInfoMessage("emailSuccessfullySent");
			// save token
			getEntityManager().persist(lostPassword);
			getEntityManager().flush();
			return true;
		}
		catch(Exception e)
		{
			addErrorMessage("lostPasswordProcessFailed");
			logger.error("Error starting lost password process", e);
			return false;
		}
	}

	public void validateNameOrEmail(FacesContext context, UIComponent component, Object value) throws ValidatorException
	{
		if(value instanceof String)
		{
			String stringValue = (String)value;
			if(stringValue != null && !stringValue.equalsIgnoreCase("admin"))
			{
				try
				{
					findUser((String)value);
					return;
				}
				catch(Exception e)
				{}
			}
		}
		throw new ValidatorException(createErrorMessage("userNotFound"));
	}

	public void validatePassword(FacesContext context, UIComponent component, Object value) throws ValidatorException
	{
		if(value instanceof String)
		{
			String stringValue = (String)value;
			if(stringValue.length() >= 5)
			{
				return;
			}
		}
		throw new ValidatorException(createErrorMessage("passwordNotValid"));
	}

	@Begin(join = true)
	public void validateToken()
	{
		if(!tokenChecked)
		{
			if(userId != null && token != null && isUserId(userId))
			{
				try
				{
					lostPassword = findLostPassword(userId);
					if(isEpired(lostPassword))
					{
						addErrorMessage("tokenExpired");
						return;
					}
					else
					{
						tokenChecked = true;
						checkedUserId = userId;
						return;
					}
				}
				catch(Exception e)
				{}
			}
			addErrorMessage("tokenNotValid");
		}
	}

	protected String createLink(String token)
	{
		StringBuilder sb = new StringBuilder();
		String serverUrl = "http://"+FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("HOST");
		if(!getServerUrl().startsWith("/"))
		{
			serverUrl = serverUrl + "/";
		}
		serverUrl = serverUrl + getServerUrl();
		if(!serverUrl.endsWith("/"))
		{
			serverUrl= serverUrl + "/";
		}
		sb.append(serverUrl);
		sb.append(getConfig().getResetPasswordView());
		sb.append("?userId=");
		sb.append(userIdProperty.getValue(user));
		sb.append("&token=");
		sb.append(token);
		return sb.toString();
	}

	protected Object findLostPassword(String userId) throws NoResultException
	{
		return getEntityManager().createQuery("select l from " + lostPasswordClass.getName() + " l where " + lostPasswordUserProperty.getName() + "=:user and " + lostPasswordTokenProperty.getName() + "=:token").setParameter("user", findUserById(userId)).setParameter("token", token).getSingleResult();
	}

	protected Object findUser(String username) throws NoResultException, NonUniqueResultException
	{
		username = username.toLowerCase();
		
		if(isEmail(username))
		{
			return getEntityManager().createQuery("select u from " + userClass.getName() + " u where lower(u." + userEmailProperty.getName() + ")=:userEmail").setParameter("userEmail", username).getSingleResult();
		}
		return getEntityManager().createQuery("select u from " + userClass.getName() + " u where lower(u." + userNameProperty.getName() + ")=:userName").setParameter("userName", username).getSingleResult();
	}

	protected Object findUserById(String userId) throws NoResultException
	{
		return getEntityManager().createQuery("select u from " + userClass.getName() + " u where " + userIdProperty.getName() + "=:userId").setParameter("userId", parseUserId(userId)).getSingleResult();
	}

	protected long getLostPasswordCount(Object userId)
	{
		Object user = findUserById(userId.toString());
		Criteria criteria = createCriteria(lostPasswordClass);
		criteria.add(Restrictions.ge(lostPasswordTimestampProperty.getName(), new Date(System.currentTimeMillis() - getConfig().getExpiryTime())));
		criteria.add(Restrictions.eq(lostPasswordUserProperty.getName(), user));
		criteria.setProjection(Projections.rowCount());
		return (Long)criteria.list().get(0);
	}

	@Override
	protected String getMessageKeyPrefix()
	{
		return MESSAGE_KEY_PREFIX;
	}

	protected String getMessageRenderView()
	{
		return getConfig().getMessageRenderView();
	}

	protected String getServerUrl()
	{
		return getConfig().getServerUrl();
	}

	protected boolean isEmail(String nameOrEmail)
	{
		return nameOrEmail != null && nameOrEmail.trim().length() != 0 && EmailUtil.isEmailValid(nameOrEmail);
	}

	protected boolean isEpired(Object lostPassword)
	{
		return System.currentTimeMillis() - ((Date)lostPasswordTimestampProperty.getValue(lostPassword)).getTime() > getConfig().getExpiryTime();
	}

	protected boolean isUserId(String userId)
	{
		return userId != null && parseUserId(userId) != null;
	}

	protected Object parseUserId(String userId)
	{
		try
		{
			Type type = userIdProperty.getPropertyType();
			if(type.equals(Integer.class) || type.equals(int.class))
			{
				return new Integer(userId);
			}
			else if(type.equals(Long.class) || type.equals(long.class))
			{
				return new Long(userId);
			}
			else if(type.equals(Short.class) || type.equals(short.class))
			{
				return new Short(userId);
			}
			else if(type.equals(Byte.class) || type.equals(byte.class))
			{
				return new Byte(userId);
			}
			else
			{
				logger.error("Unsupported user id type", type);
				return null;
			}
		}
		catch(Exception e)
		{
			return null;
		}
	}

	protected void sendMail() throws Exception
	{
		try
		{

			renderer.render(getMessageRenderView());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
