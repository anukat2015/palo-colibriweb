package at.adaptive.components.fulltext;

import java.util.ArrayList;
import java.util.List;

import at.adaptive.components.config.BaseComponentConfig;

public class FulltextDataProviderConfig extends BaseComponentConfig
{
	private List<FulltextDataConfig> configs = new ArrayList<FulltextDataConfig>();

	public List<FulltextDataConfig> getConfigs()
	{
		return configs;
	}

	public void setConfigs(List<FulltextDataConfig> configs)
	{
		this.configs = configs;
	}
}
