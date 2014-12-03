package at.adaptive.components.datahandling.dataimport.xls;

import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import at.adaptive.components.datahandling.common.ColumnDefinition;
import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;
import at.adaptive.components.datahandling.dataimport.AbstractImport;
import at.adaptive.components.datahandling.dataimport.ImportException;

public abstract class AbstractXLSImport<T> extends AbstractImport<T>
{
	protected Workbook workbook;
	protected Sheet sheet;
	protected Row row;

	public AbstractXLSImport(ColumnDefinitionContainer columnDefinitionContainer)
	{
		super(columnDefinitionContainer);
	}

	public void close() throws ImportException
	{
		try
		{
			in.close();
		}
		catch(Exception e)
		{
			throw new ImportException("Error closing file", e);
		}
	}

	@Override
	protected void finish() throws ImportException
	{}

	protected Object getCellValue(Cell cell, ColumnDefinition columnDefinition)
	{
		if(cell == null)
		{
			return null;
		}
		if(cell.getCellType() == Cell.CELL_TYPE_BLANK)
		{
			return null;
		}
		else if(cell.getCellType() == Cell.CELL_TYPE_BOOLEAN)
		{
			return cell.getBooleanCellValue();
		}
		else if(cell.getCellType() == Cell.CELL_TYPE_FORMULA)
		{
			return cell.getCellFormula();
		}
		else if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
		{
			String propertyName = null;
			if(columnDefinition != null)
			{
				propertyName = columnDefinition.getRawPropertyValue();
			}
			return getValueFromObject(cell.getNumericCellValue(), propertyName);
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

	@Override
	protected Object getValueFromObject(Object object, String propertyName)
	{
		Object ret = super.getValueFromObject(object, propertyName);
		if(ret != null)
		{
			return ret;
		}
		Class<?> propretyType = columnDefinitionContainer.getPropertyType(propertyName);
		if(propretyType == null)
		{
			return null;
		}
		if(Date.class.isAssignableFrom(propretyType))
		{
			if(object instanceof Double)
			{
				return HSSFDateUtil.getJavaDate((Double)object);
			}
		}
		return null;
	}

	@Override
	protected boolean hasNext() throws ImportException
	{
		if(row == null)
		{
			return false;
		}
		return sheet.getLastRowNum() >= row.getRowNum();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected T readLine() throws ImportException
	{
		try
		{
			// create object
			T object;
			if(useObjectArrayOutput)
			{
				int size = 0;
				for(ColumnDefinition columnDefinition : columnDefinitionContainer.getColumnDefinitions())
				{
					if((columnDefinition.getPosition() + 1) > size)
					{
						size = columnDefinition.getPosition() + 1;
					}
				}
				if(size == 0)
				{
					size = sheet.getRow(0).getLastCellNum();
				}
				object = (T)new Object[size];
			}
			else
			{
				object = (T)columnDefinitionContainer.getBeanClass().newInstance();
			}
			if(columnDefinitionContainer.size() > 0)
			{
				for(ColumnDefinition columnDefinition : columnDefinitionContainer.getColumnDefinitions())
				{
					Cell cell = row.getCell(columnDefinition.getPosition());
					Object value = getCellValue(cell, columnDefinition);
					if(useObjectArrayOutput)
					{
						Object[] objects = ((Object[])object);
						if(objects.length >= (columnDefinition.getPosition()))
						{
							objects[columnDefinition.getPosition()] = value;
						}
					}
					else
					{
						beanUtil.setValue(object, columnDefinition, value);
					}
				}
			}
			else
			{
				int size = columnDefinitionContainer.size();
				if(size == 0)
				{
					size = sheet.getRow(0).getLastCellNum();
				}
				for(int i = 0; i < size; i++)
				{
					Cell cell = row.getCell(i);
					Object value = getCellValue(cell, null);
					((Object[])object)[i] = value;
				}
			}
			// set row
			row = sheet.getRow(row.getRowNum() + 1);
			return object;
		}
		catch(Exception e)
		{
			throw new ImportException("Error reading line", e);
		}
	}

	@Override
	protected void setup() throws ImportException
	{
		try
		{
			workbook = WorkbookFactory.create(in);
			sheet = workbook.getSheetAt(0);
			row = sheet.getRow(0);
			if(ignoreHeader)
			{
				// set row
				row = sheet.getRow(1);
			}
		}
		catch(Exception e)
		{
			throw new ImportException("Error setting up xls-import", e);
		}
	}
}
