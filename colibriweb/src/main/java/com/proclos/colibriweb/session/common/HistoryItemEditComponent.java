package com.proclos.colibriweb.session.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Validation;
import javax.validation.Validator;

import com.proclos.colibriweb.entity.HistoryItem;

public class HistoryItemEditComponent<T extends HistoryItem> extends EditComponent<T>
{
	private static Validator validator =  Validation.buildDefaultValidatorFactory().getValidator();
	private Integer maxValidItems;

	public HistoryItemEditComponent(List<T> items, Class<T> itemClass)
	{
		this(items, itemClass, null);
	}

	public HistoryItemEditComponent(List<T> items, Class<T> itemClass, Integer maxValidItems)
	{
		super(items, itemClass, 0,maxValidItems);
		this.maxValidItems = maxValidItems;
	}

	public boolean canCreateNewItem()
	{
		for(T item : items)
		{
			if(!isValid(item))
			{
				return false;
			}
		}
		return true;
	}

	public boolean canSetValid(T item)
	{
		if(!item.isInvalid())
		{
			return false;
		}
		if(maxValidItems == null)
		{
			return true;
		}
		int count = 0;
		for(T currentItem : items)
		{
			if(currentItem.isUnofficial() == item.isUnofficial() && !currentItem.isInvalid())
			{
				count++;
			}
		}
		return count < maxValidItems;
	}

	@Override
	public T create()
	{
		return create(true);
	}

	public T create(boolean unoffical)
	{
		if(!canCreateNewItem())
		{
			return null;
		}
		T item = createNewItem();
		item.setUnofficial(unoffical);
		setCurrentItemsInvalid(unoffical);
		items.add(0, item);
		return item;
	}

	public void delete(T item)
	{
		if(!item.isInvalid())
		{
			T lastMatchingItem = getLastMatchingItem(item.isUnofficial());
			if(lastMatchingItem != null)
			{
				lastMatchingItem.setInvalid(false);
			}
		}
		super.delete(item);
	}

	public boolean hasInvalidItem()
	{
		for(T item : items)
		{
			if(item.isInvalid())
			{
				return true;
			}
		}
		return false;
	}

	public boolean isManaged(T item)
	{
		return item.getId() != null;
	}

	public boolean isValid(T item)
	{
		try
		{
			return validator.validate(item).isEmpty();
		}
		catch(Exception ise)
		{
			return false;
		}
	}

	public void setInvalid(T item)
	{
		item.setInvalid(true);
	}

	public void setValid(T item)
	{
		item.setInvalid(false);
	}

	public T getLastValidItem()
	{
		T result = null;
		Date last = new Date(0);
		for(T item : items)
		{
			if(!item.isInvalid() && item.getActive())
			{
				Date modDate = (item.getModificationDate() != null ? item.getModificationDate() : new Date());
				if(modDate.after(last))
				{
					last = modDate;
					result = item;
				}
			}
		}
		return result;
	}
	
	public T getLastValidItem(boolean official)
	{
		T result = null;
		Date last = new Date(0);
		for(T item : items)
		{
			if(!item.isInvalid() && item.getActive() && item.isUnofficial() != official)
			{
				Date modDate = (item.getModificationDate() != null ? item.getModificationDate() : new Date());
				if(modDate.after(last))
				{
					last = modDate;
					result = item;
				}
			}
		}
		return result;
	}
	
	public List<T> getValidItems() {
		List<T> result = new ArrayList<T>();
		for(T item : items)
		{
			if(!item.isInvalid() && item.getActive())
			{
				result.add(item);
			}
		}
		return result;
	}

	private T getLastMatchingItem(boolean unofficial)
	{
		for(T item : items)
		{
			if(item.isInvalid() && item.isUnofficial() == unofficial)
			{
				return item;
			}
		}
		return null;
	}

	private void setCurrentItemsInvalid(boolean unoffical)
	{
		int count = 0;
		for(T item : items)
		{
			if(item.isUnofficial() == unoffical)
			{
				count++;
				boolean setInvalid = false;
				if(maxValidItems != null)
				{
					if(count >= maxValidItems)
					{
						setInvalid = true;
					}
				}
				if(setInvalid)
				{
					item.setInvalid(true);
				}
			}
		}
	}
}
