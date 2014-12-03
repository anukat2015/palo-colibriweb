package at.adaptive.components.restriction;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.store.DirectoryProvider;
import org.jboss.seam.core.Expressions;

import at.adaptive.components.bean.annotation.QueryType;
import at.adaptive.components.common.CollectionUtil;
import at.adaptive.components.common.StringUtil;
import at.adaptive.components.hibernate.CriteriaWrapper;

public class BaseRestrictionGroup implements RestrictionGroup
{
	public static final int UNDEFINED = -1;
	protected boolean dirty = false;
	protected String name;
	protected QueryType queryType;
	protected List<Restriction<?>> restrictions = new ArrayList<Restriction<?>>();
	protected RestrictionType restrictionType;
	protected String value;
	private boolean useSubSelectIdFetching = false;
	private int minInputChars = UNDEFINED;
	private boolean fuzzy;
	
	private static final String GRAMMED_WORDS_FIELD = "words";
    private static final String SOURCE_WORD_FIELD = "sourceWord";
    private static final String COUNT_FIELD = "count";



	public void addRestriction(Restriction<?> restriction)
	{
		restrictions.add(restriction);
	}
	
	public List<Restriction<?>> getRestrictions() {
		return restrictions;
	}

	public void clear()
	{
		setValue("");
	}

	public void clearDirty()
	{
		dirty = false;
	}
	
