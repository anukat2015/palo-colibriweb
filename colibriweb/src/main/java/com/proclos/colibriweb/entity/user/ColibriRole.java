package com.proclos.colibriweb.entity.user;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.jboss.seam.annotations.security.management.RoleName;

import com.proclos.colibriweb.entity.SelectionItem;

@Entity
public class ColibriRole extends SelectionItem
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4894815003632262600L;
	
	public static final String ADMIN = "admin";
	public static final String USER = "user";

	@RoleName
	@Override
	@Transient
	public String getName()
	{
		return super.getName();
	}
	
	@Transient
	public String getGondorRole() {
		return (getDescription() != null) ? getDescription() : getName();
	}
	
	
}
