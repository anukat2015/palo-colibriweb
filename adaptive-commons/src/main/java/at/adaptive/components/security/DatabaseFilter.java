package at.adaptive.components.security;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class DatabaseFilter
{
	private String filterName;
	private FilterType filterType;
	private Integer id;
	private String recipient;
	private String enabled;

	public String getFilterName()
	{
		return filterName;
	}

	public FilterType getFilterType()
	{
		return filterType;
	}

	@Id
	@GeneratedValue
	public Integer getId()
	{
		return id;
	}

	public String getRecipient()
	{
		return recipient;
	}

	@Transient
	public boolean isRoleFilter()
	{
		return filterType != null && filterType.equals(FilterType.ROLE);
	}

	public void setFilterName(String filterName)
	{
		this.filterName = filterName;
	}

	public void setFilterType(FilterType filterType)
	{
		this.filterType = filterType;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public void setRecipient(String recipient)
	{
		this.recipient = recipient;
	}

	public String getEnabled()
	{
		return enabled;
	}

	public void setEnabled(String enabled)
	{
		this.enabled = enabled;
	}
}
