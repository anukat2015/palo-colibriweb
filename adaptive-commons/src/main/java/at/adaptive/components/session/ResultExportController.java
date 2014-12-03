package at.adaptive.components.session;

import java.beans.IntrospectionException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;

import at.adaptive.components.common.MessageHandler;
import at.adaptive.components.common.Validator;
import at.adaptive.components.datahandling.common.ColumnDefinition;
import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;
import at.adaptive.components.datahandling.common.Converter;
import at.adaptive.components.datahandling.common.TemplateDefinition;
import at.adaptive.components.datahandling.dataexport.Export;
import at.adaptive.components.datahandling.dataexport.ExportException;
import at.adaptive.components.datahandling.dataexport.ExportType;
import at.adaptive.components.datahandling.dataexport.csv.CSVExport;
import at.adaptive.components.datahandling.dataexport.xls.AbstractXLSExport;
import at.adaptive.components.datahandling.dataexport.xls.XLSExport;
import at.adaptive.components.datahandling.dataexport.xls.XLSTemplateExport;
import at.adaptive.components.datahandling.dataexport.xls.XLSXExport;
import at.adaptive.components.datahandling.dataexport.xml.XMLExport;
import at.adaptive.components.hibernate.CriteriaWrapper;

public abstract class ResultExportController<T, E> extends ResultSearchController<T> implements IResultExportController<T, E>
{
	private static final Logger logger = Logger.getLogger(ResultExportController.class);

	private static final long serialVersionUID = -5987731296864391829L;

	protected ColumnDefinitionContainer columnDefinitionContainer;

	protected ExportType exportType;

	protected Integer firstExportResult;

	protected Integer maxExportResults;

	protected byte[] exportContent;
	protected Export<E> currentExport;

	public void addColumnDefinition(int position, String headerText, String el)
	{
		columnDefinitionContainer.addColumnDefinition(new ColumnDefinition(position, headerText, el));
	}

	public void addColumnDefinition(String headerText, String el)
	{
		columnDefinitionContainer.addColumnDefinition(new ColumnDefinition(headerText, el));
	}

	public void downloadResults()
	{
		if(!isExportComplete())
		{
			return;
		}
		try
		{
			deliverExport(exportContent, currentExport);
		}
		catch(Exception e)
		{
			logger.error("Error exporting results", e);
			addExportErrorMessage();
		}
		finally
		{
			exportContent = null;
			currentExport = null;
		}
	}

	public void export()
	{
		preExportResults();
		export(exportType);
		postExportResults();
	}

	public void export(String type)
	{
		preExportResults();
		export(parseExportType(type));
		postExportResults();
	}

	public ColumnDefinitionContainer getColumnDefinitionContainer()
	{
		return columnDefinitionContainer;
	}

	public String getExportFileName()
	{
		return getEntityClass().getName();
	}

	public List<T> getExportResults()
	{
		return getExportResults(null, null);
	}

	@SuppressWarnings("unchecked")
	public List<T> getExportResults(Integer firstResult, Integer maxResults)
	{
		try
		{
			CriteriaWrapper criteriaWrapper = createCriteriaWrapper();
			applyExportRestrictions(criteriaWrapper);
			applyExportOrders(criteriaWrapper);
			applyCustomExportFilters(criteriaWrapper);
			applyExportRestrictionGroups(criteriaWrapper);
			Criteria criteria = criteriaWrapper.createCriteria();
			criteria.setFlushMode(FlushMode.MANUAL);
			if(firstResult != null && maxResults != null)
			{
				criteria.setFirstResult(firstResult);
				criteria.setMaxResults(maxResults);
			}
			if(getFirstExportResult() != null)
			{
				criteria.setFirstResult(getFirstExportResult());
			}
			if(!Validator.isEmpty(getMaxExportResults()))
			{
				criteria.setMaxResults(getMaxExportResults());
			}
			// else
			// {
			// criteria.setMaxResults(getResultCount());
			// }
			return criteria.list();
		}
		catch(NoResultException e)
		{
			return Collections.EMPTY_LIST;
		}
	}

	public ExportType getExportType()
	{
		return exportType;
	}

	/**
	 * Returns all known export types
	 * 
	 * @return all known export types
	 */
	public ExportType[] getExportTypes()
	{
		return ExportType.values();
	}

	public Integer getFirstExportResult()
	{
		return firstExportResult;
	}