	public List<String> autocomplete(Object suggest) {
    	// get the top 5 terms for query
		String value = suggest.toString();
		List<String> suggestions = new ArrayList<String>();
		if (!StringUtils.isEmpty(value)) {
			FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(Expressions.instance().createValueExpression("#{entityManager}", EntityManager.class).getValue());
			try {
				for (Restriction<?> r : restrictions) {
					if (r.isFulltext()) {
						IndexReader autoCompleteReader = fullTextEntityManager.getSearchFactory().getIndexReaderAccessor().open(r.getSearchClass());
						IndexSearcher autoCompleteSearcher = new IndexSearcher(autoCompleteReader);
						PrefixQuery query = new PrefixQuery(new Term(r.getFulltextFieldName(), value));
						TopDocs docs = autoCompleteSearcher.search(query, 5);
						for (ScoreDoc doc : docs.scoreDocs) {
							String result = autoCompleteReader.document(doc.doc).get(r.getFulltextFieldName()); 
							if (result != null) {
								suggestions.add(result);
							}
				    	}
						autoCompleteSearcher.close();
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
    	return suggestions;
    }

	public Criterion createCriterion(Session session, Class<?> entityClass) throws NoResultException
	{
		if(value != null && minInputChars != UNDEFINED && value.length() < minInputChars)
		{
			return null;
		}
		Junction junction;
		if(queryType.equals(QueryType.DISJUNCTION))
		{
			// or
			junction = Restrictions.disjunction();
		}
		else
		{
			// and
			junction = Restrictions.conjunction();
		}
		int count = 0;
		for(Restriction<?> restriction : restrictions)
		{
			Criterion criterion = null;
			// check for fulltext restriction
			if(restriction.isFulltext() && restrictionType.equals(RestrictionType.FULLTEXT))
			{
				if(restriction.getSearchClass() != null)
				{
					if(!restriction.getSearchClass().equals(entityClass))
					{
						try
						{
							// create a normal id-projected criterion
							CriteriaWrapper criteriaWrapper = new CriteriaWrapper(session, entityClass);
							boolean fuzzyOrig = restriction.isFuzzy();
							restriction.setFuzzy(fuzzy);
							Criterion detachedCriterion = restriction.createCriterion(value, restrictionType);
							restriction.setFuzzy(fuzzyOrig);
							if(detachedCriterion == null)
							{
								continue;
							}
							criteriaWrapper.addCriterion(restriction.getAssociationPath(), detachedCriterion);
							if(useSubSelectIdFetching)
							{
								DetachedCriteria detachedCriteria = criteriaWrapper.createDetachedCriteria();
								detachedCriteria.setProjection(Projections.id());
								criterion = Subqueries.propertyIn(restriction.getIdPropertyName(), detachedCriteria);
							}
							else
							{
								Criteria criteria = criteriaWrapper.createCriteria();
								criteria.setProjection(Projections.id());
								List<?> ids = criteria.list();
								if(CollectionUtil.isEmpty(ids))
								{
									continue;
								}
								criterion = Restrictions.in(restriction.getIdPropertyName(), ids);
							}
						}
						catch(NoResultException nre)
						{
							// check for conjunction
							if(queryType.equals(QueryType.CONJUNCTION))
							{
								// and restriction is empty, throw NoResultException
								throw nre;
							}
						}
					}
					else
					{
						// create a fulltext criterion (Restrictions.in(idPropertyName, value)) for the entity/search-class
						try
						{
							criterion = restriction.createCriterion(value, restrictionType);
						}
						catch(NoResultException nre)
						{
							// check for conjunction
							if(queryType.equals(QueryType.CONJUNCTION))
							{
								// and restriction is empty, throw NoResultException
								throw nre;
							}
						}
					}
				}
			}
			else
			{
				CriteriaWrapper criteriaWrapper = new CriteriaWrapper(session, entityClass);
				Criterion detachedCriterion = restriction.createCriterion(value, restrictionType);
				if(detachedCriterion == null)
				{
					continue;
				}
				criteriaWrapper.addCriterion(restriction.getAssociationPath(), detachedCriterion);
				if(useSubSelectIdFetching)
				{
					DetachedCriteria detachedCriteria = criteriaWrapper.createDetachedCriteria();
					detachedCriteria.setProjection(Projections.id());
					criterion = Subqueries.propertyIn(restriction.getIdPropertyName(), detachedCriteria);
				}
				else
				{
					Criteria criteria = criteriaWrapper.createCriteria();
					criteria.setProjection(Projections.id());
					List<?> ids = criteria.list();
					if(CollectionUtil.isEmpty(ids))
					{
						continue;
					}
					criterion = Restrictions.in(restriction.getIdPropertyName(), ids);
				}
			}
			if(criterion != null)
			{
				junction.add(criterion);
				count++;
			}
		}
		if(count == 0)
		{
			throw new NoResultException();
		}
		return junction;
	}

	public int getMinInputChars()
	{
		return minInputChars;
	}

	public String getName()
	{
		return name;
	}

	public QueryType getQueryType()
	{
		return queryType;
	}

	public RestrictionType getRestrictionType()
	{
		return restrictionType;
	}

	public String getValue()
	{
		return value;
	}

	public boolean isDirty()
	{
		return dirty;
	}

	public boolean isFuzzy()
	{
		return fuzzy;
	}

	public boolean isUseSubSelectIdFetching()
	{
		return useSubSelectIdFetching;
	}

	public boolean isValueSet()
	{
		return !StringUtil.isEmpty(value);
	}

	public void removeAllRestrictions()
	{
		restrictions.clear();
	}

	public void removeRestriction(Restriction<?> restriction)
	{
		restrictions.remove(restriction);
	}

	public void setFuzzy(boolean fuzzy)
	{
		dirty = isDirty(this.fuzzy, fuzzy);
		this.fuzzy = fuzzy;
	}

	public void setMinInputChars(int minInputChars)
	{
		this.minInputChars = minInputChars;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setQueryType(QueryType queryType)
	{
		this.queryType = queryType;
	}

	public void setRestrictionType(RestrictionType restrictionType)
	{
		this.restrictionType = restrictionType;
	}

	public void setUseSubSelectIdFetching(boolean useSubSelectIdFetching)
	{
		this.useSubSelectIdFetching = useSubSelectIdFetching;
	}

	public void setValue(String value)
	{
		setDirty(this.value, value);
		this.value = value;
	}

	/**
	 * Returns whether the specified values are equal
	 * 
	 * @param <U>
	 *            the paremeter type
	 * @param oldValue
	 *            the old value
	 * @param newValue
	 *            the new value
	 * @return <code>true</code> if the values are not equal; <code>false</code> otherwise
	 */
	protected <U> boolean isDirty(U oldValue, U newValue)
	{
		if(oldValue == null && newValue == null)
		{
			return false;
		}
		if(oldValue == null || newValue == null)
		{
			return true;
		}
		return !oldValue.equals(newValue);
	}

	/**
	 * Sets the dirty flag of the value
	 * 
	 * @param oldValue
	 *            the old value
	 * @param newValue
	 *            the new value
	 */
	protected void setDirty(String oldValue, String newValue)
	{
		dirty = isDirty(oldValue, newValue);
		if(dirty && minInputChars != UNDEFINED)
		{
			// check for length
			if((oldValue != null && newValue != null && newValue.length() < minInputChars) || (oldValue == null && newValue != null && newValue.length() < minInputChars))
			{
				dirty = false;
			}
			if(oldValue != null && StringUtil.isEmpty(newValue))
			{
				dirty = true;
			}
		}
	}
}
