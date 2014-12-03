package at.adaptive.components.bean.annotation;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;

/**
 * This class may be used to load classes for the specified annotation type by using a configurable class path
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class AnnotationLoader
{
	private static final Logger logger = Logger.getLogger(AnnotationLoader.class);

	private List<URL> urls;
	private String[] classAnnotationNames;

	/**
	 * Creates a new instance of AnnotationLoader by using a default class path ({@link ClasspathUrlFinder#findClassPaths()})
	 * 
	 * @param classAnnotationNames
	 *            the names of the class-level annotations
	 */
	public AnnotationLoader(String[] classAnnotationNames)
	{
		this(classAnnotationNames, ClasspathUrlFinder.findClassPaths());
	}

	/**
	 * Creates a new instance of AnnotationLoader
	 * 
	 * @param classAnnotationNames
	 *            the names of the class-level annotations
	 * @param urls
	 *            the urls to scan
	 */
	public AnnotationLoader(String[] classAnnotationNames, URL[] urls)
	{
		this.classAnnotationNames = classAnnotationNames;
		this.urls = Arrays.asList(urls);
	}

	/**
	 * Adds class path urls to the list of class paths to scan
	 * 
	 * @param urls
	 *            the urls to scan
	 */
	public void addClasspathUrls(URL[] urls)
	{
		this.urls.addAll(Arrays.asList(urls));
	}

	/**
	 * Returns a list of classes which are annotated with the specified annotations
	 * 
	 * @return a list of classes which are annotated with the specified annotations
	 * @throws Exception
	 *             on error
	 */
	public List<Class<?>> getAnnotatedClasses() throws Exception
	{
		List<Class<?>> classes = new ArrayList<Class<?>>();
		AnnotationDB db = new AnnotationDB();
		db.scanArchives(urls.toArray(new URL[urls.size()]));
		for(String classAnnotationName : classAnnotationNames)
		{
			Set<String> classNames = db.getAnnotationIndex().get(classAnnotationName);
			if(classNames == null)
			{
				continue;
			}
			for(String className : classNames)
			{
				classes.add(Class.forName(className));
			}
		}
		return classes;
	}

	/**
	 * Returns a list of annotation infos for the specified annotation class
	 * 
	 * @param annotationClass
	 *            the annotation class
	 * @return a list of annotation infos for the specified annotation class
	 * @throws Exception
	 *             on error
	 */
	public <T extends Annotation> List<AnnotationInfo<T>> getAnnotations(Class<T> annotationClass) throws Exception
	{
		logger.info("Loading annotations: " + annotationClass.getName());
		AnnotationDB db = new AnnotationDB();
		db.scanArchives(urls.toArray(new URL[urls.size()]));
		Set<ClassInfo> entityClasses = new HashSet<ClassInfo>();
		for(String classAnnotationName : classAnnotationNames)
		{
			Set<String> classes = db.getAnnotationIndex().get(classAnnotationName);
			if(classes == null)
			{
				continue;
			}
			for(String className : classes)
			{
				entityClasses.add(new ClassInfo(className, classAnnotationName));
			}
		}
		List<AnnotationInfo<T>> annotations = new ArrayList<AnnotationInfo<T>>();
		if(entityClasses != null)
		{
			for(Iterator<ClassInfo> iterator = entityClasses.iterator(); iterator.hasNext();)
			{
				ClassInfo classInfo = iterator.next();
				String entityClass = classInfo.getClassName();
				String classAnnotationName = classInfo.getAnnotationName();
				Class<?> clazz = Class.forName(entityClass);
				BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
				PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
				for(PropertyDescriptor propertyDescriptor : propertyDescriptors)
				{
					Method readMethod = propertyDescriptor.getReadMethod();
					if(readMethod != null)
					{
						T annotation = readMethod.getAnnotation(annotationClass);
						if(annotation != null)
						{
							Class<?> type = propertyDescriptor.getPropertyType();
							if(Collection.class.isAssignableFrom(type))
							{
								Map<String, Field> fieldMap = new HashMap<String, Field>();
								Class<?> oClazz = clazz;
								while(oClazz != null)
								{
									Field[] fields = oClazz.getDeclaredFields();
									for(Field field : fields)
									{
										if(!fieldMap.containsKey(field.getName()))
										{
											fieldMap.put(field.getName(), field);
										}
									}
									oClazz = oClazz.getSuperclass();
								}
								Field f = fieldMap.get(propertyDescriptor.getName());
								Type t = f.getGenericType();
								if(t instanceof ParameterizedType)
								{
									Object obj = ((ParameterizedType)t).getActualTypeArguments()[0];
									while(obj instanceof ParameterizedType)
									{
										obj = ((ParameterizedType)obj).getRawType();
									}
									type = (Class<?>)obj;
								}
							}
							logger.info("Annotation found on property \"" + propertyDescriptor.getName() + "\", on class: " + clazz.getName());
							AnnotationInfo<T> annotationInfo = new AnnotationInfo<T>(clazz, propertyDescriptor.getName(), type, annotation);
							annotationInfo.setClassAnnotationName(classAnnotationName);
							annotations.add(annotationInfo);
						}
					}
				}
			}
		}
		return annotations;
	}

	private class ClassInfo
	{
		private String className;
		private String annotationName;

		public ClassInfo(String className, String annotationName)
		{
			super();
			this.className = className;
			this.annotationName = annotationName;
		}

		public String getAnnotationName()
		{
			return annotationName;
		}

		public String getClassName()
		{
			return className;
		}
	}
}