	public Integer getMaxExportResults()
	{
		return maxExportResults;
	}

	public boolean isExportComplete()
	{
		return exportContent != null;
	}

	public ScrollableResults scrollExportResults()
	{
		try
		{
			CriteriaWrapper criteriaWrapper = createCriteriaWrapper();
			applyExportRestrictions(criteriaWrapper);
			applyExportOrders(criteriaWrapper);
			applyCustomExportFilters(criteriaWrapper);
			applyExportRestrictionGroups(criteriaWrapper);
			Criteria criteria = criteriaWrapper.createCriteria();
			if(getFirstExportResult() != null)
			{
				criteria.setFirstResult(getFirstExportResult());
			}
			if(!Validator.isEmpty(getMaxExportResults()))
			{
				criteria.setMaxResults(getMaxExportResults());
			}
			// else
			// {
			// if(getResultCount() != null)
			// {
			// criteria.setMaxResults(getResultCount());
			// }
			// }
			return criteria.scroll(ScrollMode.FORWARD_ONLY);
		}
		catch(NoResultException e)
		{
			return null;
		}
	}

	public void setExportType(ExportType exportType)
	{
		this.exportType = exportType;
	}

	public void setFirstExportResult(Integer firstExportResult)
	{
		this.firstExportResult = firstExportResult;
	}

	public void setMaxExportResults(Integer maxExportResults)
	{
		this.maxExportResults = maxExportResults;
	}

	/**
	 * Adds a column definition to the column definition container
	 * 
	 * @param columnDefinition
	 *            the column definition to add
	 */
	protected void addColumnDefinition(ColumnDefinition columnDefinition)
	{
		columnDefinitionContainer.addColumnDefinition(columnDefinition);
	}

	/**
	 * Adds an export column to the {@link ColumnDefinitionContainer}
	 * 
	 * @param headerText
	 *            the header text to be used
	 * @param el
	 *            the expression (e.g. "name" or "child.name")
	 */
	protected void addExportColumn(String headerText, String el)
	{
		addExportColumn(headerText, el, null);
	}

	/**
	 * Adds an export column to the {@link ColumnDefinitionContainer}
	 * 
	 * @param headerText
	 *            the header text to be used
	 * @param el
	 *            the expression (e.g. "name" or "child.name")
	 * @param converter
	 *            the converter to use
	 */
	protected void addExportColumn(String headerText, String el, Converter<?, ?> converter)
	{
		ColumnDefinition columnDefinition = new ColumnDefinition(headerText, el);
		columnDefinition.setConverter(converter);
		addColumnDefinition(columnDefinition);
	}

	/**
	 * Adds an new export error message to the faces messages
	 */
	protected void addExportErrorMessage()
	{
		MessageHandler.addErrorMessage("at.adaptive.components.common.export.exportError");
	}

	/**
	 * Applies custom export filters. Subclasses may override. This default implementation calls {@link #applyCustomFilters(CriteriaWrapper)}
	 * 
	 * @param criteriaWrapper
	 *            the {@link CriteriaWrapper} to use
	 */
	protected void applyCustomExportFilters(CriteriaWrapper criteriaWrapper)
	{
		applyCustomFilters(criteriaWrapper);
	}

	/**
	 * Applies export orders. Subclasses may override. This default implementation calls {@link #applyOrders(CriteriaWrapper)}
	 * 
	 * @param criteriaWrapper
	 *            the {@link CriteriaWrapper} to use
	 */
	protected void applyExportOrders(CriteriaWrapper criteriaWrapper)
	{
		applyOrders(criteriaWrapper);
	}

	/**
	 * Applies export restriction groups. Subclasses may override. This default implementation calls {@link #applyExportRestrictionGroups(CriteriaWrapper)}
	 * 
	 * @param criteriaWrapper
	 *            the {@link CriteriaWrapper} to use
	 */
	protected void applyExportRestrictionGroups(CriteriaWrapper criteriaWrapper)
	{
		applyRestrictionGroups(criteriaWrapper);
	}

	/**
	 * Applies export restrictions. Subclasses may override. This default implementation calls {@link #applyRestrictions(CriteriaWrapper)}
	 * 
	 * @param criteriaWrapper
	 *            the {@link CriteriaWrapper} to use
	 */
	protected void applyExportRestrictions(CriteriaWrapper criteriaWrapper)
	{
		applyRestrictions(criteriaWrapper);
	}

