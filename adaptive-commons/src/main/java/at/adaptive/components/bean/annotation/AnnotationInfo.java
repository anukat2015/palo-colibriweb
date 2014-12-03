package at.adaptive.components.bean.annotation;

import java.lang.annotation.Annotation;

/**
 * Wrapper class for an annotation
 * 
 * @author Bernhard Hablesreiter
 * 
 * @param <T>
 *            The type of the annotation
 */
public class AnnotationInfo<T extends Annotation>
{
	private T annotation;
	private String classAnnotationName;
	private Class<?> declaringClass;
	private String name;
	private Class<?> type;

	/**
	 * Creates a new instance of AnnotationInfo
	 * 
	 * @param declaringClass
	 *            the declaring class
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param annotation
	 *            the annotation
	 */
	public AnnotationInfo(Class<?> declaringClass, String name, Class<?> type, T annotation)
	{
		super();
		this.declaringClass = declaringClass;
		this.name = name;
		this.type = type;
		this.annotation = annotation;
	}

	public T getAnnotation()
	{
		return annotation;
	}

	public String getClassAnnotationName()
	{
		return classAnnotationName;
	}

	public Class<?> getDeclaringClass()
	{
		return declaringClass;
	}

	public String getName()
	{
		return name;
	}

	public Class<?> getType()
	{
		return type;
	}

	public void setClassAnnotationName(String classAnnotationName)
	{
		this.classAnnotationName = classAnnotationName;
	}

	@Override
	public String toString()
	{
		return new StringBuilder().append("name=").append(name).append(", declaringClass=").append(declaringClass).append(", type=").append(type).append(", annotation=").append(annotation).toString();
	}
}