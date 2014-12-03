package at.adaptive.components.datahandling.dataexport.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import at.adaptive.components.datahandling.common.ColumnDefinition;
import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;
import at.adaptive.components.datahandling.dataexport.AbstractExport;
import at.adaptive.components.datahandling.dataexport.ExportException;

public class CSVExport<T> extends AbstractExport<T>
{
	private static final String SUFFIX = "csv";

	private String separator = ";";
	private String encoding = "utf-8";
	private DateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	private BufferedWriter writer;
	private EnclosingStrategy enclosingStrategy = EnclosingStrategy.NONE;
	private int linesWritten = 0;

	public CSVExport(ColumnDefinitionContainer columnDefinitionContainer)
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

	public EnclosingStrategy getEnclosingStrategy()
	{
		return enclosingStrategy;
	}

	public String getFileSuffix()
	{
		return SUFFIX;
	}

	public void setDateformat(DateFormat dateformat)
	{
		this.dateformat = dateformat;
	}

	public void setEnclosingStrategy(EnclosingStrategy enclosingStrategy)
	{
		this.enclosingStrategy = enclosingStrategy;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	public void setSeparator(String separator)
	{
		this.separator = separator;
	}

	@Override
	protected void addHeaderValue(ColumnDefinition columnDefinition) throws ExportException
	{
		writeValue(columnDefinition, columnDefinition.getValue());
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

	@Override
	protected void handleLineComplete(boolean hasNext) throws ExportException
	{
		try
		{
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

	public String getSeparator()
	{
		return separator;
	}

	@Override
	protected void writeValue(ColumnDefinition columnDefinition, Object value) throws ExportException
	{
		try
		{
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
					stringValue = stringValue.replaceAll(separator, "");
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
			boolean enclose;
			if(enclosingStrategy.equals(EnclosingStrategy.ALL))
			{
				enclose = true;
			}
			else if(enclosingStrategy.equals(EnclosingStrategy.NONE))
			{
				enclose = false;
			}
			else
			{
				if(value instanceof String)
				{
					enclose = true;
				}
				else
				{
					enclose = false;
				}
			}
			if(enclose)
			{
				StringBuilder sb = new StringBuilder();
				sb.append("\"");
				sb.append(stringValue);
				sb.append("\"");
				stringValue = sb.toString();
			}
			writer.write(stringValue);
			if(columnDefinition.getPosition() < (columnDefinitionContainer.size() - 1))
			{
				writer.write(separator);
			}
		}
		catch(IOException e)
		{
			throw new ExportException("Error setting value", e);
		}
	}
}
