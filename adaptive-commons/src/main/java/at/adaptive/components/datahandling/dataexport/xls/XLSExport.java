package at.adaptive.components.datahandling.dataexport.xls;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;
import at.adaptive.components.datahandling.common.XLSUtil;

public class XLSExport<T> extends AbstractXLSExport<T>
{
	public XLSExport(ColumnDefinitionContainer columnDefinitionContainer)
	{
		super(columnDefinitionContainer);
	}

	public String getFileSuffix()
	{
		return XLSUtil.getFileSuffix();
	}

	@Override
	protected Font createHeaderFont()
	{
		return XLSUtil.createHeaderFont(workbook);
	}

	@Override
	protected Workbook createWorkbook()
	{
		return XLSUtil.createWorkbkook();
	}
}