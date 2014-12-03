package at.adaptive.components.datahandling.common;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.adaptive.components.bean.annotation.SearchField;
import at.adaptive.components.common.StringUtil;

public class ColumnDefinitionContainer
{
	private List<ColumnDefinition> columnDefinitions = new ArrayList<ColumnDefinition>();

	private Map<String, PropertyDescriptor> propertyDescriptorMap;

	private boolean useEl;
	private Class<?> beanClass;

	public ColumnDefinitionContainer(Class<?> beanClass, boolean useEl) throws IntrospectionException
	{
		this.beanClass = beanClass;
		this.useEl = useEl;
		// if(!isUseEl())
		// {
		setupBeanInfo(beanClass);
		// }
	}

	public void addColumnDefinition(ColumnDefinition columnDefinition)
	{
		if(isUseEl() && !columnDefinition.isFunctionSet() && columnDefinition.isExport())
		{
			columnDefinition.setPropertyValue(createElPropertyValue(columnDefinition.getPropertyValue()));
		}
		if(!columnDefinition.isPositionSet())
		{
			columnDefinition.setPosition(columnDefinitions.size());
		}
		columnDefinitions.add(columnDefinition);
	}

	public Class<?> getBeanClass()
	{
		return beanClass;
	}

	public ColumnDefinition getColumnDefinition(String value)
	{
		if(value == null)
		{
			return null;
		}
		for(ColumnDefinition columnDefinition : columnDefinitions)
		{
			String columnDefinitionValue = unifyValue(columnDefinition.getValue());
			value = unifyValue(value);
			if(columnDefinitionValue.equals(value))
			{
				return columnDefinition;
			}
		}
		return null;
	}

	public List<ColumnDefinition> getColumnDefinitions()
	{
		return columnDefinitions;
	}

	public Class<?> getPropertyType(String propertyName)
	{
		String lookupKey = getTrimmedPropertyValue(propertyName);
		if(!propertyDescriptorMap.containsKey(lookupKey))
		{
			return null;
		}
		return propertyDescriptorMap.get(lookupKey).getPropertyType();
	}

	public Method getReadMethod(String propertyName)
	{
		if(!propertyDescriptorMap.containsKey(propertyName))
		{
			return null;
		}
		return propertyDescriptorMap.get(propertyName).getReadMethod();
	}

	public Method getWriteMethod(String propertyName)
	{
		if(!propertyDescriptorMap.containsKey(propertyName))
		{
			return null;
		}
		return propertyDescriptorMap.get(propertyName).getWriteMethod();
	}

	public void initializeFromBean(Class<?> beanClass) throws Exception
	{
		Set<Class<?>> processedClasses = new HashSet<Class<?>>();
		updateColumnDefinitions(beanClass, null, processedClasses);
	}

	public boolean isUseEl()
	{
		return useEl;
	}

	public Iterator<ColumnDefinition> iterator()
	{
		Collections.sort(columnDefinitions, new ColumnDefinitionComparator());
		return columnDefinitions.iterator();
	}

	public void removeAll()
	{
		columnDefinitions.clear();
	}

	public void setColumnDefinitions(List<ColumnDefinition> columnDefinitions)
	{
		removeAll();
		for(ColumnDefinition columnDefinition : columnDefinitions)
		{
			addColumnDefinition(columnDefinition);
		}
	}

	public void setupBeanInfo(Class<?> beanClass) throws IntrospectionException
	{
		propertyDescriptorMap = new HashMap<String, PropertyDescriptor>();
		BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for(int i = 0; i < propertyDescriptors.length; i++)
		{
			PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
			String name = propertyDescriptor.getName();
			propertyDescriptorMap.put(name, propertyDescriptor);
		}
	}

	public void setUseEl(boolean useEl)
	{
		this.useEl = useEl;
	}

	public int size()
	{
		return columnDefinitions.size();
	}

