package at.adaptive.components.documentmanagement.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import at.adaptive.components.documentmanagement.annotation.DocumentContent;
import at.adaptive.components.documentmanagement.annotation.DocumentFile;
import at.adaptive.components.documentmanagement.annotation.DocumentFileName;
import at.adaptive.components.bean.annotation.SearchField;

@MappedSuperclass
@Indexed
public abstract class Document<A, CAT, COM, TAG>
{
	// private List<A> authors = new ArrayList<A>();
	//
	// private List<CAT> categories = new ArrayList<CAT>();

	// private List<COM> comments = new ArrayList<COM>();

	private String content;

	private byte[] file;

	private String fileName;

	private Integer fileSize;

	private Long id;

	private Integer revisionNumber;

	// private List<TAG> tags = new ArrayList<TAG>();

	private String title;

	private String topic;

	private Integer versionNumber;

	// @SearchField(groups = "documentGroup")
	// @OneToMany
	// public List<A> getAuthors()
	// {
	// return authors;
	// }
	//
	// @SearchField(groups = "documentGroup")
	// @OneToMany
	// public List<CAT> getCategories()
	// {
	// return categories;
	// }

	// @SearchField(groups = "documentGroup")
	// @OneToMany
	// public List<COM> getComments()
	// {
	// return comments;
	// }

	@Lob
	@Type(type = "org.hibernate.type.StringClobType") 
	@SearchField(groups = "documentGroup")
	@Field(index = Index.YES, analyze=Analyze.YES, store = Store.NO)
	@DocumentContent
	public String getContent()
	{
		return content;
	}

	@Lob
	@Type(type = "org.hibernate.type.StringClobType") 
	@DocumentFile
	public byte[] getFile()
	{
		return file;
	}

	@DocumentFileName
	public String getFileName()
	{
		return fileName;
	}

	public Integer getFileSize()
	{
		return fileSize;
	}

	@Id
	@GeneratedValue
	@DocumentId
	public Long getId()
	{
		return id;
	}

	public Integer getRevisionNumber()
	{
		return revisionNumber;
	}

	// @SearchField(groups = "documentGroup")
	// @OneToMany
	// public List<TAG> getTags()
	// {
	// return tags;
	// }

	@SearchField(groups = "documentGroup")
	@Field(index = Index.YES, analyze=Analyze.YES, store = Store.NO)
	public String getTitle()
	{
		return title;
	}

	@SearchField(groups = "documentGroup")
	@Field(index = Index.YES, analyze=Analyze.YES, store = Store.NO)
	public String getTopic()
	{
		return topic;
	}

	public Integer getVersionNumber()
	{
		return versionNumber;
	}

	// public void setAuthors(List<A> authors)
	// {
	// this.authors = authors;
	// }
	//
	// public void setCategories(List<CAT> categories)
	// {
	// this.categories = categories;
	// }

	// public void setComments(List<COM> comments)
	// {
	// this.comments = comments;
	// }

	public void setContent(String content)
	{
		this.content = content;
	}

	public void setFile(byte[] file)
	{
		this.file = file;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	public void setFileSize(Integer fileSize)
	{
		this.fileSize = fileSize;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void setRevisionNumber(Integer revisionNumber)
	{
		this.revisionNumber = revisionNumber;
	}

	// public void setTags(List<TAG> tags)
	// {
	// this.tags = tags;
	// }

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setTopic(String topic)
	{
		this.topic = topic;
	}

	public void setVersionNumber(Integer versionNumber)
	{
		this.versionNumber = versionNumber;
	}
}
