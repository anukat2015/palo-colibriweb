package com.proclos.colibriweb.session.system.config;

public class FilterCondition
{

	public enum Languages
	{
		hql, sql
	}

	private String definition;
	private Languages language;
	private boolean shortNotation;

	public FilterCondition(String definition, String language, boolean shortNotation)
	{
		setDefinition(definition);
		setLanguage(Languages.valueOf(language));
		setShortNotation(shortNotation);
	}

	public String getDefinition()
	{
		return definition;
	}

	public Languages getLanguage()
	{
		return language;
	}

	public boolean isShortNotation()
	{
		return shortNotation;
	}

	public void setDefinition(String definition)
	{
		this.definition = definition;
	}

	public void setLanguage(Languages language)
	{
		this.language = language;
	}

	public void setShortNotation(boolean shortNotation)
	{
		this.shortNotation = shortNotation;
	}

}