	protected abstract E convertExportResult(T object);

	/**
	 * Creates a filename for an export file
	 * 
	 * @param suffix
	 *            the file suffix to use
	 * @return a created filename for an export file
	 */
	protected String createExportFileName(String suffix)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getExportFileName());
		sb.append("_");
		sb.append(new SimpleDateFormat("yyyy-MM-dd-HHmm").format(new Date()));
		if(!suffix.startsWith("."))
		{
			suffix = "." + suffix;
		}
		sb.append(suffix);
		return sb.toString();
	}

	// private void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException
	// {
	// final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
	// while(src.read(buffer) != -1)
	// {
	// buffer.flip();
	// dest.write(buffer);
	// buffer.compact();
	// }
	// buffer.flip();
	// while(buffer.hasRemaining())
	// {
	// dest.write(buffer);
	// }
	// }

	/**
	 * Delivers the export result to the client
	 * 
	 * @param bytes
	 *            the byte-array holding the result
	 * @param export
	 *            the export
	 * @throws Exception
	 *             on error
	 */
	protected void deliverExport(byte[] bytes, Export<E> export) throws Exception
	{
		HttpServletResponse response = (HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse();
		response.setContentType("application/octet-stream");
		response.addHeader("Content-Disposition", "attachment;filename=\"" + createExportFileName(export.getFileSuffix()) + "\"");
		response.setContentLength(bytes.length);
		// fastChannelCopy(Channels.newChannel(new ByteArrayInputStream(bytes)), Channels.newChannel(response.getOutputStream()));
		OutputStream writer = response.getOutputStream();
		writer.write(bytes);
		writer.flush();
		writer.close();
		FacesContext.getCurrentInstance().responseComplete();
	}

	/**
	 * Exports the results. Queries the results and exports them using an exporter for the specified {@link ExportType}
	 * 
	 * @param exportType
	 *            the {@link ExportType}
	 */
	@SuppressWarnings("unchecked")
	protected void export(ExportType exportType)
	{
		try
		{
			exportStarted();
			if(exportType != null)
			{
				if(useScrollExportMode())
				{
					Export<E> export = getExport(exportType);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					export.prepareExport(baos);
					ScrollableResults scrollableResults = scrollExportResults();
					// int i = 0;
					if(scrollableResults != null)
					{
						int count = 0;
						while(scrollableResults.next())
						{
							// i++;
							count++;
							T item = (T)scrollableResults.get(0);
							E exportItem = convertExportResult(item);
							export.addObject(exportItem, !scrollableResults.isLast());
							if(getExportChunkSize() != null && (count % 1000) == 0)
							{
								getEntityManager().clear();
							}
						}
						scrollableResults.close();
					}
					export.finishExport();
					byte[] bytes = baos.toByteArray();
					export.close();
					deliverExport(bytes, export);
				}
				else
				{
					Export<E> export = getExport(exportType);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					if(getExportChunkSize() == null)
					{
						List<T> resultList = getExportResults();
						List<E> exportList = new ArrayList<E>(resultList.size());
						for(T exportResult : resultList)
						{
							exportList.add(convertExportResult(exportResult));
						}
						export.export(exportList, baos);
					}
					else
					{
						export.prepareExport(baos);
						Integer resultCount = getResultCount();
						Integer firstResult = 0;
						int exportCount = 0;
						while(firstResult < resultCount)
						{
							List<T> resultList = getExportResults(firstResult, getExportChunkSize());
							List<E> exportList = new ArrayList<E>(getExportChunkSize());
							for(T exportResult : resultList)
							{
								exportList.add(convertExportResult(exportResult));
							}
							for(E exportItem : exportList)
							{
								exportCount++;
								export.addObject(exportItem, exportCount < resultCount);
							}
							firstResult += getExportChunkSize();
							getEntityManager().clear();
						}
						export.finishExport();
					}
					byte[] bytes = baos.toByteArray();
					export.close();
					if(isImmediateExport())
					{
						deliverExport(bytes, export);
					}
					else
					{
						this.exportContent = bytes;
						this.currentExport = export;
					}
				}
			}
		}
		catch(Exception e)
		{
			logger.error("Error exporting results", e);
			addExportErrorMessage();
		}
	}

	protected void exportStarted()
	{}

	/**
	 * Returns the default export encoding
	 * 
	 * @return the default export encoding
	 */
	protected String getDefaultExportEncoding()
	{
		return "utf-8";
	}

	/**
	 * Returns an {@link Export} instance for the specified {@link ExportType}
	 * 
	 * @param exportType
	 *            the {@link ExportType}
	 * @return the {@link Export} instance for the specified {@link ExportType}
	 */
	protected Export<E> getExport(ExportType exportType) throws ExportException
	{
		if(columnDefinitionContainer == null || !columnDefinitionContainer.iterator().hasNext())
		{
			initializeColumnDefinitionContainerDefaults();
		}
		if(exportType.equals(ExportType.CSV))
		{
			CSVExport<E> csvExport = new CSVExport<E>(columnDefinitionContainer);
			csvExport.setEncoding(getDefaultExportEncoding());
			return csvExport;
		}
		else if(exportType.equals(ExportType.XLS))
		{
			AbstractXLSExport<E> xlsExport;
			if(getExportXLSTemplateDefinition() != null && getExportXLSTemplateFile() != null)
			{
				xlsExport = new XLSTemplateExport<E>(getExportXLSTemplateFile(), getExportXLSTemplateDefinition());
			}
			else
			{
				xlsExport = new XLSExport<E>(columnDefinitionContainer);
				xlsExport.setSheetName(getExportSheetName());
			}
			return xlsExport;
		}
		else if(exportType.equals(ExportType.XLSX))
		{
			XLSXExport<E> xlsxExport = new XLSXExport<E>(columnDefinitionContainer);
			xlsxExport.setSheetName(getExportSheetName());
			return xlsxExport;
		}
		else if(exportType.equals(ExportType.XML))
		{
			XMLExport<E> xmlExport = new XMLExport<E>(columnDefinitionContainer);
			xmlExport.setUsePropertyNamesForElements(true);
			xmlExport.setExportEmptyElements(false);
			return xmlExport;
		}
		else
		{
			return null;
		}
	}

	protected Integer getExportChunkSize()
	{
		return null;
	}

	/**
	 * Returns the export sheet name for sheet-based-exports
	 * 
	 * @return the export sheet name for sheet-based-exports
	 */
	protected String getExportSheetName()
	{
		return "data";
	}

	/**
	 * Returns the xls export template definition. Subclasses may override.<br>
	 * This default implementation always returns <code>null</code>
	 * 
	 * @return the xls export template definition
	 * @see TemplateDefinition
	 */
	protected TemplateDefinition getExportXLSTemplateDefinition()
	{
		return null;
	}

	/**
	 * Returns the xls export template file. Subclasses may override.<br>
	 * This default implementation always returns <code>null</code>
	 * 
	 * @return the xls export template file as {@link InputStream}
	 */
	protected InputStream getExportXLSTemplateFile()
	{
		return null;
	}

	/**
	 * Initializes the {@link ColumnDefinitionContainer} during the export process
	 */
	protected void initializeColumnDefinitionContainer()
	{
		try
		{
			columnDefinitionContainer = new ColumnDefinitionContainer(getEntityClass(), true);
		}
		catch(IntrospectionException e)
		{
			logger.error("Error initializing column definition container", e);
		}
	}

	/**
	 * Initializes the {@link ColumnDefinitionContainer} during the export process if it has not been populated via {@link #addColumnDefinition(String, String)} or
	 * {@link #addColumnDefinition(int, String, String)}
	 */
	protected void initializeColumnDefinitionContainerDefaults()
	{
		try
		{
			columnDefinitionContainer.initializeFromBean(getEntityClass());
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * Initializes export columns. Subclasses may override.<br>
	 * This default implementation does nothing
	 */
	protected void initializeExportColumns()
	{}

	protected boolean isImmediateExport()
	{
		return true;
	}

	/**
	 * Parses the {@link ExportType} from a specified string value
	 * 
	 * @param type
	 *            the string value
	 * @return the parsed {@link ExportType}
	 */
	protected ExportType parseExportType(String type)
	{
		return ExportType.valueOf(type);
	}

	/**
	 * This method is called after the results have been exported. Subclasses may override.<br>
	 * This default implementation does nothing
	 */
	protected void postExportResults()
	{}

	/**
	 * This method is called before the results are being exported. Subclasses may override.<br>
	 * This default implementation does nothing
	 */
	protected void preExportResults()
	{}

	protected boolean useScrollExportMode()
	{
		return false;
	}
}
