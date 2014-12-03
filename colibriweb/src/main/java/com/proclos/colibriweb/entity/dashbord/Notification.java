package com.proclos.colibriweb.entity.dashbord;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import at.adaptive.components.bean.annotation.SearchField;

import com.proclos.colibriweb.entity.BaseEntity;
import com.proclos.colibriweb.entity.user.ColibriUser;
import com.proclos.colibriweb.session.system.ModuleManager;

@Entity
public class Notification extends BaseEntity
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4057358267583582557L;
	private String targetModule;
	private String origin;
	private String title;
	private String details;
	private Long targetId;
	private ColibriUser targetUser;
	private Boolean read; // entspricht dem Wert isDone von den Tasks 
	private Object entity;
	private Date visibleDate;

	@Transient
	public Object getEntity()
	{
		if(entity == null)
		{
			entity = ModuleManager.instance().findEntity(targetModule, getTargetId());
		}
		return entity;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	@Lob
	@Type(type = "org.hibernate.type.StringClobType") 
	@SearchField(groups = {"listSearchProperties"})
	public String getTitle()
	{
		return title;
	}

	public void setRead(Boolean read)
	{
		this.read = read;
	}

	public Boolean getRead()
	{
		return read;
	}

	public void setTargetModule(String targetModule)
	{
		this.targetModule = targetModule;
	}

	public String getTargetModule()
	{
		return targetModule;
	}

	public void setTargetId(Long targetId)
	{
		this.targetId = targetId;
	}

	public Long getTargetId()
	{
		return targetId;
	}

	public void setTargetUser(ColibriUser targetUser)
	{
		this.targetUser = targetUser;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	public ColibriUser getTargetUser()
	{
		return targetUser;
	}

	public void setOrigin(String origin)
	{
		this.origin = origin;
	}

	@SearchField(groups = {"listSearchProperties"})
	public String getOrigin()
	{
		return origin;
	}

	public void setDetails(String details)
	{
		this.details = details;
	}

	@Lob
	@Type(type = "org.hibernate.type.StringClobType") 
	public String getDetails()
	{
		return details;
	}

	public void setVisibleDate(Date visibleDate) {
		this.visibleDate = visibleDate;
	}

	public Date getVisibleDate() {
		return visibleDate;
	}

}
