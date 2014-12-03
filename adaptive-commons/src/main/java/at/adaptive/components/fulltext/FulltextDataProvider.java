package at.adaptive.components.fulltext;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.ServletLifecycle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import at.adaptive.components.session.BaseComponent;

@Name("fulltextDataProvider")
@Scope(ScopeType.APPLICATION)
@Install(precedence = Install.FRAMEWORK)
@AutoCreate
public class FulltextDataProvider extends BaseComponent<FulltextDataProviderConfig>
{
	private static final long serialVersionUID = -3339233950630296541L;

	protected static final String MESSAGE_KEY_PREFIX = "at.adaptive.components.fulltext.fulltextDataProvider";

	protected static final Logger logger = Logger.getLogger(FulltextDataProvider.class);

	private static final Set<String> EMPTY_SET = new HashSet<String>(0);

	public static FulltextDataProvider instance()
	{
		if(!Contexts.isApplicationContextActive())
		{
			throw new IllegalStateException("No active application context");
		}
		FulltextDataProvider instance = (FulltextDataProvider)Component.getInstance(FulltextDataProvider.class, ScopeType.APPLICATION);
		if(instance == null)
		{
			throw new IllegalStateException("No FulltextDataProvider could be created");
		}
		return instance;
	}

	protected Map<String, FulltextDataHandler> fulltextDataHandlerMap;

	@Create
	public void create()
	{
		try
		{
			fulltextDataHandlerMap = new HashMap<String, FulltextDataHandler>();
			FulltextDataProviderConfig config = getConfig();
			// read configurations
			for(FulltextDataConfig fulltextDataConfig : config.getConfigs())
			{
				FulltextDataHandler fulltextDataHandler = createFulltextDataHandler(fulltextDataConfig);
				fulltextDataHandlerMap.put(fulltextDataHandler.getName(), fulltextDataHandler);
			}
		}
		catch(Exception e)
		{
			logger.error("Error creating fulltext data provider", e);
		}
	}

	@Destroy
	public void destroy()
	{
		for(Iterator<String> iterator = fulltextDataHandlerMap.keySet().iterator(); iterator.hasNext();)
		{
			try
			{
				FulltextDataHandler fulltextDataHandler = fulltextDataHandlerMap.get(iterator.next());
				fulltextDataHandler.close();
			}
			catch(Exception e)
			{
				logger.error("Error closing fulltext data handler", e);
			}
		}
	}

	public FulltextDataHandler getFulltextDataHandler(String indexName)
	{
		return fulltextDataHandlerMap.get(indexName);
	}

	protected FulltextDataHandler createFulltextDataHandler(FulltextDataConfig fulltextDataConfig) throws Exception
	{
		logger.info("Building index: " + fulltextDataConfig.getName());
		Directory directory;
		if(fulltextDataConfig.isUseFSDirectory() && fulltextDataConfig.getFsDirectoryPath() != null)
		{
			// clear directory
			File file = new File(fulltextDataConfig.getFsDirectoryPath());
			file.delete();
			directory = FSDirectory.open(new File(fulltextDataConfig.getFsDirectoryPath()));
		}
		else
		{
			directory = new RAMDirectory();
		}
		IndexWriterConfig luceneConfig = new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36,EMPTY_SET));
		IndexWriter writer = new IndexWriter(directory, luceneConfig);
		FulltextBeanDescriptor fulltextBeanDescriptor = new FulltextBeanDescriptor(fulltextDataConfig.getBeanClass());
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(getDataFile(fulltextDataConfig));
		NodeList nodeList = document.getElementsByTagName("item");
		for(int i = 0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			org.apache.lucene.document.Document luceneDocument = new org.apache.lucene.document.Document();
			NodeList childNodes = node.getChildNodes();
			for(int j = 0; j < childNodes.getLength(); j++)
			{
				Node childNode = childNodes.item(j);
				if(childNode.getNodeType() == Node.ELEMENT_NODE)
				{
					String name = childNode.getNodeName();
					String value = childNode.getTextContent();
					Field field = new Field(name, value, Field.Store.YES, Field.Index.ANALYZED);
					luceneDocument.add(field);
				}
			}
			writer.addDocument(luceneDocument);
		}
		writer.optimize();
		writer.close();
		logger.info("Index successfully built");
		return new FulltextDataHandler(fulltextDataConfig.getName(), directory, fulltextBeanDescriptor);
	}

	protected File getDataFile(FulltextDataConfig fulltextDataConfig) throws Exception
	{
		File file = new File(fulltextDataConfig.getFilename());
		if(file.exists())
		{
			return file;
		}
		else
		{
			ServletContext ctx = ServletLifecycle.getServletContext();
			file = new File(ctx.getRealPath(fulltextDataConfig.getFilename()));
		}
		return file;
	}

	@Override
	protected String getMessageKeyPrefix()
	{
		return MESSAGE_KEY_PREFIX;
	}
}
