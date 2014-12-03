package at.adaptive.components.datahandling.dataexport.xls;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import at.adaptive.components.common.CollectionUtil;
import at.adaptive.components.common.StringUtil;
import at.adaptive.components.datahandling.common.ColumnDefinition;
import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;
import at.adaptive.components.datahandling.dataexport.AbstractExport;
import at.adaptive.components.datahandling.dataexport.ExportException;

public abstract class AbstractXLSExport<T> extends AbstractExport<T>
{
	protected Workbook workbook;
	protected Sheet sheet;
	protected Row row;
	protected int currentRow;
	protected CellStyle headerCellStyle;
	protected CellStyle dateCellStyle;
	protected CellStyle currencyCellStyle;
	protected String sheetName = "Data";
	protected boolean formatBigDecimalAsCurrency = true;
	protected boolean autosizeColumns = true;

	public AbstractXLSExport(ColumnDefinitionContainer columnDefinitionContainer)
	{
		super(columnDefinitionContainer);
	}

	public void close() throws ExportException
	{
		try
		{
			out.close();
		}
		catch(IOException e)
		{
			throw new ExportException("Error closing stream", e);
		}
	}

	public boolean isAutosizeColumns()
	{
		return autosizeColumns;
	}

	public boolean isFormatBigDecimalAsCurrency()
	{
		return formatBigDecimalAsCurrency;
	}

	public void setAutosizeColumns(boolean autosizeColumns)
	{
		this.autosizeColumns = autosizeColumns;
	}

