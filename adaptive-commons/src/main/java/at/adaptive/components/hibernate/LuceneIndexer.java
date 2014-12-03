package at.adaptive.components.hibernate;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.core.ResourceLoader;

import at.adaptive.components.bean.annotation.AnnotationLoader;
import at.adaptive.components.common.CollectionUtil;
import at.adaptive.components.common.Validator;
import at.adaptive.components.session.BaseComponent;

@Name("at.adaptive.components.hibernate.luceneIndexer")
@Scope(ScopeType.APPLICATION)
@Install(precedence = Install.FRAMEWORK)
public class LuceneIndexer extends BaseComponent<LuceneIndexerConfig>
{
	private static final int DEFAULT_BATCH_SIZE = 1000;

	protected static final String MESSAGE_KEY_PREFIX = "at.adaptive.components.hibernate.luceneIndexer";

	private static final Logger logger = Logger.getLogger(LuceneIndexer.class);

	private boolean disableTransactionCommits = false;

	public static LuceneIndexer instance()
	{
		if(!Contexts.isApplicationContextActive())
		{
			throw new IllegalStateException("No active application context");
		}
		LuceneIndexer instance = (LuceneIndexer)Component.getInstance(LuceneIndexer.class, ScopeType.APPLICATION);
		if(instance == null)
		{
			throw new IllegalStateException("No LuceneIndexer could be created");
		}
		return instance;
	}

	public List<Class<?>> getAvailableIndexedClasses() throws Exception
	{
		URL[] urls = getClassPathUrls();
		AnnotationLoader annotationLoader = new AnnotationLoader(new String[]{Indexed.class.getName()}, urls);
		return annotationLoader.getAnnotatedClasses();
	}

	public synchronized void optimize(EntityManager entityManager)
	{
		optimize(entityManager, null);
	}

	public synchronized void optimize(EntityManager entityManager, List<Class<?>> classes)
	{
		try
		{
			logger.info("Starting optimization...");
			FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(entityManager);
			if(CollectionUtil.isEmpty(classes))
			{
				classes = getAvailableIndexedClasses();
			}
			optimize(fullTextEntityManager, classes);
			logger.info("Optimization successfully completed");
		}
		catch(Exception e)
		{
			logger.error("Error while optimizing fulltext index", e);
		}
	}

	public synchronized void optimize(List<Class<?>> classes)
	{
		optimize(getEntityManager(), classes);
	}

	public synchronized void reIndex()
	{
		reIndex(getEntityManager(), null);
	}

	public synchronized void reIndex(EntityManager entityManager)
	{
		reIndex(entityManager, null);
	}

	public synchronized void reIndex(EntityManager entityManager, List<Class<?>> classes)
	{
		try
		{
			logger.info("Starting re-indexing...");
			FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(entityManager);
			if(CollectionUtil.isEmpty(classes))
			{
				classes = getAvailableIndexedClasses();
			}
			((Session)fullTextEntityManager.getDelegate()).setFlushMode(org.hibernate.FlushMode.MANUAL);
			((Session)fullTextEntityManager.getDelegate()).setCacheMode(CacheMode.IGNORE);
			// Transaction tx = fullTextEntityManager.beginTransaction();
			logger.info("Clearing fulltext index...");
			for(Class<?> currentClass : classes)
			{
				fullTextEntityManager.purgeAll(currentClass);
			}
			logger.info("Fulltext index successfully cleared");
			optimize(fullTextEntityManager, classes);
			fullTextEntityManager.flushToIndexes();
			logger.info("Starting re-indexing...");
			for(Class<?> currentClass : classes)
			{
				logger.info("Re-indexing: " + currentClass);
				reIndex(currentClass, fullTextEntityManager, entityManager);
				logger.info("Re-indexing completed: " + currentClass);
			}
			logger.info("Re-indexing successfully completed");
			optimize(fullTextEntityManager, classes);
		}
		catch(Exception e)
		{
			logger.error("Error while re-indexing", e);
		}
	}

	public synchronized void reIndex(List<Class<?>> classes)
	{
		reIndex(getEntityManager(), classes);
	}

	public void setDisableTransactionCommits(boolean disableTransactionCommits)
	{
		this.disableTransactionCommits = disableTransactionCommits;
	}

	protected int getBatchSize()
	{
		return DEFAULT_BATCH_SIZE;
	}

