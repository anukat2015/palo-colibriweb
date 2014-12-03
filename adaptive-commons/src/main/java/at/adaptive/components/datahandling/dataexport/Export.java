package at.adaptive.components.datahandling.dataexport;

import java.io.OutputStream;
import java.util.List;

import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;

public interface Export<T>
{
	void export(List<T> objects, OutputStream out) throws ExportException;

	void prepareExport(OutputStream out) throws ExportException;

	void addObject(T object, boolean hasNext) throws ExportException;

	void finishExport() throws ExportException;

	String getFileSuffix();

	void setColumnDefinitionContainer(ColumnDefinitionContainer columnDefinitionContainer);

	void close() throws ExportException;
}