	public void setCurrencyForma(String pattern)
	{
		currencyCellStyle = workbook.createCellStyle();
		currencyCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat(pattern));
	}

	public void setDateFormat(String pattern)
	{
		dateCellStyle = workbook.createCellStyle();
		dateCellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat(pattern));
	}

	public void setFormatBigDecimalAsCurrency(boolean formatBigDecimalAsCurrency)
	{
		this.formatBigDecimalAsCurrency = formatBigDecimalAsCurrency;
	}

	public void setSheetName(String sheetName)
	{
		this.sheetName = sheetName;
	}

	@Override
	protected void addHeaderValue(ColumnDefinition columnDefinition) throws ExportException
	{
		Cell cell = row.createCell(columnDefinition.getPosition());
		cell.setCellValue(columnDefinition.getValue());
		cell.setCellStyle(headerCellStyle);
	}

	protected abstract Font createHeaderFont();

	protected void createNewRow()
	{
		row = sheet.createRow(currentRow);
		currentRow++;
	}

	protected abstract Workbook createWorkbook();

	@Override
	protected void finish() throws ExportException
	{
		try
		{
			if(isAutosizeColumns())
			{
				int cols = columnDefinitionContainer.size();
				for(short i = 0; i < cols; i++)
				{
					try
					{
						sheet.autoSizeColumn(i);
					}
					catch(Exception e)
					{}
				}
			}
			int rows = sheet.getLastRowNum();
			for(Iterator<ColumnDefinition> iterator = columnDefinitionContainer.iterator(); iterator.hasNext();)
			{
				ColumnDefinition columnDefinition = iterator.next();
				if(columnDefinition.isHideIfEmpty())
				{
					boolean empty = true;
					for(int i = 1; i <= rows; i++)
					{
						Row row = sheet.getRow(i);
						if(row != null)
						{
							Cell cell = row.getCell(columnDefinition.getPosition());
							if(!StringUtil.isEmpty(getStringCellValue(cell)))
							{
								empty = false;
								break;
							}
						}
					}
					if(empty)
					{
						sheet.setColumnHidden(columnDefinition.getPosition(), true);
					}
				}
			}
			workbook.write(out);
		}
		catch(IOException e)
		{
			throw new ExportException("Error exporting XLS", e);
		}
	}

	protected String getStringCellValue(Cell cell)
	{
		if(cell == null)
		{
			return null;
		}
		if(cell.getCellType() == Cell.CELL_TYPE_BLANK)
		{
			return "";
		}
		else if(cell.getCellType() == Cell.CELL_TYPE_BOOLEAN)
		{
			return String.valueOf(cell.getBooleanCellValue());
		}
		else if(cell.getCellType() == Cell.CELL_TYPE_FORMULA)
		{
			return cell.getCellFormula();
		}
		else if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
		{
			return String.valueOf(cell.getNumericCellValue());
		}
		else if(cell.getCellType() == Cell.CELL_TYPE_STRING)
		{
			return cell.getStringCellValue();
		}
		else
		{
			return null;
		}
	}

	protected void handleCellWritten(Cell cell, ColumnDefinition columnDefinition, int rowNum)
	{}

	@Override
	protected void handleLineComplete(boolean hasNext) throws ExportException
	{
		createNewRow();
	}

	protected void setCellStyle(Cell cell, int position, CellStyle cellStyle)
	{
		if(cellStyle != null)
		{
			cell.setCellStyle(cellStyle);
		}
	}

	protected void setCellValue(Cell cell, Object value)
	{
		if(value != null)
		{
			if(value instanceof String)
			{
				cell.setCellValue((String)value);
				// check for email and url
				if(isEmail((String)value) || isUrl((String)value))
				{
					Hyperlink link = getCreationHelper().createHyperlink(Hyperlink.LINK_URL);
					String address = isEmail((String)value) ? "mailto:" + value : (String)value;
					link.setAddress(address);
					cell.setHyperlink(link);
				}
			}
			else if(value instanceof RichTextString)
			{
				cell.setCellValue((RichTextString)value);
			}
			else if(value instanceof Integer)
			{
				cell.setCellValue((Integer)value);
			}
			else if(value instanceof Long)
			{
				cell.setCellValue((Long)value);
			}
			else if(value instanceof Date)
			{
				cell.setCellValue((Date)value);
			}
			else if(value instanceof Calendar)
			{
				cell.setCellValue((Calendar)value);
			}
			else if(value instanceof BigDecimal)
			{
				cell.setCellValue(((BigDecimal)value).doubleValue());
			}
			else if(Collection.class.isAssignableFrom(value.getClass()))
			{
				String collectionValue = CollectionUtil.getStringValueFromCollection((Collection<?>)value);
				if(collectionValue != null)
				{
					cell.setCellValue(collectionValue);
				}
			}
			else
			{
				try
				{
					String stringValue = value.toString();
					if(stringValue != null)
					{
						cell.setCellValue(stringValue);
					}
				}
				catch(Exception e)
				{}
			}
		}
		else
		{
			cell.setCellType(Cell.CELL_TYPE_BLANK);
		}
	}

	protected CreationHelper getCreationHelper()
	{
		return workbook.getCreationHelper();
	}

	@Override
	protected void setup() throws ExportException
	{
		workbook = createWorkbook();
		sheet = workbook.createSheet();
		setupCellStyles();
		workbook.setSheetName(0, sheetName);
		currentRow = 0;
		createNewRow();
	}

	protected void setupCellStyles()
	{
		headerCellStyle = workbook.createCellStyle();
		Font boldFont = createHeaderFont();
		headerCellStyle.setFont(boldFont);
		dateCellStyle = workbook.createCellStyle();
		dateCellStyle.setDataFormat(workbook.createDataFormat().getFormat("dd.mm.yyyy hh:mm"));
		currencyCellStyle = workbook.createCellStyle();
		currencyCellStyle.setDataFormat(workbook.createDataFormat().getFormat("\u20AC #,##0.00;\u20AC #,##0.00"));
	}

	@Override
	protected void writeValue(ColumnDefinition columnDefinition, Object value) throws ExportException
	{
		Cell cell = row.createCell(columnDefinition.getPosition());
		CellStyle cellStyle = null;
		setCellValue(cell, value);
		if(value != null)
		{
			if(value instanceof Date)
			{
				cellStyle = dateCellStyle;
			}
			else if(value instanceof BigDecimal)
			{
				if(isFormatBigDecimalAsCurrency())
				{
					cellStyle = currencyCellStyle;
				}
			}
		}
		setCellStyle(cell, columnDefinition.getPosition(), cellStyle);
		handleCellWritten(cell, columnDefinition, row.getRowNum());
	}
}