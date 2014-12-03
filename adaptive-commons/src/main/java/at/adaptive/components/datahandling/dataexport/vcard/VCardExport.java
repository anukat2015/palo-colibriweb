package at.adaptive.components.datahandling.dataexport.vcard;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import at.adaptive.components.common.StringUtil;
import at.adaptive.components.datahandling.common.ColumnDefinition;
import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;
import at.adaptive.components.datahandling.dataexport.AbstractExport;
import at.adaptive.components.datahandling.dataexport.ExportException;

public class VCardExport<T> extends AbstractExport<T>
{
	private static final String SUFFIX = "vcf";

	private String encoding = "utf-8";
	private BufferedWriter writer;
	private DateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	private VCardEntry vCardEntry;
	private int linesWritten = 0;

	public VCardExport(ColumnDefinitionContainer columnDefinitionContainer)
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

	public String getFileSuffix()
	{
		return SUFFIX;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	@Override
	protected void addHeaderValue(ColumnDefinition columnDefinition) throws ExportException
	{
		// not supported
	}

	@Override
	protected void finish() throws ExportException
	{
		try
		{
			writer.flush();
		}
		catch(IOException e)
		{
			throw new ExportException("Error finishing export", e);
		}
	}

	protected void addLine(T object) throws ExportException
	{
		try
		{
			vCardEntry = new VCardEntry(encoding);
			super.addLine(object);
			String vCardString = vCardEntry.createVCardString();
			writer.write(vCardString);
			writer.newLine();
			linesWritten++;
			if(linesWritten % 50 == 0)
			{
				writer.flush();
			}
		}
		catch(IOException e)
		{
			throw new ExportException("Error adding new line", e);
		}
	}

	@Override
	protected void handleLineComplete(boolean hasNext) throws ExportException
	{}

	@Override
	protected void setup() throws ExportException
	{
		try
		{
			writer = new BufferedWriter(new OutputStreamWriter(out, encoding));
		}
		catch(UnsupportedEncodingException e)
		{
			throw new ExportException("Unsupported encoding: " + encoding, e);
		}
	}

	@Override
	protected void writeValue(ColumnDefinition columnDefinition, Object value) throws ExportException
	{
		try
		{
			String vCardProperty = columnDefinition.getValue();
			if(vCardProperty == null)
			{
				return;
			}
			String stringValue;
			if(value == null)
			{
				stringValue = "";
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
			if(StringUtil.isEmpty(stringValue))
			{
				return;
			}
			vCardEntry.addProperty(vCardProperty.toUpperCase(), stringValue);
		}
		catch(Exception e)
		{
			throw new ExportException("Error setting value", e);
		}
	}
}
