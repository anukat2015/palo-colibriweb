package com.proclos.colibriweb.entity.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import at.adaptive.components.usermanagement.annotation.LostPasswordTimestamp;
import at.adaptive.components.usermanagement.annotation.LostPasswordToken;
import at.adaptive.components.usermanagement.annotation.LostPasswordUser;

@Entity
public class LostPassword implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 149746614979273355L;
	private Integer id;
	private String token;
	private Date timestamp;
	private ColibriUser user;

	public LostPassword()
	{}

	public LostPassword(String token, ColibriUser user)
	{
		this.token = token;
		this.user = user;
	}

	@Id
	@GeneratedValue
	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	@LostPasswordToken
	public String getToken()
	{
		return token;
	}

	public void setToken(String token)
	{
		this.token = token;
	}

	@LostPasswordTimestamp
	public Date getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}

	@PrePersist
	protected void onCreate()
	{
		timestamp = new Date();
	}

	@PreUpdate
	protected void onUpdate()
	{
		timestamp = new Date();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@LostPasswordUser
	public ColibriUser getUser()
	{
		return user;
	}

	public void setUser(ColibriUser user)
	{
		this.user = user;
	}
}