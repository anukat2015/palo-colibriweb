package at.adaptive.components.common;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jboss.seam.annotations.Name;

@Name("collectionUtil")
public class CollectionUtil
{
	public static <T> T getFirstElement(List<T> list)
	{
		if(isEmpty(list))
		{
			return null;
		}
		return list.get(0);
	}

	public static <T> T getLastElement(List<T> list)
	{
		if(isEmpty(list))
		{
			return null;
		}
		return list.get(list.size() - 1);
	}

	public static String getStringValueFromCollection(Collection<?> collection)
	{
		if(CollectionUtil.isEmpty(collection))
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for(Iterator<?> iterator = collection.iterator(); iterator.hasNext();)
		{
			Object object = iterator.next();
			sb.append(object);
			if(iterator.hasNext())
			{
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	public static boolean isEmpty(Collection<?> collection)
	{
		return collection == null || collection.size() == 0;
	}

	public static boolean isEmpty(Object[] array)
	{
		return array == null || array.length == 0;
	}

	public static boolean isNotEmpty(Collection<?> collection)
	{
		return !isEmpty(collection);
	}

	public static boolean isNotEmpty(Object[] array)
	{
		return !isEmpty(array);
	}

	public List<?> getEmptyList()
	{
		return Collections.EMPTY_LIST;
	}
}