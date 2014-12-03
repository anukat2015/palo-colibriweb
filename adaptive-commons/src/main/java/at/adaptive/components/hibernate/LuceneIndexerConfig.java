package at.adaptive.components.hibernate;

import java.util.List;

import at.adaptive.components.config.BaseComponentConfig;

public class LuceneIndexerConfig extends BaseComponentConfig
{
	private List<String> urls;

	public List<String> getUrls()
	{
		return urls;
	}

	public void setUrls(List<String> urls)
	{
		this.urls = urls;
	}
}
