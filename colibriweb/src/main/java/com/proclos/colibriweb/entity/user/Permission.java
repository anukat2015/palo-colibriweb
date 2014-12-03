package com.proclos.colibriweb.entity.user;

import javax.persistence.Entity;

import org.jboss.seam.annotations.security.permission.PermissionAction;
import org.jboss.seam.annotations.security.permission.PermissionDiscriminator;
import org.jboss.seam.annotations.security.permission.PermissionRole;
import org.jboss.seam.annotations.security.permission.PermissionTarget;
import org.jboss.seam.annotations.security.permission.PermissionUser;

import com.proclos.colibriweb.entity.BaseEntity;

@Entity
public class Permission extends BaseEntity
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3279011652524954045L;
	private String recipient;
	private String target;
	private String action;
	private String discriminator;

	@PermissionUser
	@PermissionRole
	public String getRecipient()
	{
		return recipient;
	}

	public void setRecipient(String recipient)
	{
		this.recipient = recipient;
	}

	@PermissionTarget
	public String getTarget()
	{
		return target;
	}

	public void setTarget(String target)
	{
		this.target = target;
	}

	@PermissionAction
	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	@PermissionDiscriminator
	public String getDiscriminator()
	{
		return discriminator;
	}

	public void setDiscriminator(String discriminator)
	{
		this.discriminator = discriminator;
	}
}
