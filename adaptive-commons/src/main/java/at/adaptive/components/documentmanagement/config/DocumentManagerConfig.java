package at.adaptive.components.documentmanagement.config;

import java.util.List;

import at.adaptive.components.config.BaseComponentConfig;

public class DocumentManagerConfig extends BaseComponentConfig
{
	private List<String> supportedDocumentTypes;

	public List<String> getSupportedDocumentTypes()
	{
		return supportedDocumentTypes;
	}

	public void setSupportedDocumentTypes(List<String> supportedDocumentTypes)
	{
		this.supportedDocumentTypes = supportedDocumentTypes;
	}
}
