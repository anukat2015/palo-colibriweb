package at.adaptive.components.datahandling.dataimport;

import java.io.InputStream;
import java.util.List;

public interface Import<T>
{
	List<T> importData(InputStream in) throws ImportException;

	List<T> importData(InputStream in, int maxRecords) throws ImportException;

	void close() throws ImportException;

	void setIgnoreHeader(boolean ignoreHeader);

	boolean isIgnoreHeader();

	void setSuppressErrors(boolean suppressErrors);

	boolean isSuppressErrors();
}
