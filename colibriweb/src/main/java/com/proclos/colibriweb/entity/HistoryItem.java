package com.proclos.colibriweb.entity;

import java.util.Date;

import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@MappedSuperclass
public abstract class HistoryItem extends BaseEntity implements Cloneable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1514097074815655745L;
	private boolean unofficial;
	private boolean invalid;
	private Date invalidationDate;

	@Temporal(TemporalType.TIMESTAMP)
	public Date getInvalidationDate()
	{
		return invalidationDate;
	}

	public boolean isInvalid()
	{
		return invalid;
	}

	public boolean isUnofficial()
	{
		return unofficial;
	}

	public void setInvalid(boolean invalid)
	{
		this.invalid = invalid;
		if(invalid && invalidationDate == null) invalidationDate = new Date();
	}

	public void setInvalidationDate(Date invalidationDate)
	{
		this.invalidationDate = invalidationDate;
	}

	public void setUnofficial(boolean unofficial)
	{
		this.unofficial = unofficial;
	}

	@Transient
	public int getHistoryItemHashCode()
	{
		return 0;
	}

	@Override
	public Object clone()
	{
		try
		{
			HistoryItem historyItem = (HistoryItem)super.clone();
			historyItem.setId(null);
			historyItem.setCreator(null);
			historyItem.setModificator(null);
			historyItem.setCreationDate(null);
			historyItem.setModificationDate(null);
			return historyItem;
		}
		catch(CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
