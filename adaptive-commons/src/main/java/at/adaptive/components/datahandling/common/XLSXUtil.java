package at.adaptive.components.datahandling.common;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XLSXUtil
{
	private static final String SUFFIX = "xlsx";

	public static String getFileSuffix()
	{
		return SUFFIX;
	}

	public static Font createHeaderFont(Workbook workbook)
	{
		XSSFFont font = ((XSSFWorkbook)workbook).createFont();
		font.setBold(true);
		return font;
	}

	public static Workbook createWorkbkook()
	{
		return new XSSFWorkbook();
	}
}
