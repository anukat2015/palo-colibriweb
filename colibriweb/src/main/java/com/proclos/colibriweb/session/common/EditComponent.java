package com.proclos.colibriweb.session.common;

import java.util.List;

public class EditComponent<T> implements IEditComponent<T>
{
	protected Class<T> itemClass;
	protected List<T> items;
	private Integer maxItems;
	private Integer minItems;

	public EditComponent(List<T> items, Class<T> itemClass, Integer minItems, Integer maxItems)
	{
		super();
		this.items = items;
		this.itemClass = itemClass;
		this.maxItems = maxItems;
		this.minItems = minItems;
	}
	
	protected void listChanged() {}

	public T create()
	{
		T item = createNewItem();
		items.add(item);
		listChanged();
		return item;
	}

	public void createAfter(T afterItem)
	{
		if(items.contains(afterItem))
		{
			T item = createNewItem();
			items.add(items.indexOf(afterItem) + 1, item);
			listChanged();

		}
	}

	public void createBefore(T beforeItem)
	{
		if(items.contains(beforeItem))
		{
			T item = createNewItem();
			items.add(items.indexOf(beforeItem), item);
			listChanged();

		}
	}

	public void delete(T item)
	{
		items.remove(item);
		listChanged();

	}

	public void deleteAll()
	{
		for(int i = (items.size() - 1); i >= 0; i--)
		{
			T item = items.get(i);
			delete(item);
		}
	}

	public List<T> getItems()
	{
		return items;
	}

	protected T createNewItem()
	{
		try
		{
			return (T)itemClass.newInstance();
		}
		catch(Exception e)
		{
			return null;
		}
	}

	@Override
	public boolean canCreateNewItem() {
		return (maxItems == null ||  maxItems > items.size());
	}
	
	public boolean canDeleteItem(T item) {
		if (item != null) {
			return minItems < items.size();
		}
		return false;
	}
	
	public Integer getMaxItems() {
		return maxItems;
	}

	public void setMaxItems(Integer maxItems) {
		this.maxItems = maxItems;
	}

	public Integer getMinItems() {
		return minItems;
	}

	public void setMinItems(Integer minItems) {
		this.minItems = minItems;
	}
}
