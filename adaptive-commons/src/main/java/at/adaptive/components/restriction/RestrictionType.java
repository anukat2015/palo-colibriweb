package at.adaptive.components.restriction;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.Messages;

/**
 * Restriction types
 * 
 * @author Bernhard Hablesreiter
 * 
 */
public enum RestrictionType
{
	NO_RESTRICTION("restrictions.noRestriction"),
	EQ("restrictions.eq"),
	NE("restrictions.ne"),
	LTE("restrictions.lte"),
	GTE("restrictions.gte"),
	LT("restrictions.lt"),
	GT("restrictions.gt"),
	NULL("restrictions.null"),
	NOT_NULL("restrictions.notNull"),
	LIKE("restrictions.like"),
	ILIKE("restrictions.ilike"),
	CONTAINS("restrictions.contains"),
	FULLTEXT("restrictions.fulltext");

	private String name;

	private RestrictionType(String name)
	{
		if(Contexts.isEventContextActive())
		{
			this.name = Messages.instance().get(name);
		}
		else
		{
			this.name = name;
		}
	}

	@Override
	public String toString()
	{
		return name;
	}

	public String getName()
	{
		return name;
	}
}
