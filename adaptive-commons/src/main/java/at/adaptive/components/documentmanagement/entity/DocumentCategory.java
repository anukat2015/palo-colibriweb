package at.adaptive.components.documentmanagement.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import at.adaptive.components.bean.annotation.SearchField;

@Entity
public class DocumentCategory
{
	private Integer id;
	private String name;

	@Id
	@GeneratedValue
	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	@SearchField
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
