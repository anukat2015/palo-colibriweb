package at.adaptive.components.fulltext;

public class FulltextDataConfig
{
	private String name;
	private String filename;
	private String beanClass;
	private boolean useFSDirectory;
	private String fsDirectoryPath;

	public String getBeanClass()
	{
		return beanClass;
	}

	public String getFilename()
	{
		return filename;
	}

	public String getFsDirectoryPath()
	{
		return fsDirectoryPath;
	}

	public String getName()
	{
		return name;
	}

	public boolean isUseFSDirectory()
	{
		return useFSDirectory;
	}

	public void setBeanClass(String beanClass)
	{
		this.beanClass = beanClass;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public void setFsDirectoryPath(String fsDirectoryPath)
	{
		this.fsDirectoryPath = fsDirectoryPath;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setUseFSDirectory(boolean useFSDirectory)
	{
		this.useFSDirectory = useFSDirectory;
	}
}