	protected URL[] getClassPathUrls()
	{
		try
		{
			List<URL> urls = new ArrayList<URL>();
			URL url = ResourceLoader.instance().getResource("/META-INF/application.xml");
			URL baseUrl;
			if(url != null)
			{
				File applicationFile = new File(url.toURI());
				baseUrl = applicationFile.getParentFile().getParentFile().toURL();
				urls.add(baseUrl);
			}
			else
			{
				// fallback (maybe we're running on tomcat)
				ServletContext servletContext = ServletLifecycle.getServletContext();
				baseUrl = new File(servletContext.getRealPath("/")).toURL();
			}
			if(getConfig() != null)
			{
				List<String> configuredUrls = getConfig().getUrls();
				if(!Validator.isEmpty(configuredUrls))
				{
					for(String configuredUrl : configuredUrls)
					{
						File file = new File(new File(baseUrl.toURI()), configuredUrl);
						urls.add(file.toURL());
					}
				}
			}
			return urls.toArray(new URL[urls.size()]);
		}
		catch(Exception e)
		{
			logger.error("Error loading class path urls", e);
			return new URL[0];
		}
	}

	@Override
	protected String getMessageKeyPrefix()
	{
		return MESSAGE_KEY_PREFIX;
	}

	private void commit(FullTextEntityManager fullTextEntityManager) throws Exception
	{
		// fullTextEntityManager.flush();
		fullTextEntityManager.flushToIndexes();
		// if(!disableTransactionCommits)
		// {
		// fullTextEntityManager.beginTransaction();
		fullTextEntityManager.clear();
		// Transaction.instance().begin();
		// getEntityManager().joinTransaction();
		// }
	}

	private ScrollableResults getScrollableResults(FullTextEntityManager fullTextEntityManager, Class<?> searchClass, int index) throws Exception
	{
		return ((Session)fullTextEntityManager.getDelegate()).createCriteria(searchClass).setFetchSize(getBatchSize()).setFirstResult(index).scroll(org.hibernate.ScrollMode.FORWARD_ONLY);
	}

	private void optimize(FullTextEntityManager fullTextEntityManager, List<Class<?>> classes) throws Exception
	{
		logger.info("Optimizing...");
		for(Class<?> currentClass : classes)
		{
			fullTextEntityManager.getSearchFactory().optimize(currentClass);
		}
		fullTextEntityManager.flushToIndexes();
		// tx.commit();
		logger.info("Optimization complete");
	}

	private List<?> queryResults(FullTextEntityManager fullTextEntityManager, Class<?> searchClass, int index)
	{
		Criteria criteria = ((Session)fullTextEntityManager.getDelegate()).createCriteria(searchClass);
		criteria.setFirstResult(index);
		criteria.setMaxResults(getBatchSize());
		return criteria.list();
	}

	private void reIndex(Class<?> searchClass, FullTextEntityManager fullTextEntityManager, EntityManager entityManager) throws Exception
	{
		logger.info("Starting re-indexing...");
		((Session)fullTextEntityManager.getDelegate()).setFlushMode(org.hibernate.FlushMode.MANUAL);
		((Session)fullTextEntityManager.getDelegate()).setCacheMode(CacheMode.IGNORE);

		// logger.info("Fetching " + getBatchSize() + " objects of type: " + currentClass.getName());

		ScrollableResults results = ((Session)fullTextEntityManager.getDelegate()).createCriteria(searchClass).setFetchSize(getBatchSize()).scroll(org.hibernate.ScrollMode.FORWARD_ONLY);
		int index = 0;
		while(results.next())
		{
			index++;
			fullTextEntityManager.index(results.get(0));
			if(index % getBatchSize() == 0)
			{
				logger.info(index + " objects re-indexed. Flushing to disk now");
				commit(fullTextEntityManager);
			}
		}
		results.close();

		// List<?> results = queryResults(fullTextEntityManager, currentClass, index);
		// while(true)
		// {
		// if(CollectionUtil.isEmpty(results))
		// {
		// break;
		// }
		// for(Object result : results)
		// {
		// index++;
		// fullTextEntityManager.index(result);
		// }
		// logger.info(index + " objects re-indexed. Closing results and commiting transaction");
		// commit(fullTextEntityManager);
		// logger.info("Fetching " + getBatchSize() + " objects of type: " + currentClass.getName());
		// results = queryResults(fullTextEntityManager, currentClass, index);
		// }
	}
}
