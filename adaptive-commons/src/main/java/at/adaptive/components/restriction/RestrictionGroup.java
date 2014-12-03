package at.adaptive.components.restriction;

import java.util.List;

import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;

import at.adaptive.components.bean.annotation.QueryType;

public interface RestrictionGroup
{
	void addRestriction(Restriction<?> restriction);

	void clear();

	/**
	 * Clears the dirty state of this restriction group
	 */
	void clearDirty();

	Criterion createCriterion(Session session, Class<?> entityClass) throws NoResultException;

	int getMinInputChars();

	String getName();

	QueryType getQueryType();

	RestrictionType getRestrictionType();

	String getValue();

	boolean isDirty();

	boolean isUseSubSelectIdFetching();

	boolean isValueSet();

	void removeAllRestrictions();

	void removeRestriction(Restriction<?> restriction);

	void setMinInputChars(int minInputChars);

	void setName(String name);

	void setQueryType(QueryType queryType);

	void setRestrictionType(RestrictionType restrictionType);

	void setUseSubSelectIdFetching(boolean useSubSelectIdFetching);

	void setValue(String value);
	
	List<Restriction<?>> getRestrictions(); 
}
