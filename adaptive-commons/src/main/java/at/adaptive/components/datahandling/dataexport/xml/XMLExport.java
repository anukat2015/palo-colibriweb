package at.adaptive.components.datahandling.dataexport.xml;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import at.adaptive.components.datahandling.common.ColumnDefinition;
import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;
import at.adaptive.components.datahandling.dataexport.AbstractExport;
import at.adaptive.components.datahandling.dataexport.ExportException;

public class XMLExport<T> extends AbstractExport<T>
{
	private static final String SUFFIX = "xml";
	private String rootName = "root";
	private String nodeName = "entry";
	private boolean usePropertyNamesForElements = false;
	private DateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	private XMLWriter writer;
	private Document document;
	private Element rootElement;
	private Element currentElement;
	private boolean exportEmptyElements;
	private String encoding = "utf-8";

	public XMLExport(ColumnDefinitionContainer columnDefinitionContainer)
	{
		super(columnDefinitionContainer);
	}

	public void close() throws ExportException
	{
		try
		{
			writer.close();
		}
		catch(IOException e)
		{
			throw new ExportException("Error closing export", e);
		}
	}

	public String getEncoding()
	{
		return encoding;
	}

	public String getFileSuffix()
	{
		return SUFFIX;
	}

	public String getNodeName()
	{
		return nodeName;
	}

	public String getRootName()
	{
		return rootName;
	}

	public boolean isExportEmptyElements()
	{
		return exportEmptyElements;
	}

	public boolean isUsePropertyNamesForElements()
	{
		return usePropertyNamesForElements;
	}

	public void setDateformat(DateFormat dateformat)
	{
		this.dateformat = dateformat;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	public void setExportEmptyElements(boolean exportEmptyElements)
	{
		this.exportEmptyElements = exportEmptyElements;
	}

	public void setNodeName(String nodeName)
	{
		this.nodeName = nodeName;
	}

	public void setRootName(String rootName)
	{
		this.rootName = rootName;
	}

	public void setUsePropertyNamesForElements(boolean usePropertyNamesForElements)
	{
		this.usePropertyNamesForElements = usePropertyNamesForElements;
	}

	@Override
	protected void addHeaderValue(ColumnDefinition columnDefinition) throws ExportException
	{}

	@Override
	protected void finish() throws ExportException
	{
		try
		{
			writer.write(document);
			writer.flush();
		}
		catch(IOException e)
		{
			throw new ExportException("Error finishing export", e);
		}
	}

	@Override
	protected void handleLineComplete(boolean hasNext) throws ExportException
	{
		createElement();
	}

	@Override
	protected void setup() throws ExportException
	{
		try
		{
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding(encoding);
			writer = new XMLWriter(out, format);
			document = DocumentHelper.createDocument();
			rootElement = document.addElement(getRootName());
		}
		catch(UnsupportedEncodingException e)
		{
			throw new ExportException("Unsupported encoding", e);
		}
	}

	@Override
	protected void writeValue(ColumnDefinition columnDefinition, Object value) throws ExportException
	{
		try
		{
			String stringValue;
			if(value == null)
			{
				if(isExportEmptyElements())
				{
					stringValue = "";
				}
				else
				{
					return;
				}
			}
			else
			{
				if(value instanceof String)
				{
					stringValue = (String)value;
				}
				else if(value instanceof Number)
				{
					stringValue = value.toString();
				}
				else if(value instanceof Date)
				{
					stringValue = dateformat.format((Date)value);
				}
				else if(value instanceof Calendar)
				{
					stringValue = dateformat.format(((Calendar)value).getTime());
				}
				else
				{
					stringValue = "";
				}
			}
			String name;
			if(isUsePropertyNamesForElements() && !columnDefinitionContainer.isUseEl())
			{
				name = columnDefinition.getPropertyValue();
			}
			else
			{
				name = columnDefinition.getValue();
			}
			if(currentElement == null)
			{
				createElement();
			}
			Element subElement = currentElement.addElement(name);
			subElement.setText(stringValue);
		}
		catch(Exception e)
		{
			throw new ExportException("Error writing element", e);
		}
	}

	private void createElement()
	{
		currentElement = rootElement.addElement(getNodeName());
	}

}
