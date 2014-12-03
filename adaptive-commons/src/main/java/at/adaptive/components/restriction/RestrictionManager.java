package at.adaptive.components.restriction;

import static org.jboss.seam.ScopeType.APPLICATION;

import java.io.File;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.ServletLifecycle;

import at.adaptive.components.bean.annotation.AnnotationInfo;
import at.adaptive.components.bean.annotation.AnnotationLoader;
import at.adaptive.components.bean.annotation.SearchField;
import at.adaptive.components.common.CollectionUtil;
import at.adaptive.components.common.StringUtil;
import at.adaptive.components.session.BaseComponent;

/**
 * Manager class for handling restrictions
 * 
 * @author Bernhard Hablesreiter
 * 
 */
@Name("at.adaptive.components.restriction.restrictionManager")
@Scope(APPLICATION)
@Install(precedence = Install.FRAMEWORK)
@BypassInterceptors
@Startup
public class RestrictionManager extends BaseComponent<RestrictionManagerConfig>
{
	protected static final String MESSAGE_KEY_PREFIX = "at.adaptive.components.restriction.restrictionManager";

	private Map<Class<?>, Map<String, AnnotationInfo<SearchField>>> searchFieldMap;
	private Map<Class<?>, Map<String, AnnotationInfo<Field>>> fieldMap;
	private Map<Class<?>, Map<String, AnnotationInfo<Id>>> idMap;

	private static final Logger logger = Logger.getLogger(RestrictionManager.class);

	/**
	 * Returns the instance of the restriction manager
	 * 
	 * @return the instance of the restriction manager
	 */
	public static RestrictionManager instance()
	{
		if(!Contexts.isApplicationContextActive())
		{
			throw new IllegalStateException("No active application context");
		}
		RestrictionManager instance = (RestrictionManager)Component.getInstance(RestrictionManager.class, ScopeType.APPLICATION);
		if(instance == null)
		{
			throw new IllegalStateException("No RestrictionManager could be created");
		}
		return instance;
	}

	@Create
	public void create()
	{
		logger.info("Creating RestrictionManager");
		loadRestrictions();
	}

	/**
	 * Creates a map of restrictions (holding property names and restrictions) for a specified class, adding the specified order listener to each restriction
	 * 
	 * @param clazz
	 *            the class
	 * @return the created restrictions on success or <code>null</code> on error
	 */
	public Map<String, Restriction<?>> createRestrictions(Class<?> clazz)
	{
		try
		{
			Map<String, Restriction<?>> restrictionMap = new HashMap<String, Restriction<?>>();
			Map<String, AnnotationInfo<SearchField>> searchFields = searchFieldMap.get(clazz);
			Map<String, AnnotationInfo<Field>> fields = fieldMap.get(clazz);
			Map<String, AnnotationInfo<Id>> ids = idMap.get(clazz);
			if(searchFields != null)
			{
				for(Iterator<String> iterator = searchFields.keySet().iterator(); iterator.hasNext();)
				{
					String name = iterator.next();
					AnnotationInfo<SearchField> annotationInfo = searchFields.get(name);
					List<ClassHistoryEntry> classHistory = new ArrayList<ClassHistoryEntry>();
					classHistory.add(new ClassHistoryEntry(annotationInfo.getDeclaringClass(), annotationInfo.getName()));
					List<Restriction<?>> restrictions = createRestrictions(new ArrayList<Restriction<?>>(), annotationInfo, null, annotationInfo.getDeclaringClass(), classHistory, fields, ids);
					if(restrictions != null && restrictions.size() > 0)
					{
						for(Restriction<?> restriction : restrictions)
						{
							restrictionMap.put(restriction.getPropertyName(), restriction);
						}
					}
					else
					{
						// logger.warn("No restrictions found for annotated entity-relation: " + annotationInfo);
					}
				}
				return restrictionMap;
			}
		}
		catch(Exception e)
		{
			logger.error("Error creating restrictions for class: " + clazz, e);
		}
		return null;
	}

	/**
	 * Returns a map (holding property names and annotation infos) of annotations for a specified class
	 * 
	 * @param clazz
	 *            the class
	 * @return the map of annotations for the specified class on success or <code>null</code> if no annotations could be found for the specified class
	 */
	public Map<String, AnnotationInfo<SearchField>> getAnnotations(Class<?> clazz)
	{
		return searchFieldMap.get(clazz);
	}

