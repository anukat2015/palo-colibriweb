package at.adaptive.components.datahandling.dataexport;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import at.adaptive.components.datahandling.common.BeanUtil;
import at.adaptive.components.datahandling.common.ColumnDefinition;
import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;

public abstract class AbstractExport<T> implements Export<T>
{
	protected static final Logger logger = Logger.getLogger(AbstractExport.class);

	protected ColumnDefinitionContainer columnDefinitionContainer;

	protected OutputStream out;

	protected BeanUtil<T> beanUtil;

	protected int length;

	private static String ATOM = "[^\\x00-\\x1F^\\(^\\)^\\<^\\>^\\@^\\,^\\;^\\:^\\\\^\\\"^\\.^\\[^\\]^\\s]";
	private static String DOMAIN = "(" + ATOM + "+(\\." + ATOM + "+)*";
	private static String IP_DOMAIN = "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]";
	private static String URL = "^(https?://)" + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" // user@
			+ "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP- 199.194.52.184
			+ "|" // allows either IP or domain
			+ "([0-9a-z_!~*'()-]+\\.)*" // tertiary domain(s)- www.
			+ "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." // second level domain
			+ "[a-z]{2,6})" // first level domain- .com or .museum
			+ "(:[0-9]{1,4})?" // port number- :80
			+ "((/?)|" // a slash isn't required if there is no file name
			+ "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";

	private Pattern emailPattern = Pattern.compile("^" + ATOM + "+(\\." + ATOM + "+)*@" + DOMAIN + "|" + IP_DOMAIN + ")$", java.util.regex.Pattern.CASE_INSENSITIVE);

	private Pattern urlPattern = Pattern.compile(URL);

	protected boolean isEmail(String value)
	{
		return emailPattern.matcher(value).matches();
	}

	protected boolean isUrl(String value)
	{
		return urlPattern.matcher(value).matches();
	}

	public AbstractExport(ColumnDefinitionContainer columnDefinitionContainer)
	{
		this.columnDefinitionContainer = columnDefinitionContainer;
		beanUtil = new BeanUtil<T>(columnDefinitionContainer);
	}

	public void prepareExport(OutputStream out) throws ExportException
	{
		if(columnDefinitionContainer == null)
		{
			throw new ExportException("No column definition container specified");
		}
		this.out = out;
		// setup
		setup();
		initLength();
		// write header
		for(Iterator<ColumnDefinition> iterator = columnDefinitionContainer.iterator(); iterator.hasNext();)
		{
			ColumnDefinition columnDefinition = iterator.next();
			addHeaderValue(columnDefinition);
		}
		handleLineComplete(true);
	}

	public void finishExport() throws ExportException
	{
		finish();
	}

	public void addObject(T object, boolean hasNext) throws ExportException
	{
		addLine(object);
		if(hasNext)
		{
			handleLineComplete(hasNext);
		}
	}

	public void export(List<T> objects, OutputStream out) throws ExportException
	{
		prepareExport(out);
		// write lines
		for(Iterator<T> iterator = objects.iterator(); iterator.hasNext();)
		{
			addLine(iterator.next());
			if(iterator.hasNext())
			{
				handleLineComplete(iterator.hasNext());
			}
		}
		// finish
		finish();
	}

	protected void initLength()
	{
		length = columnDefinitionContainer.size();
	}

	public void setColumnDefinitionContainer(ColumnDefinitionContainer columnDefinitionContainer)
	{
		this.columnDefinitionContainer = columnDefinitionContainer;
	}

	protected abstract void addHeaderValue(ColumnDefinition columnDefinition) throws ExportException;

	@SuppressWarnings("unchecked")
	protected void addLine(T object) throws ExportException
	{
		try
		{
			// iterate over column definitions
			for(Iterator<ColumnDefinition> iterator = columnDefinitionContainer.iterator(); iterator.hasNext();)
			{
				ColumnDefinition columnDefinition = iterator.next();
				Object value = null;
				if(columnDefinition != null && columnDefinition.isExport())
				{
					// get the value
					value = beanUtil.getValue(object, columnDefinition);
					if(columnDefinition.getConverter() != null)
					{
						value = columnDefinition.getConverter().convert(value);
					}
				}
				// write the value
				writeValue(columnDefinition, value);
			}
		}
		catch(Exception e)
		{
			throw new ExportException("Error adding line", e);
		}
	}

	protected abstract void finish() throws ExportException;

	protected abstract void handleLineComplete(boolean hasNext) throws ExportException;

	protected abstract void setup() throws ExportException;

	protected abstract void writeValue(ColumnDefinition columnDefinition, Object value) throws ExportException;
}