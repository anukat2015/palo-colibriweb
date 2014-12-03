package at.adaptive.components.documentmanagement.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import at.adaptive.components.bean.annotation.SearchField;

@Indexed
@Entity
public class DocumentComment
{
	private Integer id;
	private String text;

	@Id
	@GeneratedValue
	@DocumentId
	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	@Lob
	@SearchField
	@Field(index = Index.YES, analyze=Analyze.YES, store = Store.NO)
	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

}
