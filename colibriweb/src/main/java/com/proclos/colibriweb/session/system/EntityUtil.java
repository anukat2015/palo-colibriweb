package com.proclos.colibriweb.session.system;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.proclos.colibriweb.entity.BaseEntity;

public class EntityUtil
{

	private static final EntityUtil instance = new EntityUtil();
	private static final Log log = LogFactory.getLog(EntityUtil.class);

	public static EntityUtil getInstance()
	{
		return instance;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T extends BaseEntity> T cloneEntity(T entity, Set<String> cloneFilter, Set<String> excludeFilter)
	{
		try
		{
			Object result = entity.getClass().newInstance();
			for(Field field : getAllFields(entity.getClass()))
			{
				try
				{
					if(!excludeFilter.contains(field.getName()))
					{

						Class<?> fieldClazz = field.getType();
						String prefix = (fieldClazz.isPrimitive() && fieldClazz.isAssignableFrom(Boolean.class)) ? "is" : "get";
						String getMethodName = prefix + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
						Method getMethod = null;
						try
						{
							getMethod = entity.getClass().getMethod(getMethodName);
						}
						catch(Exception e)
						{ // no getter found. just continue
							continue;
						}
						Object fieldObject = getMethod.invoke(entity);
						if(fieldObject instanceof Collection && cloneFilter.contains(field.getName()))
						{
							Collection source = (Collection)fieldObject;
							Collection target = null;
							if(fieldClazz.isAssignableFrom(List.class)) target = new ArrayList();
							if(fieldClazz.isAssignableFrom(Set.class)) target = new HashSet();
							if(source != null) for(Object o : source)
							{
								if(o instanceof BaseEntity) target.add(cloneEntity((BaseEntity)o, cloneFilter, excludeFilter));
							}
							fieldObject = target;
						}
						else if(BaseEntity.class.isAssignableFrom(fieldClazz) && cloneFilter.contains(field.getName()))
						{
							fieldObject = cloneEntity((BaseEntity)fieldObject, cloneFilter, excludeFilter);
						}
						String setMethodName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
						Method setMethod = entity.getClass().getMethod(setMethodName, new Class<?>[]{fieldClazz});
						setMethod.invoke(result, fieldObject);
					}
				}
				catch(Exception e)
				{
					log.error("Cloning error on entity of class " + entity.getClass().getCanonicalName() + " on Field " + field.getName() + ": " + e.getMessage());
				}
			}
			return (T)result;
		}
		catch(Exception e)
		{
			log.error("Cloning error. Cannot instantiate base class: " + e.getMessage());
		}
		return null;
	}

	public <T extends BaseEntity> void setField(T entity, String fieldName, Object value, Set<String> filter)
	{
		for(Field field : getAllFields(entity.getClass()))
		{
			try
			{
				Class<?> fieldClazz = field.getType();
				if(field.getName().equals(fieldName))
				{
					String setMethodName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
					Method setMethod = entity.getClass().getMethod(setMethodName, new Class<?>[]{fieldClazz});
					setMethod.invoke(entity, value);
				}
				else if((BaseEntity.class.isAssignableFrom(fieldClazz) || Collection.class.isAssignableFrom(fieldClazz)) && filter.contains(field.getName()))
				{
					String getMethodName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
					Method getMethod = entity.getClass().getMethod(getMethodName);
					Object fieldObject = getMethod.invoke(entity);
					if(fieldObject instanceof Collection)
					{
						Collection<?> collection = (Collection<?>)fieldObject;
						for(Object o : collection)
						{
							if(o instanceof BaseEntity) setField((BaseEntity)o, fieldName, value, filter);
						}
					}
					else
					{
						setField((BaseEntity)fieldObject, fieldName, value, filter);
					}
				}
			}
			catch(Exception e)
			{
				log.error("Field set error on entity of class " + entity.getClass().getCanonicalName() + " on Field " + field.getName() + ": " + e.getMessage());
			}
		}
	}

	public <T extends BaseEntity> Collection<?> getCollection(T entity, String fieldName)
	{
		Field field = getField(entity.getClass(), fieldName);
		try
		{
			String getMethodName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
			Method getMethod = entity.getClass().getMethod(getMethodName);
			Object fieldObject = getMethod.invoke(entity);
			if(fieldObject instanceof Collection)
			{
				Collection<?> collection = (Collection<?>)fieldObject;
				return collection;
			}
			return null;
		}
		catch(Exception e)
		{
			log.error("Collection get error on entity of class " + entity.getClass().getCanonicalName() + " on Field " + field.getName() + ": " + e.getMessage());
		}
		return null;
	}

	public List<Field> getAllFields(Class<?> clazz)
	{
		List<Field> result = new ArrayList<Field>();
		for(Field f : clazz.getDeclaredFields())
		{
			result.add(f);
		}
		Class<?> c = clazz;
		while(c != null && !c.equals(BaseEntity.class))
		{
			c = c.getSuperclass();
			if(c != null)
			{
				for(Field f : c.getDeclaredFields())
				{
					result.add(f);
				}
			}
		}
		return result;
	}

	private Field searchField(Class<?> clazz, String name)
	{
		Field f = null;
		Class<?> c = clazz;
		while(f == null && c != null)
		{
			try
			{
				f = c.getDeclaredField(name);
			}
			catch(Exception e)
			{};
			c = c.getSuperclass();
		}
		return f;
	}

	public Field getField(Class<?> clazz, String fieldName)
	{
		if(fieldName.contains("."))
		{
			String pathElement = fieldName.substring(0, fieldName.indexOf("."));
			String path = fieldName.substring(fieldName.indexOf(".") + 1);
			Field f = getField(clazz, pathElement);
			if(f != null)
			{
				if(f.getGenericType() instanceof ParameterizedType)
				{
					ParameterizedType t = (ParameterizedType)f.getGenericType();
					return getField((Class<?>)t.getActualTypeArguments()[0], path);
				}
				else
				{
					return getField(f.getType(), path);
				}
			}
			else
			{
				log.error("Class " + clazz.getCanonicalName() + " does not have field " + pathElement);
				return null;
			}
		}
		return searchField(clazz, fieldName);
	}

}