	private Restriction<?> createRestriction(AnnotationInfo<SearchField> annotationInfo, String overrideName, Map<String, AnnotationInfo<Field>> fields, Map<String, AnnotationInfo<Id>> ids, List<ClassHistoryEntry> classHistory) throws Exception
	{
		Class<?> type = annotationInfo.getType();
		String propertyName = annotationInfo.getAnnotation().propertyName();
		if(StringUtil.isEmpty(propertyName))
		{
			propertyName = annotationInfo.getName();
		}
		else
		{
			type = getPropertyClass(annotationInfo);
		}
		if(overrideName != null)
		{
			propertyName = overrideName + "." + propertyName;
		}
		boolean orderEnabled = annotationInfo.getAnnotation().orderEnabled();
		Class<?> clazz = null;
		if(!annotationInfo.getAnnotation().impl().equals(void.class))
		{
			clazz = annotationInfo.getAnnotation().impl();
		}
		else
		{
			if(String.class.isAssignableFrom(type))
			{
				clazz = StringRestriction.class;
			}
			else if(Byte.class.isAssignableFrom(type) || byte.class.isAssignableFrom(type))
			{
				clazz = ByteRestriction.class;
			}
			else if(Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type))
			{
				clazz = ShortRestriction.class;
			}
			else if(Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type))
			{
				clazz = IntegerRestriction.class;
			}
			else if(Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type))
			{
				clazz = LongRestriction.class;
			}
			else if(Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type))
			{
				clazz = FloatRestriction.class;
			}
			else if(Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type))
			{
				clazz = DoubleRestriction.class;
			}
			else if(BigDecimal.class.isAssignableFrom(type))
			{
				clazz = BigDecimalRestriction.class;
			}
			else if(Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type))
			{
				clazz = BooleanRestriction.class;
			}
			else if(Enum.class.isAssignableFrom(type))
			{
				clazz = EnumRestriction.class;
			}
			else if(Date.class.isAssignableFrom(type))
			{
				clazz = DateRestriction.class;
			}
		}
		if(clazz != null)
		{
			try
			{
				Restriction<?> restriction = (Restriction<?>)clazz.newInstance();
				restriction.setPropertyName(propertyName);
				restriction.setPropertyClass(type);
				restriction.setOrderEnabled(orderEnabled);
				// restriction.setEntityManager(entityManager);
				if(annotationInfo.getDeclaringClass().isAnnotationPresent(Embeddable.class) || isSuperClassAnnotated(classHistory, annotationInfo.getDeclaringClass(), Embeddable.class))
				{
					restriction.setEmbeddedType(true);
					// get the annotated class
					for(int i = classHistory.size() - 1; i >= 0; i--)
					{
						ClassHistoryEntry classHistoryEntry = classHistory.get(i);
						// check for annotation
						if(classHistoryEntry.getClazz().isAnnotationPresent(Embeddable.class))
						{
							// get the property name
							String embeddedPropertyName = classHistoryEntry.getPropertyName();
							if((classHistory.size() - 1) >= (i + 1))
							{
								// get the next class history entry
								ClassHistoryEntry nextClassHistoryEntry = classHistory.get(i + 1);
								// set the embedded property name
								embeddedPropertyName = embeddedPropertyName + "." + nextClassHistoryEntry.getPropertyName();
								restriction.setEmbeddedPropertyName(embeddedPropertyName);
							}
							break;
						}
					}
				}
				String name = annotationInfo.getName();
				if(ids != null && ids.size() == 1)
				{
					// TODO
					AnnotationInfo<Id> idInfo = ids.get(ids.keySet().iterator().next());
					String idPropertyName = idInfo.getName();
					String fulltextIdPropertyName;
					Class<?> searchClass;
					if(annotationInfo.getDeclaringClass().isAnnotationPresent(Indexed.class))
					{
						searchClass = annotationInfo.getDeclaringClass();
						Map<String, AnnotationInfo<Id>> fulltextIds = idMap.get(searchClass);
						if(fulltextIds != null && fulltextIds.size() == 1)
						{
							AnnotationInfo<Id> fulltextIdInfo = fulltextIds.get(fulltextIds.keySet().iterator().next());
							fulltextIdPropertyName = fulltextIdInfo.getName();
						}
						else
						{
							fulltextIdPropertyName = idPropertyName;
						}
					}
					else
					{
						searchClass = idInfo.getDeclaringClass();
						fulltextIdPropertyName = idPropertyName;
					}
					restriction.setIdPropertyName(idPropertyName);
					restriction.setFulltextIdPropertyName(fulltextIdPropertyName);
					restriction.setSearchClass(searchClass);
				}
				if(fields != null && ids != null && fields.containsKey(name) && ids.size() == 1)
				{
					AnnotationInfo<Field> fieldAnnotationInfo = fields.get(name);
					String fulltextFieldName = fieldAnnotationInfo.getAnnotation().name();
					if(StringUtil.isEmpty(fulltextFieldName))
					{
						fulltextFieldName = fieldAnnotationInfo.getName();
					}
					if(restriction.isEmbeddedType() && !annotationInfo.getDeclaringClass().isAnnotationPresent(Indexed.class))
					{
						int index = propertyName.lastIndexOf(".");
						if(index != -1)
						{
							fulltextFieldName = propertyName.substring(0, index + 1) + fulltextFieldName;
						}
					}
					restriction.setFulltext(true);
					restriction.setFulltextFieldName(fulltextFieldName);
				}
				if(CollectionUtil.isNotEmpty(annotationInfo.getAnnotation().groups()))
				{
					restriction.setGroups(annotationInfo.getAnnotation().groups());
				}
				return restriction;
			}
			catch(Exception e)
			{
				logger.error("Error creating restriction from class: " + clazz, e);
			}
		}
		return null;
	}

	private List<Restriction<?>> createRestrictions(List<Restriction<?>> restrictions, AnnotationInfo<SearchField> annotationInfo, String overrideName, Class<?> baseClass, List<ClassHistoryEntry> classHistory, Map<String, AnnotationInfo<Field>> fields, Map<String, AnnotationInfo<Id>> ids) throws Exception
	{
		Class<?> clazz = annotationInfo.getType();
		ClassHistoryEntry classHistoryEntry = new ClassHistoryEntry(clazz, annotationInfo.getName());
		if(classHistory.contains(classHistoryEntry))
		{
			// prevent cycling
			return restrictions;
		}
		// check if the type is a referenced entity class or embedded type
		if(searchFieldMap.containsKey(clazz))
		{
			if(!classHistory.contains(classHistoryEntry))
			{
				classHistory.add(classHistoryEntry);
			}
			ids = idMap.get(clazz);
			fields = fieldMap.get(clazz);
			// check for embedded type
			if(clazz.isAnnotationPresent(Embeddable.class) || isSuperClassAnnotated(classHistory, annotationInfo.getDeclaringClass(), Embeddable.class))
			{
				if(ids == null || !ids.containsKey(annotationInfo.getName()))
				{
					ids = idMap.get(baseClass);
				}
			}
			if(overrideName == null)
			{
				overrideName = annotationInfo.getName();
			}
			else
			{
				overrideName = overrideName + "." + annotationInfo.getName();
			}
			// get the restriction map
			Map<String, AnnotationInfo<SearchField>> restrictionMap = searchFieldMap.get(clazz);
			for(Iterator<String> iterator = restrictionMap.keySet().iterator(); iterator.hasNext();)
			{
				AnnotationInfo<SearchField> currentAnnotationInfo = restrictionMap.get(iterator.next());
				createRestrictions(restrictions, currentAnnotationInfo, overrideName, baseClass, classHistory, fields, ids);
			}
		}
		else
		{
			Restriction<?> restriction = createRestriction(annotationInfo, overrideName, fields, ids, classHistory);
			if(restriction != null)
			{
				restrictions.add(restriction);
			}
			ids = idMap.get(baseClass);
			fields = fieldMap.get(baseClass);
		}
		return restrictions;
	}

	private boolean isSuperClassAnnotated(List<ClassHistoryEntry> classHistory, Class<?> declaringClass, Class<? extends Annotation> annotationClass)
	{
		int pos = findClassPositionInClassHistory(classHistory, declaringClass);
		for(int i = pos; i >= 0; i--)
		{
			ClassHistoryEntry classHistoryEntry = classHistory.get(i);
			if(classHistoryEntry.getClazz().isAnnotationPresent(annotationClass))
			{
				return true;
			}
		}
		return false;
	}

	private int findClassPositionInClassHistory(List<ClassHistoryEntry> classHistory, Class<?> declaringClass)
	{
		for(int i = 0; i < classHistory.size(); i++)
		{
			if(classHistory.get(i).getClazz().equals(declaringClass))
			{
				return i;
			}
		}
		return classHistory.size();
	}

	/**
	 * Returns the class for the property name of a specified annotation info.<br>
	 * If an association path is specified (e.g. referencedEntity.property), loops through the path and returns the property's class
	 * 
	 * @param annotationInfo
	 *            the annotation info
	 * @return the clazz for the property name of the specified annotation info
	 */
	private Class<?> getPropertyClass(AnnotationInfo<SearchField> annotationInfo)
	{
		String propertyName = annotationInfo.getAnnotation().propertyName();
		if(propertyName.indexOf('.') > 0)
		{
			try
			{
				Class<?> clazz = annotationInfo.getDeclaringClass();
				String currentPropertyName = null;
				while(propertyName.indexOf('.') > 0)
				{
					currentPropertyName = propertyName.substring(0, propertyName.indexOf('.'));
					clazz = clazz.getDeclaredField(currentPropertyName).getType();
					propertyName = propertyName.substring(propertyName.indexOf('.') + 1);
				}
				clazz = clazz.getDeclaredField(propertyName).getType();
				return clazz;
			}
			catch(Exception e)
			{
				logger.error("Error retrieving type for property name via reflection: " + propertyName, e);
				return null;
			}
		}
		else
		{
			return annotationInfo.getDeclaringClass();
		}
	}

	/**
	 * Loads the restrictions
	 */
	private void loadRestrictions()
	{
		try
		{
			logger.info("Loading restrictions");
			searchFieldMap = new HashMap<Class<?>, Map<String, AnnotationInfo<SearchField>>>();
			fieldMap = new HashMap<Class<?>, Map<String, AnnotationInfo<Field>>>();
			idMap = new HashMap<Class<?>, Map<String, AnnotationInfo<Id>>>();
			// get the ear path
			URL[] urls = getClassPathUrls();
			AnnotationLoader annotationLoader = new AnnotationLoader(new String[]{Entity.class.getName(), Embeddable.class.getName()}, urls);
			List<AnnotationInfo<SearchField>> searchFieldInfos = annotationLoader.getAnnotations(SearchField.class);
			for(AnnotationInfo<SearchField> annotationInfo : searchFieldInfos)
			{
				Map<String, AnnotationInfo<SearchField>> annotations = searchFieldMap.get(annotationInfo.getDeclaringClass());
				if(annotations == null)
				{
					annotations = new HashMap<String, AnnotationInfo<SearchField>>();
				}
				annotations.put(annotationInfo.getName(), annotationInfo);
				searchFieldMap.put(annotationInfo.getDeclaringClass(), annotations);
			}
			List<AnnotationInfo<Field>> fieldInfos = annotationLoader.getAnnotations(Field.class);
			for(AnnotationInfo<Field> annotationInfo : fieldInfos)
			{
				Map<String, AnnotationInfo<Field>> annotations = fieldMap.get(annotationInfo.getDeclaringClass());
				if(annotations == null)
				{
					annotations = new HashMap<String, AnnotationInfo<Field>>();
				}
				annotations.put(annotationInfo.getName(), annotationInfo);
				fieldMap.put(annotationInfo.getDeclaringClass(), annotations);
			}
			List<AnnotationInfo<Id>> idInfos = annotationLoader.getAnnotations(Id.class);
			for(AnnotationInfo<Id> annotationInfo : idInfos)
			{
				Map<String, AnnotationInfo<Id>> annotations = idMap.get(annotationInfo.getDeclaringClass());
				if(annotations == null)
				{
					annotations = new HashMap<String, AnnotationInfo<Id>>();
				}
				annotations.put(annotationInfo.getName(), annotationInfo);
				idMap.put(annotationInfo.getDeclaringClass(), annotations);
			}
		}
		catch(Exception e)
		{
			logger.error("Error loading restrictions", e);
		}
	}

	protected URL[] getClassPathUrls()
	{
		try
		{
			List<URL> urls = new ArrayList<URL>();
			ServletContext servletContext = ServletLifecycle.getServletContext();
			// URL baseUrl = propFile.getParentFile().toURI().toURL();
			File baseDir = new File(servletContext.getRealPath("/"));
			if(getConfig() != null)
			{
				List<String> configuredUrls = getConfig().getUrls();
				if(CollectionUtil.isNotEmpty(configuredUrls))
				{
					for(String configuredUrl : configuredUrls)
					{
						File file = new File(baseDir, configuredUrl);
						urls.add(file.toURI().toURL());
					}
				}
			}
			return urls.toArray(new URL[urls.size()]);
		}
		catch(Exception e)
		{
			logger.warn("Restriction Manager could not load classpath urls. Using defaults");
			return new URL[0];
		}
	}

	private class ClassHistoryEntry
	{
		private Class<?> clazz;
		private String propertyName;

		public ClassHistoryEntry(Class<?> clazz, String propertyName)
		{
			super();
			this.clazz = clazz;
			this.propertyName = propertyName;
		}

		public Class<?> getClazz()
		{
			return clazz;
		}

		public String getPropertyName()
		{
			return propertyName;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
			result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj) return true;
			if(obj == null) return false;
			if(getClass() != obj.getClass()) return false;
			ClassHistoryEntry other = (ClassHistoryEntry)obj;
			if(!getOuterType().equals(other.getOuterType())) return false;
			if(clazz == null)
			{
				if(other.clazz != null) return false;
			}
			else if(!clazz.equals(other.clazz)) return false;
			if(propertyName == null)
			{
				if(other.propertyName != null) return false;
			}
			else if(!propertyName.equals(other.propertyName)) return false;
			return true;
		}

		private RestrictionManager getOuterType()
		{
			return RestrictionManager.this;
		}

	}

	@Override
	protected String getMessageKeyPrefix()
	{
		return MESSAGE_KEY_PREFIX;
	}
}