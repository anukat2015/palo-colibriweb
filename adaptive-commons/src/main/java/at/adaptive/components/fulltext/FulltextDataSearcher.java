package at.adaptive.components.fulltext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.util.Version;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import at.adaptive.components.common.Validator;

@Name("fulltextDataSearcher")
@Install(precedence = Install.FRAMEWORK)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class FulltextDataSearcher implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1285358392201879006L;

	protected static final Logger logger = Logger.getLogger(FulltextDataSearcher.class);

	private static final String WILDCARD_VALUE = "*";

	private static final String FUZZY_VALUE = "~";

	private static final Set<String> EMPTY_SET = new HashSet<String>(0);

	@In
	private FulltextDataProvider fulltextDataProvider;

	@SuppressWarnings("unchecked")
	public <T> List<T> search(String queryString, FulltextSearchConfiguration fulltextSearchConfiguration)
	{
		try
		{
			if(queryString == null)
			{
				return null;
			}
			if(fulltextSearchConfiguration.getMinChars() != null && fulltextSearchConfiguration.getMinChars() > queryString.length())
			{
				return null;
			}
			long start = System.currentTimeMillis();
			MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_36,getFieldNames(fulltextSearchConfiguration), new StandardAnalyzer(Version.LUCENE_36,EMPTY_SET));
			parser.setDefaultOperator(Operator.AND);
			String[] splits = queryString.split("\\s");
			StringBuilder sb = new StringBuilder();
			for(String split : splits)
			{
				if(fulltextSearchConfiguration.isAddWildcardPrefix())
				{
					parser.setAllowLeadingWildcard(true);
					sb.append(WILDCARD_VALUE);
				}
				sb.append(split.trim());
				if(fulltextSearchConfiguration.isAddWildcardSuffix())
				{
					sb.append(WILDCARD_VALUE);
				}
				if(fulltextSearchConfiguration.isAddFuzzySuffix())
				{
					queryString = queryString + FUZZY_VALUE + fulltextSearchConfiguration.getFuzzyFactor();
				}
				sb.append(" ");
			}
			queryString = sb.toString().trim();
			Query query = parser.parse(queryString);
			TopScoreDocCollector collector = TopScoreDocCollector.create(fulltextSearchConfiguration.getMaxResults(), true);
			FulltextDataHandler fulltextDataHandler = getFulltextDataHandler(fulltextSearchConfiguration);
			Searcher searcher = fulltextDataHandler.getSearcher();
			searcher.search(query,collector);
			List<ScoreDoc> hits = Arrays.asList(collector.topDocs().scoreDocs);
			int hitCount = hits.size();
			if(hitCount > fulltextSearchConfiguration.getMaxResults())
			{
				hitCount = fulltextSearchConfiguration.getMaxResults();
			}
			List<T> results = new ArrayList<T>();
			if(hitCount > 0)
			{
				FulltextBeanDescriptor fulltextBeanDescriptor = fulltextDataHandler.getFulltextBeanDescriptor();
				if(fulltextSearchConfiguration.isSortByLength())
				{
					Collections.sort(hits, new ScoreDocComparator(searcher, fulltextSearchConfiguration.getFieldNames()));
				}
				for(ScoreDoc scoreDoc : hits)
				{
					T bean = (T)fulltextBeanDescriptor.createBeanInstance();
					Document doc = searcher.doc(scoreDoc.doc);
					List<String> properties = fulltextBeanDescriptor.getProperties();
					for(String property : properties)
					{
						fulltextBeanDescriptor.setBeanProperty(bean, property, doc.get(property));
					}
					if(fulltextBeanDescriptor.isScoreAnnotationPresent())
					{
						float score = scoreDoc.score;
						fulltextBeanDescriptor.setScore(bean, score);
					}
					results.add(bean);
				}
			}
			else
			{
				results = null;
			}
			long end = System.currentTimeMillis();
			logger.info("Search completed in " + (end - start) + " ms.");
			return results;
		}
		catch(ParseException pe)
		{
			logger.info("Cannot parse: " + pe.getMessage());
		}
		catch(Exception e)
		{
			logger.error("Error during search", e);
		}
		return null;
	}

	protected String[] getFieldNames(FulltextSearchConfiguration fulltextSearchConfiguration)
	{
		if(Validator.isEmpty(fulltextSearchConfiguration.getFieldNames()))
		{
			List<String> properties = getFulltextDataHandler(fulltextSearchConfiguration).getFulltextBeanDescriptor().getProperties();
			return properties.toArray(new String[properties.size()]);
		}
		else
		{
			return fulltextSearchConfiguration.getFieldNames();
		}
	}

	protected FulltextDataHandler getFulltextDataHandler(FulltextSearchConfiguration fulltextSearchConfiguration)
	{
		return fulltextDataProvider.getFulltextDataHandler(fulltextSearchConfiguration.getIndexName());
	}

	public class ScoreDocComparator implements Comparator<ScoreDoc>
	{
		private Searcher searcher;
		private String[] fields;

		public ScoreDocComparator(Searcher searcher, String[] fields)
		{
			this.searcher = searcher;
			this.fields = fields;
		}

		public int compare(ScoreDoc o1, ScoreDoc o2)
		{
			int res = new Float(o1.score).compareTo(new Float(o2.score));
			if(res == 0)
			{
				String str1 = concatFields(o1);
				String str2 = concatFields(o2);
				if(str1 == null && str2 == null)
				{
					return 0;
				}
				if(str1 != null && str2 == null)
				{
					return 1;
				}
				if(str1 == null && str2 != null)
				{
					return -1;
				}
				return new Integer(str1.length()).compareTo(new Integer(str2.length()));
			}
			else
			{
				return res;
			}
		}

		private String concatFields(ScoreDoc scoreDoc)
		{
			try
			{
				Document doc = searcher.doc(scoreDoc.doc);
				StringBuilder sb = new StringBuilder();
				for(String field : fields)
				{
					String value = doc.get(field);
					if(value != null)
					{
						sb.append(value);
					}
				}
				return sb.toString();
			}
			catch(Exception e)
			{
				return null;
			}
		}

	}
}
