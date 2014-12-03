package at.adaptive.components.restriction;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import at.adaptive.components.common.CollectionUtil;

/**
 * Implementation of an enum type restriction
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public class EnumRestriction extends AbstractRestriction<Enum<?>>
{
	public Object getAsObject(String value, RestrictionType restrictionType)
	{
		Object[] enums = getPropertyClass().getEnumConstants();
		List<Object> objects = new ArrayList<Object>();
		if(enums != null)
		{
			for(Object object : enums)
			{
				String objectValue = object.toString();
				if(restrictionType.equals(RestrictionType.EQ))
				{
					if(objectValue.equals(value))
					{
						objects.add(object);
					}
				}
				else if(restrictionType.equals(RestrictionType.LIKE))
				{
					if(objectValue.startsWith(value))
					{
						objects.add(object);
					}
				}
				else if(restrictionType.equals(RestrictionType.ILIKE))
				{
					if(objectValue.toLowerCase().startsWith(value.toLowerCase()))
					{
						objects.add(object);
					}
				}
				else if(restrictionType.equals(RestrictionType.CONTAINS))
				{
					if(objectValue.toLowerCase().contains(value.toLowerCase()))
					{
						objects.add(object);
					}
				}
			}
		}
		return objects;
	}

	public Class<Enum<?>> getType()
	{
		return null;
	}

	protected boolean isCreateCustomCriterion()
	{
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Criterion createCustomCriterion(Object value, RestrictionType restrictionType)
	{
		if(value instanceof List<?>)
		{
			List<Object> objects = (List<Object>)value;
			if(CollectionUtil.isEmpty(objects))
			{
				return null;
			}
			Disjunction disjunction = Restrictions.disjunction();
			for(Object object : objects)
			{
				disjunction.add(Restrictions.eq(propertyName, object));
			}
			return disjunction;
		}
		else
		{
			return Restrictions.eq(propertyName, value);
		}
	}

	public String getAsString(Enum<?> value)
	{
		return value.toString();
	}
}