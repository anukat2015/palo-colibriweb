package com.proclos.colibriweb.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import at.adaptive.components.bean.annotation.SearchField;
import at.adaptive.components.common.StringUtil;

import com.proclos.colibriweb.common.ISelectionItem;

@MappedSuperclass
public abstract class SelectionItem extends BaseEntity implements ISelectionItem
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4828839542606364886L;
	private String name;
	private String label;
	private String description;
	private Integer sortOrder;
	private boolean immutable = false;

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof SelectionItem)) return false;
		SelectionItem other = (SelectionItem)obj;
		if(getId() == null)
		{
			if(other.getId() != null) return false;
		}
		else if(!getId().equals(other.getId())) return false;
		if(getName() == null)
		{
			if(other.getName() != null) return false;
		}
		else if(!getName().equals(other.getName())) return false;
		return true;
	}

	@SearchField(groups = {"listSearchProperties"})
	@Column(length = 32768)
	public String getDescription()
	{
		return description;
	}

	@SearchField(groups = {"listSearchProperties"})
	public String getLabel()
	{
		return label;
	}

	@Column(nullable=false, unique=true)
	@SearchField(groups = {"listSearchProperties"})
	public String getName()
	{
		return name;
	}

	public Integer getSortOrder()
	{
		return sortOrder;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	public boolean isImmutable()
	{
		return immutable;
	}

	@PrePersist
	@PreUpdate
	public void prePersist()
	{
		super.prePersist();
		if(StringUtil.isEmpty(label))
		{
			label = name;
		}
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setImmutable(boolean immutable)
	{
		this.immutable = immutable;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setSortOrder(Integer sortOrder)
	{
		this.sortOrder = sortOrder;
	}
}
