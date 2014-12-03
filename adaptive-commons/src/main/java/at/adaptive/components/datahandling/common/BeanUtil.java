package at.adaptive.components.datahandling.common;

import java.lang.reflect.Method;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.jboss.el.ExpressionFactoryImpl;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;

import at.adaptive.components.bean.el.SimpleContext;
import at.adaptive.components.datahandling.dataexport.ExportException;
import at.adaptive.components.datahandling.dataexport.function.Function;
import at.adaptive.components.datahandling.dataimport.ImportException;

public class BeanUtil<T>
{
	private ColumnDefinitionContainer columnDefinitionContainer;
	private SimpleContext elContext;
	private static final String BIND_VALUE = "bean";
	private boolean useSeamContext = false;
	private ExpressionFactory expressionFactory;
	private boolean suppressErrors = false;

	public BeanUtil(ColumnDefinitionContainer columnDefinitionContainer)
	{
		this.columnDefinitionContainer = columnDefinitionContainer;
		useSeamContext = Contexts.getEventContext() != null;
		if(!useSeamContext)
		{
			if(columnDefinitionContainer.isUseEl())
			{
				expressionFactory = new ExpressionFactoryImpl();
				elContext = new SimpleContext();
			}
		}
	}

	public Object getValue(T object, ColumnDefinition columnDefinition) throws ExportException
	{
		try
		{
			// check for function
			if(columnDefinition.isFunctionSet())
			{
				Function function = columnDefinition.getFunction();
				return function.execute(object);
			}
			else
			{
				// get property value
				String propertyValue = columnDefinition.getPropertyValue();
				if(propertyValue == null)
				{
					// fallback to value
					propertyValue = columnDefinition.getValue();
				}
				if(columnDefinitionContainer.isUseEl())
				{
					// use el
					return getValueFromEL(propertyValue, object);
				}
				else
				{
					// use method
					Method readMethod = columnDefinitionContainer.getReadMethod(propertyValue);
					if(readMethod == null)
					{
						throw new ExportException("Property not found: " + propertyValue);
					}
					Object value = readMethod.invoke(object);
					return value;
				}
			}
		}
		catch(Exception e)
		{
			throw new ExportException("Error retrieving value from bean", e);
		}
	}

	public boolean isSuppressErrors()
	{
		return suppressErrors;
	}

	public void setSuppressErrors(boolean suppressErrors)
	{
		this.suppressErrors = suppressErrors;
	}

	public void setValue(T object, ColumnDefinition columnDefinition, Object value) throws ImportException
	{
		try
		{
			// get property value
			String propertyValue = columnDefinition.getPropertyValue();
			if(propertyValue == null)
			{
				// fallback to value
				propertyValue = columnDefinition.getValue();
			}
			if(columnDefinitionContainer.isUseEl())
			{
				// use el
				setValue(propertyValue, object, value);
				return;
			}
			else
			{
				// use method
				Method writeMethod = columnDefinitionContainer.getWriteMethod(propertyValue);
				if(writeMethod == null)
				{
					throw new ImportException("Property not found: " + propertyValue);
				}
				writeMethod.invoke(object);
				return;
			}
		}
		catch(Exception e)
		{
			if(!suppressErrors)
			{
				throw new ImportException("Error setting value on bean", e);
			}
		}
	}

	private Object getValueFromEL(String propertyValue, Object object)
	{
		if(useSeamContext)
		{
			Contexts.getEventContext().set(BIND_VALUE, object);
			return Expressions.instance().createValueExpression(propertyValue).getValue();
		}
		else
		{
			elContext.bind(BIND_VALUE, object);
			ValueExpression ve = expressionFactory.createValueExpression(elContext, propertyValue, Object.class);
			return ve.getValue(elContext);
		}
	}

	// public Class<?> getExpectedType(String propertyValue, Object object)
	// {
	// if(useSeamContext)
	// {
	// Contexts.getEventContext().set(BIND_VALUE, object);
	// return Expressions.instance().createValueExpression(propertyValue).getType().getClass();
	// }
	// else
	// {
	// elContext.bind(BIND_VALUE, object);
	// ValueExpression ve = expressionFactory.createValueExpression(elContext, propertyValue, Object.class);
	// return ve.getType(elContext);
	// }
	// }

	private void setValue(String propertyValue, Object object, Object value)
	{
		if(useSeamContext)
		{
			Contexts.getEventContext().set(BIND_VALUE, object);
			Expressions.instance().createValueExpression(propertyValue).setValue(value);
		}
		else
		{
			elContext.bind(BIND_VALUE, object);
			ValueExpression ve = expressionFactory.createValueExpression(elContext, propertyValue, Object.class);
			ve.setValue(elContext, value);
		}
	}
}
