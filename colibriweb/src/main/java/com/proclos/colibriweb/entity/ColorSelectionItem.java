package com.proclos.colibriweb.entity;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ColorSelectionItem extends SelectionItem
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5227863960172961584L;
	private String color;

	public String getColor()
	{
		return color;
	}

	public void setColor(String color)
	{
		this.color = color;
	}
}
