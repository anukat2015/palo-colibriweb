package com.proclos.colibriweb.session.common;

import java.util.List;

public interface IEditComponent<T>
{
	T create();

	void createAfter(T afterItem);

	void delete(T item);

	void deleteAll();

	List<T> getItems();
	
	boolean canCreateNewItem();
	
	boolean canDeleteItem(T item);
	
}