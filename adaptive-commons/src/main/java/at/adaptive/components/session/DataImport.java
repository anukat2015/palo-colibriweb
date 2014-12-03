package at.adaptive.components.session;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import at.adaptive.components.common.ImportColumn;
import at.adaptive.components.datahandling.common.ColumnDefinition;
import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;
import at.adaptive.components.datahandling.dataimport.Import;

public abstract class DataImport<T> extends BaseComponent
{
	private static final long serialVersionUID = 445037870119660403L;

	private static final String MESSAGE_KEY_PREFIX = "at.adaptive.components.DataImporter";

	private static final Logger logger = Logger.getLogger(DataImport.class);

	private List<ImportColumn> importColumns;

	private List<T> data;

	private int currentItem = 0;

	private boolean importRunning;

	public boolean checkImport()
	{
		try
		{
			InputStream importSource = getImportSource();
			if(importSource == null)
			{
				addErrorMessage("fileNotFound");
				return false;
			}
			ColumnDefinitionContainer columnDefinitionContainer = createColumnDefinitionContainer();
			columnDefinitionContainer.setColumnDefinitions(createColumnDefinitions());
			if(columnDefinitionContainer.getColumnDefinitions().isEmpty())
			{
				addWarnMessage("noColumnsSelected");
				return false;
			}
			if(!isImportColumnAssignmentValid())
			{
				return false;
			}
			Import<T> dataImport = getDataImport(columnDefinitionContainer);
			if(dataImport == null)
			{
				addErrorMessage("unknownFileFormat");
				return false;
			}
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	public int getCurrentItem()
	{
		return currentItem;
	}

	public List<T> getData()
	{
		return data;
	}

	public List<ImportColumn> getImportColumns()
	{
		return importColumns;
	}

	public abstract InputStream getImportSource();

	public int getResultCount()
	{
		if(data == null)
		{
			return 0;
		}
		return data.size();
	}

	public void importData()
	{
		try
		{
			currentItem = 0;
			importRunning = true;
			if(!isImportColumnAssignmentValid())
			{
				return;
			}
			importProcessStarted();
			InputStream importSource = getImportSource();
			if(importSource == null)
			{
				addErrorMessage("fileNotFound");
				return;
			}
			ColumnDefinitionContainer columnDefinitionContainer = createColumnDefinitionContainer();
			columnDefinitionContainer.setColumnDefinitions(createColumnDefinitions());
			if(columnDefinitionContainer.getColumnDefinitions().isEmpty())
			{
				addWarnMessage("noColumnsSelected");
				return;
			}
			Import<T> dataImport = getDataImport(columnDefinitionContainer);
			if(dataImport == null)
			{
				addErrorMessage("unknownFileFormat");
				return;
			}
			data = dataImport.importData(importSource);
			importProcessCompleted();
			importRunning = false;
		}
		catch(Exception e)
		{
			logger.error("Error importing data", e);
			addErrorMessage("importError");
			importRunning = false;
		}
	}

	public void importPreview()
	{
		try
		{
			previewProcessStarted();
			InputStream importSource = getImportSource();
			if(importSource == null)
			{
				addErrorMessage("fileNotFound");
				return;
			}
			Import<Object[]> previewImport = getPreviewImport(new ColumnDefinitionContainer(Object[].class, false));
			if(previewImport == null)
			{
				addErrorMessage("unknownFileFormat");
				return;
			}
			List<Object[]> importPreview = previewImport.importData(importSource, getPreviewSize());
			importColumns = new ArrayList<ImportColumn>();
			if(importPreview == null || importPreview.size() < 2)
			{
				addWarnMessage("noDataFound");
				return;
			}
			Object[] headers = importPreview.get(0);
			for(int i = 0; i < headers.length; i++)
			{
				Object object = headers[i];
				String header = object == null ? "" : object.toString();
				ImportColumn importColumn = new ImportColumn(header, i);
				importColumns.add(importColumn);
			}
			for(int i = 1; i < importPreview.size(); i++)
			{
				Object[] objects = importPreview.get(i);
				for(int j = 0; j < objects.length; j++)
				{
					importColumns.get(j).addValue(objects[j]);
				}
			}
			previewProcessCompleted();
		}
		catch(Exception e)
		{
			logger.error("Error importing data", e);
			addErrorMessage("importError");
		}
	}

	public boolean isImportRunning()
	{
		return importRunning;
	}

	public void setCurrentItem(int currentItem)
	{
		this.currentItem = currentItem;
	}

	public void setImportRunning(boolean importRunning)
	{
		this.importRunning = importRunning;
	}

	protected void clearData()
	{
		data = null;
	}

	protected void clearImportColumns()
	{
		importColumns = null;
	}

	protected ColumnDefinitionContainer createColumnDefinitionContainer() throws Exception
	{
		return new ColumnDefinitionContainer(getBeanClass(), true);
	}

	protected abstract List<ColumnDefinition> createColumnDefinitions();

	protected abstract Class<T> getBeanClass();

	protected abstract Import<T> getDataImport(ColumnDefinitionContainer columnDefinitionContainer);

	@Override
	protected String getMessageKeyPrefix()
	{
		return MESSAGE_KEY_PREFIX;
	}

	protected abstract Import<Object[]> getPreviewImport(ColumnDefinitionContainer columnDefinitionContainer);

	protected int getPreviewSize()
	{
		return 10;
	}

	protected void importProcessCompleted()
	{}

	protected void importProcessStarted()
	{}

	protected boolean isImportColumnAssignmentValid()
	{
		return true;
	}

	protected void previewProcessCompleted()
	{}

	protected void previewProcessStarted()
	{
		clearData();
	}
}