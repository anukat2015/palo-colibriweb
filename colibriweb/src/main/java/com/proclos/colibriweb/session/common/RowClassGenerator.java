package com.proclos.colibriweb.session.common;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("rowClassGenerator")
@Scope(ScopeType.APPLICATION)
public class RowClassGenerator
{
	// private static final String FIRST_ROW_CLASS ="first";
	private static final String EVEN_ROW_CLASS = "even";
	private static final String UNEVEN_ROW_CLASS = "uneven";
	private static final String LAST_ROW_CLASS = "last";
	private static final String SEPARATOR = ",";

	public String getRowClasses(Integer rows)
	{
		if(rows == null || rows == 0)
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		// sb.append(FIRST_ROW_CLASS);
		for(int i = 0; i < rows; i++)
		{
			int row = i + 1;
			String rowClass;
			if((row % 2) == 0)
			{
				rowClass = EVEN_ROW_CLASS;
			}
			else
			{
				rowClass = UNEVEN_ROW_CLASS;
			}
			sb.append(rowClass);
			if(i < (rows - 1))
			{
				sb.append(SEPARATOR);
			}
			if(i == (rows - 1))
			{
				sb.append(" ");
				sb.append(LAST_ROW_CLASS);
			}
		}
		return sb.toString();
	}

}
