package at.adaptive.components.datahandling.dataimport.csv;

import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import at.adaptive.components.datahandling.common.ColumnDefinition;
import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;
import at.adaptive.components.datahandling.dataimport.AbstractImport;
import at.adaptive.components.datahandling.dataimport.ImportException;
import au.com.bytecode.opencsv.CSVReader;

public class CSVImport<T> extends AbstractImport<T>
{
	private char separator = ';';
	private String encoding = "utf-8";
	private DateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	private CSVReader csvReader;
	private String[] values;

	public CSVImport(ColumnDefinitionContainer columnDefinitionContainer)
	{
		super(columnDefinitionContainer);
	}

	public void close() throws ImportException
	{
		try
		{
			csvReader.close();
		}
		catch(Exception e)
		{
			throw new ImportException("Error closing file", e);
		}
	}

	public void setDateformat(DateFormat dateformat)
	{
		this.dateformat = dateformat;
	}

	public void setEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	public void setSeparator(char separator)
	{
		this.separator = separator;
	}

	@Override
	protected void finish() throws ImportException
	{}

	@Override
	protected Object getValueFromObject(Object object, String propertyName)
	{
		Object ret = super.getValueFromObject(object, propertyName);
		if(ret != null)
		{
			return ret;
		}
		Class<?> propretyType = columnDefinitionContainer.getPropertyType(propertyName);
		if(propretyType == null)
		{
			return null;
		}
		if(Date.class.isAssignableFrom(propretyType))
		{
			if(object instanceof Double)
			{
				try
				{
					return new Date(((Double)object).longValue());
				}
				catch(Exception e)
				{
					return null;
				}
			}
		}
		return null;
	}

	@Override
	protected boolean hasNext() throws ImportException
	{
		return values != null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected T readLine() throws ImportException
	{
		try
		{
			// create object
			T object;
			if(useObjectArrayOutput)
			{
				int size = columnDefinitionContainer.size();
				if(size == 0)
				{
					size = values.length;
				}
				object = (T)new Object[size];
			}
			else
			{
				object = (T)columnDefinitionContainer.getBeanClass().newInstance();
			}
			if(columnDefinitionContainer.size() > 0)
			{
				for(ColumnDefinition columnDefinition : columnDefinitionContainer.getColumnDefinitions())
				{
					String value = values[columnDefinition.getPosition()];
					// try to get actual type
					Object actualValue = getValueFromObject(value, columnDefinition.getPropertyValue());
					if(useObjectArrayOutput)
					{
						((Object[])object)[columnDefinition.getPosition()] = actualValue;
					}
					else
					{
						beanUtil.setValue(object, columnDefinition, actualValue);
					}
				}
			}
			else
			{
				int size = columnDefinitionContainer.size();
				if(size == 0)
				{
					size = values.length;
				}
				for(int i = 0; i < size; i++)
				{
					String value = values[i];
					((Object[])object)[i] = value;
				}
			}
			// set row
			values = csvReader.readNext();
			return object;
		}
		catch(Exception e)
		{
			throw new ImportException("Error reading line", e);
		}
	}

	@Override
	protected void setup() throws ImportException
	{
		try
		{
			csvReader = new CSVReader(new InputStreamReader(in, encoding), separator, '\"');
			values = csvReader.readNext();
			if(ignoreHeader)
			{
				values = csvReader.readNext();
			}
		}
		catch(Exception e)
		{
			throw new ImportException("Error setting up xls-import", e);
		}
	}
}
