package at.adaptive.components.datahandling.common;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

public class XLSUtil
{
	private static final String SUFFIX = "xls";

	public static String getFileSuffix()
	{
		return SUFFIX;
	}

	public static Font createHeaderFont(Workbook workbook)
	{
		HSSFFont font = ((HSSFWorkbook)workbook).createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		return font;
	}

	public static Workbook createWorkbkook()
	{
		return new HSSFWorkbook();
	}
}
