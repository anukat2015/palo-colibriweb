package at.adaptive.components.datahandling.dataexport.vcard;

public class VCardEntry
{
	private StringBuilder sb = new StringBuilder();
	private static final String BEGIN_VCARD = "BEGIN:VCARD";
	private static final String END_VCARD = "END:VCARD";
	private static final String VERSION = "VERSION:3.0";
	private static final String NEW_LINE = "\n";
	private static final String SEPARATOR = ":";
	private static final String CHARSET = ";CHARSET=";
	private String encoding;

	public VCardEntry(String encoding)
	{
		this.encoding = encoding;
		init();
	}

	private void init()
	{
		sb.append(BEGIN_VCARD);
		sb.append(NEW_LINE);
		sb.append(VERSION);
		sb.append(NEW_LINE);
	}

	public String createVCardString()
	{
		finish();
		return sb.toString();
	}

	private void finish()
	{
		sb.append(END_VCARD);
		sb.append(NEW_LINE);
	}

	public void addProperty(String key, String value)
	{
		sb.append(key);
		if(encoding != null)
		{
			sb.append(CHARSET);
			sb.append(encoding.toLowerCase());
		}
		sb.append(SEPARATOR);
		sb.append(foldString(value));
		sb.append(NEW_LINE);
	}

	private String foldString(String str)
	{
		if(str.endsWith("\r\n"))
		{
			str = str.substring(0, str.length() - 2);
		}
		else if(str.endsWith("\n"))
		{
			str = str.substring(0, str.length() - 1);
		}
		str = str.replaceAll("\r\n", "\n");
		return str.replaceAll("\n", "\n ");
	}
}
