package at.adaptive.components.session;

import java.util.List;

import org.hibernate.ScrollableResults;

import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;
import at.adaptive.components.datahandling.dataexport.ExportType;

public interface IResultExportController<T, E>
{
	void addColumnDefinition(int position, String headerText, String el);

	void addColumnDefinition(String headerText, String el);

	void export();

	void export(String type);

	ColumnDefinitionContainer getColumnDefinitionContainer();

	String getExportFileName();

	List<T> getExportResults();

	ExportType getExportType();

	ExportType[] getExportTypes();

	Integer getFirstExportResult();

	Integer getMaxExportResults();

	ScrollableResults scrollExportResults();

	void setExportType(ExportType exportType);

	void setFirstExportResult(Integer firstExportResult);

	void setMaxExportResults(Integer maxExportResults);
}
