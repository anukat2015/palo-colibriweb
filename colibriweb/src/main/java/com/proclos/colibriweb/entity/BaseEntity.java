package com.proclos.colibriweb.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.jboss.seam.contexts.Contexts;

import at.adaptive.components.bean.annotation.SearchField;

import com.jedox.etl.core.persistence.hibernate.IPersistable;
import com.proclos.colibriweb.common.ContextProperties;
import com.proclos.colibriweb.entity.user.ColibriUser;

@MappedSuperclass
public abstract class BaseEntity implements Serializable, IPersistable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4835772647508196108L;

	private Long id;
	private ColibriUser creator;
	private ColibriUser modificator;
	private Date creationDate;
	private Date modificationDate;
	private Boolean active = Boolean.TRUE;
	private Boolean updateModificationDate = true;

	private Integer version;

	@Version
	public Integer getVersion()
	{
		return version;
	}

	public void setVersion(Integer version)
	{
		this.version = version;
	}

	public Boolean getActive()
	{
		return active;
	}

	@SearchField
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreationDate()
	{
		return creationDate;
	}

	// @SearchField
	@ManyToOne(fetch = FetchType.LAZY)
	public ColibriUser getCreator()
	{
		return creator;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId()
	{
		return id;
	}

	@SearchField
	@Temporal(TemporalType.TIMESTAMP)
	public Date getModificationDate()
	{
		return modificationDate;
	}

	// @SearchField
	@ManyToOne(fetch = FetchType.LAZY)
	public ColibriUser getModificator()
	{
		return modificator;
	}

	@Transient
	public void initialize()
	{
		initializeEntity();
	}

	@PrePersist
	@PreUpdate
	public void prePersist()
	{
		ColibriUser currentUser = getCurrentUser();
		if(creationDate == null)
		{
			creationDate = new Date();
		}
		/*
		if(version == null)
		{
			version = new Integer(0);
		}
		*/
		if(creator == null)
		{
			creator = currentUser;
		}
		if((modificationDate == null || modificationDate.before(new Date()) && currentUser != null && updateModificationDate))
		{
			modificationDate = new Date();
		}
		if(currentUser != null)
		{
			modificator = currentUser;
		}
		if(active == null)
		{
			active = Boolean.TRUE;
		}
		if (!Boolean.TRUE.equals(updateModificationDate)) 
		{
			updateModificationDate = true;
		}
	}

	public void setActive(Boolean active)
	{
		this.active = active;
	}

	public void setCreationDate(Date creationDate)
	{
		this.creationDate = creationDate;
	}

	public void setCreator(ColibriUser creator)
	{
		this.creator = creator;
	}

	public void setId(Long id)
	{
		this.id = id;
	}
	
	public void setModificationDate(Date modificationDate)
	{
		this.modificationDate = modificationDate;
	}

	public void setModificator(ColibriUser modificator)
	{
		this.modificator = modificator;
	}

	@Transient
	protected ColibriUser getCurrentUser()
	{
		try
		{
			return (ColibriUser)Contexts.getSessionContext().get(ContextProperties.USER);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	protected void initializeEntity()
	{}
	
	@Transient
	protected boolean equalsObject(Object o1, Object o2) {
		if (o1 == null && o2 == null) return true;
		if (o1 != null) return o1.equals(o2);
		return false;
	}
	
	@Transient
	public boolean cascadesDelete() {
		return false;
	}

	@Transient
	public void setUpdateModificationDate(Boolean updateModificationDate) {
		this.updateModificationDate = updateModificationDate;
	}

	@Transient
	public Boolean getUpdateModificationDate() {
		return updateModificationDate;
	}
	
	@Transient
	public String getIconTitle() {
		return "";
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(obj == null)
		{
			return false;
		}
		if(getClass() != obj.getClass())
		{
			return false;
		}
		BaseEntity other = (BaseEntity)obj;
		if(id == null)
		{
			if(other.id != null)
			{
				return false;
			}
		}
		else if(!id.equals(other.id))
		{
			return false;
		}
		return true;
	}

}