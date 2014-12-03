package com.proclos.colibriweb.entity.user;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.jboss.seam.annotations.security.management.UserEnabled;
import org.jboss.seam.annotations.security.management.UserPassword;
import org.jboss.seam.annotations.security.management.UserPrincipal;
import org.jboss.seam.annotations.security.management.UserRoles;

import at.adaptive.components.bean.annotation.SearchField;
import at.adaptive.components.common.StringUtil;
import at.adaptive.components.usermanagement.annotation.UserEmail;

import com.proclos.colibriweb.entity.BaseEntity;

@Entity
@Indexed
public class ColibriUser extends BaseEntity
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 268491769905183600L;

	private String username;
	private String password;
	private boolean enabled;
	private String title;
	private String firstname;
	private String lastname;
	private String email;
	private String jobtitle;
	private String completeName;
	private String signature;

	private List<ColibriRole> roles = new ArrayList<ColibriRole>();

	public String getSignature()
	{
		return signature;
	}

	public void setSignature(String signature)
	{
		this.signature = signature;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof ColibriUser)) return false;
		ColibriUser other = (ColibriUser)obj;
		if(getId() == null)
		{
			if(other.getId() != null) return false;
		}
		else if(!getId().equals(other.getId())) return false;
		if(getUsername() == null)
		{
			if(other.getUsername() != null) return false;
		}
		else if(!getUsername().equals(other.getUsername())) return false;
		return true;
	}

	@SearchField(groups = {"user", "decurs", "meeting"})
	@Field
	public String getCompleteName()
	{
		return completeName;
	}

	@Email(message = "#{messages['usermanagement.emailNotValid']}")
	@UserEmail
	@Length(max = 255)
	public String getEmail()
	{
		return email;
	}

	@SearchField(groups = {"user", "decurs", "meeting"})
	public String getFirstname()
	{
		return firstname;
	}

	public String getJobtitle()
	{
		return jobtitle;
	}

	@Transient
	public String getLabel()
	{
		return getCompleteName();
	}
	
	@Transient
	public String getDisplayName()
	{
		return getLastname()+" "+getFirstname();
	}

	@SearchField(groups = {"user", "decurs", "meeting"})
	public String getLastname()
	{
		return lastname;
	}

	@UserPassword(hash = "md5")
	public String getPassword()
	{
		return password;
	}

	@UserRoles
	@ManyToMany(fetch = FetchType.LAZY)
	public List<ColibriRole> getRoles()
	{
		return roles;
	}

	public String getTitle()
	{
		return title;
	}

	@SearchField(groups = {"user","decurs","meeting"})
	@UserPrincipal
	public String getUsername()
	{
		return username;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result + ((getUsername() == null) ? 0 : getUsername().hashCode());
		return result;
	}

	@UserEnabled
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	@PrePersist
	@PreUpdate
	public void prePersist()
	{
		super.prePersist();
		setCompleteName(computeCompleteName());
	}

	public void setCompleteName(String completeName)
	{
		this.completeName = completeName;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public void setFirstname(String firstname)
	{
		this.firstname = firstname;
	}

	// public void setFiscalJobGroup(JobGroup fiscalJobGroup)
	// {
	// this.fiscalJobGroup = fiscalJobGroup;
	// }

	public void setJobtitle(String jobtitle)
	{
		this.jobtitle = jobtitle;
	}

	public void setLastname(String lastname)
	{
		this.lastname = lastname;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public void setRoles(List<ColibriRole> roles)
	{
		this.roles = roles;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	@Transient
	private String computeCompleteName()
	{
		StringBuilder sb = new StringBuilder();
		if(!StringUtil.isEmpty(title))
		{
			sb.append(title);
			sb.append(" ");
		}
		if(!StringUtil.isEmpty(firstname))
		{
			sb.append(firstname);
			sb.append(" ");
		}
		if(!StringUtil.isEmpty(lastname))
		{
			sb.append(lastname);
		}
		if(sb.length() == 0)
		{
			sb.append(username);
		}
		return sb.toString();
	}
	
	@Transient
	public boolean hasRole(String roleName) {
		for (ColibriRole role : getRoles()) {
			if (role.getName().equalsIgnoreCase(roleName)) return true;
		}
		return false;
	}
	
	@Transient
	public boolean hasGondorRole(String roleName) {
		for (ColibriRole role : getRoles()) {
			if (role.getGondorRole().equalsIgnoreCase(roleName)) return true;
		}
		return false;
	}

}
