package at.adaptive.components.datahandling.dataexport;

public enum ExportType
{
	CSV("CSV", "Comma Separated Values"), XLS("XLS", "Microsoft Excel 97-2003"), XLSX("XLSX", "Microsoft Excel 2007"), XML("XML", "Extensible Markup Language"), VCARD("VCARD", "VCard");

	private String name;
	private String description;

	private ExportType(String name, String description)
	{
		this.name = name;
		this.description = description;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
