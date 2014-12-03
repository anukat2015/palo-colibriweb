package at.adaptive.components.datahandling.dataexport.xls;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;
import at.adaptive.components.datahandling.common.XLSXUtil;

public class XLSXExport<T> extends AbstractXLSExport<T>
{
	public XLSXExport(ColumnDefinitionContainer columnDefinitionContainer)
	{
		super(columnDefinitionContainer);
	}

	public String getFileSuffix()
	{
		return XLSXUtil.getFileSuffix();
	}

	@Override
	protected Font createHeaderFont()
	{
		return XLSXUtil.createHeaderFont(workbook);
	}

	@Override
	protected Workbook createWorkbook()
	{
		return XLSXUtil.createWorkbkook();
	}
}