	private String createElPropertyValue(String propertyValue)
	{
		if(propertyValue.contains("#{") && propertyValue.contains("bean."))
		{
			return propertyValue;
		}
		StringBuilder sb = new StringBuilder();
		if(propertyValue.startsWith("#{"))
		{
			propertyValue = propertyValue.substring(2);
		}
		if(propertyValue.endsWith("}"))
		{
			propertyValue = propertyValue.substring(0, propertyValue.length() - 2);
		}
		sb.append("#{bean.");
		sb.append(propertyValue);
		sb.append("}");
		return sb.toString();
	}

	private String getTrimmedPropertyValue(String elPropertyValue)
	{
		if(elPropertyValue == null)
		{
			return null;
		}
		if(elPropertyValue.startsWith("#{bean.") && elPropertyValue.endsWith("}"))
		{
			return elPropertyValue.substring(7, elPropertyValue.length() - 1);
		}
		return elPropertyValue;
	}

	private String unifyValue(String value)
	{
		value = value.trim().toLowerCase();
		value = value.replaceAll("\\n", " ");
		value = value.replaceAll("  ", " ");
		return value;
	}

	private void updateColumnDefinitions(Class<?> beanClass, String path, Set<Class<?>> processedClasses) throws Exception
	{
		processedClasses.add(beanClass);
		BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		for(int i = 0; i < propertyDescriptors.length; i++)
		{
			PropertyDescriptor propertyDescriptor = propertyDescriptors[i];
			String name = propertyDescriptor.getName();
			Method method = propertyDescriptor.getReadMethod();
			if(method.isAnnotationPresent(SearchField.class))
			{
				// get property type
				Class<?> propertyType = propertyDescriptor.getPropertyType();
				if(processedClasses.contains(propertyType))
				{
					continue;
				}
				boolean add = false;
				if(String.class.isAssignableFrom(propertyType))
				{
					add = true;
				}
				else if(Byte.class.isAssignableFrom(propertyType) || byte.class.isAssignableFrom(propertyType))
				{
					add = true;
				}
				else if(Short.class.isAssignableFrom(propertyType) || short.class.isAssignableFrom(propertyType))
				{
					add = true;
				}
				else if(Integer.class.isAssignableFrom(propertyType) || int.class.isAssignableFrom(propertyType))
				{
					add = true;
				}
				else if(Long.class.isAssignableFrom(propertyType) || long.class.isAssignableFrom(propertyType))
				{
					add = true;
				}
				else if(Float.class.isAssignableFrom(propertyType) || float.class.isAssignableFrom(propertyType))
				{
					add = true;
				}
				else if(Double.class.isAssignableFrom(propertyType) || double.class.isAssignableFrom(propertyType))
				{
					add = true;
				}
				else if(BigDecimal.class.isAssignableFrom(propertyType))
				{
					add = true;
				}
				else if(Boolean.class.isAssignableFrom(propertyType) || boolean.class.isAssignableFrom(propertyType))
				{
					add = true;
				}
				else if(Enum.class.isAssignableFrom(propertyType))
				{
					add = true;
				}
				else if(Date.class.isAssignableFrom(propertyType))
				{
					add = true;
				}
				else if(Collection.class.isAssignableFrom(propertyType))
				{
					add = true;
				}
				if(add)
				{
					addColumnDefinition(new ColumnDefinition(updatePath(path, name)));
				}
				else
				{
					updateColumnDefinitions(propertyType, updatePath(path, name), processedClasses);
				}
			}
		}
	}

	private String updatePath(String path, String name)
	{
		String value;
		if(!StringUtil.isEmpty(path))
		{
			value = path + "." + name;
		}
		else
		{
			value = name;
		}
		return value;
	}

	private class ColumnDefinitionComparator implements Comparator<ColumnDefinition>
	{
		public int compare(ColumnDefinition cd1, ColumnDefinition cd2)
		{
			return ((Integer)cd1.getPosition()).compareTo(cd2.getPosition());
		}
	}
}