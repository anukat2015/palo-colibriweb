package at.adaptive.components.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ImportColumn implements Serializable
{
	private String header;
	private int position;
	private List<Object> values;
	private ImportColumnAssignment importColumnAssignment;

	public ImportColumn(String header, int position)
	{
		super();
		this.header = header;
		this.position = position;
		this.values = new ArrayList<Object>();
	}

	public ImportColumnAssignment getImportColumnAssignment()
	{
		return importColumnAssignment;
	}

	public void setImportColumnAssignment(ImportColumnAssignment importColumnAssignment)
	{
		this.importColumnAssignment = importColumnAssignment;
	}

	public void addValue(Object value)
	{
		values.add(value);
	}

	public String getHeader()
	{
		return header;
	}

	public int getPosition()
	{
		return position;
	}

	public List<Object> getValues()
	{
		return values;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		result = prime * result + position;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof ImportColumn)) return false;
		ImportColumn other = (ImportColumn)obj;
		if(header == null)
		{
			if(other.header != null) return false;
		}
		else if(!header.equals(other.header)) return false;
		if(position != other.position) return false;
		return true;
	}
}