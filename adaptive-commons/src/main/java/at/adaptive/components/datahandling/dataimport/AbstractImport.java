package at.adaptive.components.datahandling.dataimport;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import at.adaptive.components.datahandling.common.BeanUtil;
import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;

public abstract class AbstractImport<T> implements Import<T>
{
	protected static final Logger logger = Logger.getLogger(AbstractImport.class);

	protected InputStream in;

	protected BeanUtil<T> beanUtil;

	protected ColumnDefinitionContainer columnDefinitionContainer;

	private DateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

	protected boolean ignoreHeader = true;

	protected boolean useObjectArrayOutput = false;

	public AbstractImport(ColumnDefinitionContainer columnDefinitionContainer)
	{
		this.columnDefinitionContainer = columnDefinitionContainer;
		beanUtil = new BeanUtil<T>(columnDefinitionContainer);
		useObjectArrayOutput = columnDefinitionContainer.getBeanClass().isAssignableFrom(Object[].class);
	}

	public void setSuppressErrors(boolean suppressErrors)
	{
		beanUtil.setSuppressErrors(suppressErrors);
	}

	public boolean isSuppressErrors()
	{
		return beanUtil.isSuppressErrors();
	}

	public List<T> importData(InputStream in) throws ImportException
	{
		return importData(in, -1);
	}

	public List<T> importData(InputStream in, int maxRecords) throws ImportException
	{
		this.in = in;
		setup();
		List<T> list = new ArrayList<T>();
		int count = 0;
		while(hasNext())
		{
			T element = readLine();
			list.add(element);
			count++;
			if(maxRecords > 0 && count >= maxRecords)
			{
				break;
			}
		}
		finish();
		close();
		return list;
	}

	public boolean isIgnoreHeader()
	{
		return ignoreHeader;
	}

	public void setIgnoreHeader(boolean ignoreHeader)
	{
		this.ignoreHeader = ignoreHeader;
	}

	protected abstract void finish() throws ImportException;

	protected Object getValueFromObject(Object object, String propertyName)
	{
		if(useObjectArrayOutput)
		{
			if(object != null && object instanceof Double)
			{
				try
				{
					return new BigDecimal((Double)object).intValueExact();
				}
				catch(ArithmeticException ae)
				{
					return object;
				}
			}
			return object;
		}
		if(object == null || propertyName == null)
		{
			return object;
		}
		Class<?> propertyType = columnDefinitionContainer.getPropertyType(propertyName);
		if(propertyType == null)
		{
			return null;
		}
		if(String.class.isAssignableFrom(propertyType))
		{
			String value = object.toString();
			if(object instanceof Double || object instanceof Float && value.endsWith(".0"))
			{
				value = value.substring(0, value.lastIndexOf(".0"));
			}
			return value;
		}
		else if(Byte.class.isAssignableFrom(propertyType) || byte.class.isAssignableFrom(propertyType))
		{
			if(object instanceof Number)
			{
				return ((Number)object).byteValue();
			}
			if(object instanceof String)
			{
				try
				{
					return Byte.parseByte(object.toString());
				}
				catch(Exception e)
				{}
			}
		}
		else if(Short.class.isAssignableFrom(propertyType) || short.class.isAssignableFrom(propertyType))
		{
			if(object instanceof Number)
			{
				return ((Number)object).shortValue();
			}
			if(object instanceof String)
			{
				try
				{
					return Short.parseShort(object.toString());
				}
				catch(Exception e)
				{}
			}
		}
		else if(Integer.class.isAssignableFrom(propertyType) || int.class.isAssignableFrom(propertyType))
		{
			if(object instanceof Number)
			{
				return ((Number)object).intValue();
			}
			if(object instanceof String)
			{
				try
				{
					return Integer.parseInt(object.toString());
				}
				catch(Exception e)
				{}
			}
		}
		else if(Long.class.isAssignableFrom(propertyType) || long.class.isAssignableFrom(propertyType))
		{
			if(object instanceof Number)
			{
				return ((Number)object).longValue();
			}
			if(object instanceof String)
			{
				try
				{
					return Long.parseLong(object.toString());
				}
				catch(Exception e)
				{}
			}
		}
		else if(Float.class.isAssignableFrom(propertyType) || float.class.isAssignableFrom(propertyType))
		{
			if(object instanceof Number)
			{
				return ((Number)object).floatValue();
			}
			if(object instanceof String)
			{
				try
				{
					return Float.parseFloat(object.toString());
				}
				catch(Exception e)
				{}
			}
		}
		else if(Double.class.isAssignableFrom(propertyType) || double.class.isAssignableFrom(propertyType))
		{
			if(object instanceof Number)
			{
				return ((Number)object).doubleValue();
			}
			if(object instanceof String)
			{
				try
				{
					return Double.parseDouble(object.toString());
				}
				catch(Exception e)
				{}
			}
		}
		else if(BigDecimal.class.isAssignableFrom(propertyType))
		{
			if(object instanceof BigDecimal)
			{
				return object;
			}
			if(object instanceof Number)
			{
				return new BigDecimal(((Number)object).doubleValue());
			}
			if(object instanceof String)
			{
				try
				{
					return new BigDecimal(Double.parseDouble(object.toString()));
				}
				catch(Exception e)
				{}
			}
		}
		else if(Boolean.class.isAssignableFrom(propertyType) || boolean.class.isAssignableFrom(propertyType))
		{
			if(object instanceof Boolean)
			{
				return object;
			}
			if(object instanceof String)
			{
				try
				{
					return Boolean.parseBoolean(object.toString());
				}
				catch(Exception e)
				{}
			}
		}
		else if(Date.class.isAssignableFrom(propertyType))
		{
			if(object instanceof Date)
			{
				return object;
			}
			if(object instanceof String)
			{
				try
				{
					return dateformat.parse(object.toString());
				}
				catch(Exception e)
				{}
			}
		}
		logger.warn("Could not retrive value from object: " + object);
		return null;
	}

	protected abstract boolean hasNext() throws ImportException;

	protected abstract T readLine() throws ImportException;

	protected abstract void setup() throws ImportException;
}