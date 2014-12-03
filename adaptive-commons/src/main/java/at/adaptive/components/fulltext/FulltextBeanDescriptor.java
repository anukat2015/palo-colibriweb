package at.adaptive.components.fulltext;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import at.adaptive.components.common.StringUtil;

public class FulltextBeanDescriptor
{
	private String className;
	private Class<?> beanClass;
	private Map<String, String> propertyMap = new HashMap<String, String>();
	private List<String> properties = new ArrayList<String>();
	private String scoreProperty;

	public FulltextBeanDescriptor(String className) throws Exception
	{
		super();
		this.className = className;
		beanClass = Class.forName(className);
		BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
		boolean scorePropertySet = false;
		for(PropertyDescriptor propertyDescriptor : propertyDescriptors)
		{
			FulltextDataField fulltextSearchField = propertyDescriptor.getReadMethod().getAnnotation(FulltextDataField.class);
			if(fulltextSearchField != null)
			{
				String mapping = fulltextSearchField.mapping();
				String propertyName = propertyDescriptor.getName();
				if(StringUtil.isEmpty(mapping))
				{
					mapping = propertyName;
				}
				propertyMap.put(mapping, propertyName);
				properties.add(propertyName);
			}
			if(!scorePropertySet)
			{
				FulltextScoreField fulltextScoreField = propertyDescriptor.getReadMethod().getAnnotation(FulltextScoreField.class);
				if(fulltextScoreField != null)
				{
					scoreProperty = propertyDescriptor.getName();
					scorePropertySet = true;
				}
			}
		}
	}

	public Object createBeanInstance() throws Exception
	{
		return beanClass.newInstance();
	}

	public String getClassName()
	{
		return className;
	}

	public List<String> getProperties()
	{
		return properties;
	}

	public String getProperty(String field)
	{
		return propertyMap.get(field);
	}

	public boolean isScoreAnnotationPresent()
	{
		return scoreProperty != null;
	}

	public void setBeanProperty(Object bean, String field, String value) throws Exception
	{
		BeanUtils.setProperty(bean, propertyMap.get(field), value);
	}

	public void setScore(Object bean, float score) throws Exception
	{
		if(!isScoreAnnotationPresent())
		{
			return;
		}
		BeanUtils.setProperty(bean, scoreProperty, score);
	}
}
