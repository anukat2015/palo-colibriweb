package at.adaptive.components.fulltext;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;

public class FulltextDataHandler
{
	private String name;
	private Directory directory;
	private FulltextBeanDescriptor fulltextBeanDescriptor;
	private Searcher searcher;

	public FulltextDataHandler(String name, Directory directory, FulltextBeanDescriptor fulltextBeanDescriptor) throws Exception
	{
		super();
		this.name = name;
		this.directory = directory;
		this.fulltextBeanDescriptor = fulltextBeanDescriptor;
		searcher = new IndexSearcher(directory);
	}

	public void close() throws Exception
	{
		searcher.close();
	}

	public Directory getDirectory()
	{
		return directory;
	}

	public FulltextBeanDescriptor getFulltextBeanDescriptor()
	{
		return fulltextBeanDescriptor;
	}

	public String getName()
	{
		return name;
	}

	public Searcher getSearcher()
	{
		return searcher;
	}
}
