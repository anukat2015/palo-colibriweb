package at.adaptive.components.documentmanagement;

import static org.jboss.seam.ScopeType.APPLICATION;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.util.AnnotatedBeanProperty;

import at.adaptive.components.common.CollectionUtil;
import at.adaptive.components.common.FileUtil;
import at.adaptive.components.documentmanagement.annotation.DocumentContent;
import at.adaptive.components.documentmanagement.annotation.DocumentFile;
import at.adaptive.components.documentmanagement.annotation.DocumentFileName;
import at.adaptive.components.documentmanagement.config.DocumentManagerConfig;
import at.adaptive.components.documentmanagement.exception.DocumentReadException;
import at.adaptive.components.documentmanagement.exception.UnknownFileNameException;
import at.adaptive.components.documentmanagement.exception.UnsupportedDocumentTypeException;
import at.adaptive.components.documentmanagement.reader.DocumentReader;
import at.adaptive.components.session.BaseComponent;

@Name("at.adaptive.components.documentmanagement.documentManager")
@Scope(APPLICATION)
@Install(precedence = Install.FRAMEWORK)
@BypassInterceptors
@Startup
public class DocumentManager extends BaseComponent<DocumentManagerConfig>
{
	protected static final String MESSAGE_KEY_PREFIX = "at.adaptive.components.documentmanagement.documentManager";

	protected Map<Class<?>, PropertySet> propertySets = new HashMap<Class<?>, PropertySet>();

	public static DocumentManager instance()
	{
		if(!Contexts.isApplicationContextActive())
		{
			throw new IllegalStateException("No active application context");
		}
		DocumentManager instance = (DocumentManager)Component.getInstance(DocumentManager.class, ScopeType.APPLICATION);
		if(instance == null)
		{
			throw new IllegalStateException("No DocumentManager could be created");
		}
		return instance;
	}

	public <T> T initializeDocumentContent(T document) throws UnknownFileNameException, UnsupportedDocumentTypeException, DocumentReadException
	{
		PropertySet propertySet = getPropertySet(document.getClass());
		// get fileName
		String fileName = (String)propertySet.getDocumentFileNameProperty().getValue(document);
		if(fileName == null)
		{
			throw new UnknownFileNameException("Filename is null");
		}
		// get bytes
		byte[] bytes;
		if(propertySet.isDocumentFilePropertyEntity())
		{
			Object documentFileEntry = propertySet.getDocumentFileProperty().getValue(document);
			AnnotatedBeanProperty<DocumentFile> documentFileProperty = new AnnotatedBeanProperty<DocumentFile>(documentFileEntry.getClass(), DocumentFile.class);
			bytes = (byte[])documentFileProperty.getValue(documentFileEntry);
		}
		else
		{
			bytes = (byte[])propertySet.getDocumentFileProperty().getValue(document);
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		try
		{
			if(getConfig() != null && CollectionUtil.isNotEmpty(getConfig().getSupportedDocumentTypes()))
			{
				String fileExtension = FileUtil.getFileExtension(fileName);
				if(fileExtension == null || !getConfig().getSupportedDocumentTypes().contains(fileExtension))
				{
					throw new UnsupportedDocumentTypeException("Unsupported document format: " + fileName);
				}
			}
			DocumentReader documentReader = new DocumentReader();
			String content = documentReader.read(bais);
			if(content == null)
			{
				throw new DocumentReadException("Could not read content");
			}
			propertySet.getDocumentContentProperty().setValue(document, content);
		}
		catch(Exception e)
		{
			throw new DocumentReadException("Could not read content", e);
		}
		return document;
	}

	@Override
	protected String getMessageKeyPrefix()
	{
		return MESSAGE_KEY_PREFIX;
	}

	protected PropertySet getPropertySet(Class<?> documentClass)
	{
		PropertySet propertySet;
		if(propertySets.containsKey(documentClass))
		{
			propertySet = propertySets.get(documentClass);
		}
		else
		{
			propertySet = new PropertySet(documentClass);
		}
		return propertySet;
	}

	protected class PropertySet
	{
		private Class<?> documentClass;

		private AnnotatedBeanProperty<DocumentContent> documentContentProperty;
		private AnnotatedBeanProperty<DocumentFileName> documentFileNameProperty;
		private AnnotatedBeanProperty<DocumentFile> documentFileProperty;
		private boolean documentFilePropertyEntity;

		public PropertySet(Class<?> documentClass)
		{
			super();
			this.documentClass = documentClass;
			initProperties();
		}

		public Class<?> getDocumentClass()
		{
			return documentClass;
		}

		public AnnotatedBeanProperty<DocumentContent> getDocumentContentProperty()
		{
			return documentContentProperty;
		}

		public AnnotatedBeanProperty<DocumentFileName> getDocumentFileNameProperty()
		{
			return documentFileNameProperty;
		}

		public AnnotatedBeanProperty<DocumentFile> getDocumentFileProperty()
		{
			return documentFileProperty;
		}

		public boolean isDocumentFilePropertyEntity()
		{
			return documentFilePropertyEntity;
		}

		private void initProperties()
		{
			documentFileNameProperty = new AnnotatedBeanProperty<DocumentFileName>(documentClass, DocumentFileName.class);
			documentFileProperty = new AnnotatedBeanProperty<DocumentFile>(documentClass, DocumentFile.class);
			if(!documentFileProperty.getPropertyType().equals(byte[].class))
			{
				documentFileProperty = new AnnotatedBeanProperty<DocumentFile>(documentClass, DocumentFile.class);
				documentFilePropertyEntity = true;
			}
			documentContentProperty = new AnnotatedBeanProperty<DocumentContent>(documentClass, DocumentContent.class);
		}
	}
}
