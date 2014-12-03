package at.adaptive.components.common;

import org.apache.log4j.Logger;

public class HTMLFormatter
{
	private static final Logger logger = Logger.getLogger(HTMLFormatter.class);

	public static String format(String content)
	{
		if(content == null)
		{
			return null;
		}
		try
		{
			content = content.replaceAll("\\r\\n", "<br />");
			content = content.replaceAll("\\n", "<br />");
			return content;
		}
		catch(Exception e)
		{
			logger.error("Error formatting content", e);
			return content;
		}
	}
}
