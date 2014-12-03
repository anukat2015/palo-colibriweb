package at.adaptive.components.datahandling.dataexport.xls;

import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

import at.adaptive.components.datahandling.common.TemplateDefinition;
import at.adaptive.components.datahandling.common.XLSUtil;
import at.adaptive.components.datahandling.dataexport.ExportException;

public class XLSTemplateExport<T> extends AbstractXLSTemplateExport<T>
{
	public XLSTemplateExport(InputStream templateFile, List<TemplateDefinition> templateDefinitions) throws ExportException
	{
		super(templateFile, templateDefinitions);
	}

	public XLSTemplateExport(InputStream templateFile, TemplateDefinition templateDefinition) throws ExportException
	{
		super(templateFile, templateDefinition);
	}

	@Override
	protected Font createHeaderFont()
	{
		return XLSUtil.createHeaderFont(workbook);
	}

	@Override
	protected Workbook createWorkbook()
	{
		return null;
	}

	public String getFileSuffix()
	{
		return XLSUtil.getFileSuffix();
	}
}
