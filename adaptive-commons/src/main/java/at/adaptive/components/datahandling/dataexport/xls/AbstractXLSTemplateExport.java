package at.adaptive.components.datahandling.dataexport.xls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.core.Expressions.ValueExpression;

import at.adaptive.components.datahandling.common.BeanUtil;
import at.adaptive.components.datahandling.common.ColumnDefinition;
import at.adaptive.components.datahandling.common.ColumnDefinitionContainer;
import at.adaptive.components.datahandling.common.TemplateDefinition;
import at.adaptive.components.datahandling.common.XLSFormulaParser;
import at.adaptive.components.datahandling.dataexport.ExportException;

public abstract class AbstractXLSTemplateExport<T> extends AbstractXLSExport<T>
{
	protected static final Pattern PATTERN = Pattern.compile("(#\\{[^\\}]*\\})");

	protected List<TemplateDefinition> templateDefinitions;
	protected TemplateDefinition currentTemplateDefinition;
	protected Map<Integer, CellStyle> cellStyles;
	protected Map<Integer, CellStyle> secondCellStyles;
	protected Map<Integer, String> formulas;
	protected Map<Integer, String> sumFormulas;
	protected Set<CellRangeAddress> mergedRegions;

	public AbstractXLSTemplateExport(InputStream templateFile, List<TemplateDefinition> templateDefinitions) throws ExportException
	{
		super(templateDefinitions.get(0).getColumnDefinitionContainer());
		try
		{
			this.templateDefinitions = templateDefinitions;
			// read template workbook
			readTemplateWorkbook(templateFile);
			// update column definition container
			// ColumnDefinitionContainer columnDefinitionContainer = templateDefinition.getColumnDefinitionContainer();
			// updateColumnDefinitionContainer(templateFile, templateDefinition);
			// super.setColumnDefinitionContainer(columnDefinitionContainer);
			setAutosizeColumns(false);
			// parse sheets
			parseELExpressions(workbook);
		}
		catch(Exception e)
		{
			throw new ExportException("Error initializing xls template export", e);
		}
		finally
		{
			try
			{
				templateFile.close();
			}
			catch(IOException e)
			{
				logger.error("Error closing template file input stream", e);
			}
		}
	}

	public AbstractXLSTemplateExport(InputStream templateFile, TemplateDefinition templateDefinition) throws ExportException
	{
		super(templateDefinition.getColumnDefinitionContainer());
		try
		{
			this.templateDefinitions = new ArrayList<TemplateDefinition>(1);
			this.templateDefinitions.add(templateDefinition);
			// read template workbook
			readTemplateWorkbook(templateFile);
			// update column definition container
			ColumnDefinitionContainer columnDefinitionContainer = templateDefinition.getColumnDefinitionContainer();
			updateColumnDefinitionContainer(templateDefinition);
			super.setColumnDefinitionContainer(columnDefinitionContainer);
			setAutosizeColumns(false);
			// parse sheets
			parseELExpressions(workbook);
		}
		catch(Exception e)
		{
			throw new ExportException("Error initializing xls template export", e);
		}
		finally
		{
			try
			{
				templateFile.close();
			}
			catch(IOException e)
			{
				logger.error("Error closing template file input stream", e);
			}
		}
	}

	public void export(List<T> objects, OutputStream out) throws ExportException
	{
		if(columnDefinitionContainer == null)
		{
			throw new ExportException("No column definition container specified");
		}
		currentTemplateDefinition = templateDefinitions.get(0);
		this.out = out;
		// setup
		setup();
		// write header
		// write lines
		for(Iterator<T> iterator = objects.iterator(); iterator.hasNext();)
		{
			addLine(iterator.next());
			if(iterator.hasNext())
			{
				handleLineComplete(iterator.hasNext());
			}
		}
		// update sums
		updateSums();
		// finish
		finish();
	}

	public void exportMultiple(List<List<T>> list, OutputStream out) throws ExportException
	{
		try
		{
			if(list.size() != templateDefinitions.size())
			{
				throw new ExportException("Error exporting multiple results. Number of elements in list does not match number of template definitions.");
			}
			this.out = out;
			for(int i = 0; i < templateDefinitions.size(); i++)
			{
				TemplateDefinition templateDefinition = templateDefinitions.get(i);
				currentTemplateDefinition = templateDefinition;
				columnDefinitionContainer = currentTemplateDefinition.getColumnDefinitionContainer();
				beanUtil = new BeanUtil<T>(columnDefinitionContainer);
				ColumnDefinitionContainer columnDefinitionContainer = templateDefinition.getColumnDefinitionContainer();
				updateColumnDefinitionContainer(templateDefinition);
				super.setColumnDefinitionContainer(columnDefinitionContainer);
				// setup
				setup();
				// write header
				// write lines
				List<T> objects = list.get(i);
				for(Iterator<T> iterator = objects.iterator(); iterator.hasNext();)
				{
					addLine(iterator.next());
					if(iterator.hasNext())
					{
						handleLineComplete(iterator.hasNext());
					}
				}
				// update sums
				updateSums();
			}
			// finish
			finish();
		}
		catch(ExportException ee)
		{
			throw ee;
		}
		catch(Exception e)
		{
			throw new ExportException("Error exporting multiple results", e);
		}
	}

	protected void applyMergedRegions()
	{
		for(CellRangeAddress cellRangeAddress : mergedRegions)
		{
			sheet.addMergedRegion(new CellRangeAddress(currentRow, currentRow, cellRangeAddress.getFirstColumn(), cellRangeAddress.getLastColumn()));
		}
	}

	protected void createNewRow()
	{
		int numberOfRows = sheet.getLastRowNum() + 1;
		sheet.shiftRows(currentRow + 1, numberOfRows + 1, 1);
		currentRow++;
		row = sheet.getRow(currentRow);
		if(row == null)
		{
			row = sheet.createRow(currentRow);
		}
		applyMergedRegions();
	}

	protected int getNumberOfLinesWritten()
	{
		return currentRow - currentTemplateDefinition.getStartLine();
	}

	@Override
	protected void handleCellWritten(Cell cell, ColumnDefinition columnDefinition, int rowNum)
	{
		if(formulas.containsKey(columnDefinition.getPosition()))
		{
			String formula = formulas.get(columnDefinition.getPosition());
			// update formula
			String updatedFormula = XLSFormulaParser.parseFormula(formula, rowNum + 1, getNumberOfLinesWritten(), currentTemplateDefinition.getStartLine(), false);
			if(formula == null)
			{
				logger.warn("Error parsing formula: " + formula);
			}
			cell.setCellFormula(updatedFormula);
		}
	}

	@Override
	public void prepareExport(OutputStream out) throws ExportException
	{
		currentTemplateDefinition = templateDefinitions.get(0);
		super.prepareExport(out);
	}

	@Override
	protected void initLength()
	{
		length = sheet.getRow(currentTemplateDefinition.getStartLine()).getLastCellNum();
	}

	protected void parseELExpressions(Workbook workbook)
	{
		int numberOfSheets = workbook.getNumberOfSheets();
		for(int i = 0; i < numberOfSheets; i++)
		{
			Sheet sheet = workbook.getSheetAt(i);
			if(sheet == null)
			{
				continue;
			}
			int lastRowNum = sheet.getLastRowNum();
			for(int j = 0; j <= lastRowNum; j++)
			{
				Row row = sheet.getRow(j);
				if(row == null)
				{
					continue;
				}
				short lastCellNum = row.getLastCellNum();
				for(short k = 0; k <= lastCellNum; k++)
				{
					Cell cell = row.getCell(k);
					if(cell != null)
					{
						String elValue = getStringCellValue(cell);
						Matcher matcher = PATTERN.matcher(elValue);
						if(elValue != null && matcher.find())
						{
							try
							{
								ValueExpression<?> valueExpression = Expressions.instance().createValueExpression(elValue);
								Object value = valueExpression.getValue();
								if(value instanceof String)
								{
									// flag indicating a single cell-entry (workaround for rich text string with single formatting)
									boolean singleFormat = false;
									HSSFRichTextString richTextString = (HSSFRichTextString)cell.getRichStringCellValue();
									// reset matcher
									matcher.reset();
									// iterate over all matches
									List<MatchInfo> matchInfos = new ArrayList<MatchInfo>();
									while(matcher.find())
									{
										int start = matcher.start();
										int end = matcher.end();
										// get the current el value
										String currentElValue = matcher.group(1);
										if(currentElValue.equals(elValue))
										{
											singleFormat = true;
										}
										// create the value expression
										ValueExpression<?> currentValueExpression = Expressions.instance().createValueExpression(currentElValue);
										Object obj = currentValueExpression.getValue();
										if(obj == null)
										{
											obj = "";
										}
										matchInfos.add(new MatchInfo(start, end, obj.toString()));
									}
									// create new rich text string
									RichTextString newRichTextString = getCreationHelper().createRichTextString((String)value);
									List<FontInfo> fontInfos = new ArrayList<FontInfo>();
									int index = 0;
									// iterate over rich text string formattings
									for(int l = 0; l < richTextString.numFormattingRuns(); l++)
									{
										int lastIndex = index;
										index = richTextString.getIndexOfFormattingRun(l);
										short font = richTextString.getFontAtIndex(lastIndex);
										if((index - lastIndex) > 0)
										{
											fontInfos.add(new FontInfo(lastIndex, index, font));
										}
									}
									if(index < (richTextString.length() - 1))
									{
										fontInfos.add(new FontInfo(index, richTextString.length() - 1, richTextString.getFontAtIndex(richTextString.length() - 1)));
									}
									if(fontInfos.size() < 2)
									{
										singleFormat = true;
									}
									int lengthToCorrect = 0;
									if(!singleFormat)
									{
										for(FontInfo fontInfo : fontInfos)
										{
											// get length difference of match infos in range
											int lengthDifference = getLengthDifferenceOfMatchInfosInRange(matchInfos, fontInfo.getStart(), fontInfo.getEnd());
											// get range
											int start = fontInfo.getStart() - lengthToCorrect;
											int end = fontInfo.getEnd() - lengthToCorrect;
											short font = fontInfo.getFont();
											// set length to correct
											lengthToCorrect += lengthDifference;
											end = end - lengthDifference;
											if(end > newRichTextString.length() - 1)
											{
												end = newRichTextString.length() - 1;
											}
											// apply font
											newRichTextString.applyFont(start, end, font);
										}
									}
									// set the rich text string
									setCellValue(cell, newRichTextString);
								}
								else
								{
									setCellValue(cell, value);
								}
							}
							catch(Exception e)
							{
								logger.error("Error evaluating value expression: " + elValue, e);
								setCellValue(cell, "");
							}
						}
					}
				}
			}
		}
	}

	protected void readTemplateWorkbook(InputStream templateFile) throws Exception
	{
		workbook = WorkbookFactory.create(templateFile);
	}

	@Override
	protected void setCellStyle(Cell cell, int position, CellStyle cellStyle)
	{
		Map<Integer, CellStyle> cellStyles;
		if(currentTemplateDefinition.isSecondLine())
		{
			cellStyles = (getNumberOfLinesWritten() % 2) == 0 ? this.cellStyles : this.secondCellStyles;
		}
		else
		{
			cellStyles = this.cellStyles;
		}
		if(cellStyles.containsKey(position))
		{
			cellStyle = cellStyles.get(position);
			cell.setCellStyle(cellStyle);
		}
	}

	@Override
	protected void setup() throws ExportException
	{
		setupCellStyles();
		setupFormulas();
		setupMergedRegions();
		currentRow = currentTemplateDefinition.getStartLine();
		// remove template line
		int numberOfRows = sheet.getLastRowNum() + 1;
		int rowsToShift = currentTemplateDefinition.isSecondLine() ? 2 : 1;
		for(int i = 0; i < rowsToShift; i++)
		{
			sheet.shiftRows(currentRow + 1, numberOfRows + 1, -1);
		}
		for(CellRangeAddress cellRangeAddress : mergedRegions)
		{
			if(cellRangeAddress.getFirstRow() == currentRow)
			{
				for(int i = 0; i < sheet.getNumMergedRegions(); i++)
				{
					if(sheet.getMergedRegion(i).equals(cellRangeAddress))
					{
						sheet.removeMergedRegion(i);
						break;
					}
				}
			}
		}
		currentRow--;
		createNewRow();
	}

	@Override
	protected void setupCellStyles()
	{
		super.setupCellStyles();
		// read template cell styles
		cellStyles = new HashMap<Integer, CellStyle>();
		setupCellStyles(cellStyles, currentTemplateDefinition.getStartLine());
		secondCellStyles = new HashMap<Integer, CellStyle>();
		if(currentTemplateDefinition.isSecondLine())
		{
			setupCellStyles(secondCellStyles, currentTemplateDefinition.getStartLine() + 1);
		}
	}

	protected void setupFormulas()
	{
		formulas = new HashMap<Integer, String>();
		setupFormulas(formulas, currentTemplateDefinition.getStartLine());
		sumFormulas = new HashMap<Integer, String>();
		setupFormulas(sumFormulas, currentTemplateDefinition.getSumLine());
	}

	protected void setupMergedRegions()
	{
		Integer line = currentTemplateDefinition.getStartLine();
		mergedRegions = new HashSet<CellRangeAddress>();
		for(int i = 0; i < sheet.getNumMergedRegions(); i++)
		{
			CellRangeAddress cellRangeAddress = sheet.getMergedRegion(i);
			if(cellRangeAddress.getFirstRow() == cellRangeAddress.getLastRow() && cellRangeAddress.getFirstRow() == line)
			{
				if(!mergedRegions.contains(cellRangeAddress))
				{
					mergedRegions.add(cellRangeAddress);
				}
			}
		}
	}

	protected void updateColumnDefinitionContainer(TemplateDefinition templateDefinition) throws Exception
	{
		// get the sheet
		sheet = workbook.getSheetAt(templateDefinition.getSheetPosition());
		// read columns
		Row headerRow = sheet.getRow(templateDefinition.getHeaderLine());
		// iterate over cells
		short lastCellNum = headerRow.getLastCellNum();
		for(int i = 0; i < lastCellNum; i++)
		{
			// get value
			Cell cell = headerRow.getCell(i);
			if(cell == null)
			{
				// empty cell
				continue;
			}
			// get the value
			String value = cell.getStringCellValue();
			// get the corresponding column definition
			ColumnDefinition columnDefinition = columnDefinitionContainer.getColumnDefinition(value);
			if(columnDefinition == null)
			{
				columnDefinitionContainer.addColumnDefinition(new ColumnDefinition(i, value, false));
				// no column definition found - this might be ok
				continue;
			}
			// update position
			columnDefinition.setPosition(i);
		}
	}

	protected void updateSums()
	{
		if(currentTemplateDefinition.getSumLine() != null)
		{
			int offsetCorrection = 0;
			if(currentTemplateDefinition.isSecondLine())
			{
				offsetCorrection = -1;
			}
			// get the sum line
			int line = currentTemplateDefinition.getSumLine() + getNumberOfLinesWritten();
			if(currentTemplateDefinition.isSecondLine())
			{
				line--;
			}
			Row row = sheet.getRow(line);
			if(row == null)
			{
				logger.warn("No sum-line found. Skipping sum-line formula update");
				return;
			}
			int lastCellNum = row.getLastCellNum();
			for(int k = 0; k <= lastCellNum; k++)
			{
				if(sumFormulas.containsKey(k))
				{
					String formula = sumFormulas.get(k);
					if(formula == null)
					{
						logger.warn("Error parsing formula: " + formula);
						continue;
					}
					Cell cell = row.getCell(k);
					String updatedFormula = XLSFormulaParser.parseSumFormula(formula, getNumberOfLinesWritten() + 1, currentTemplateDefinition.getStartLine(), currentTemplateDefinition.getSumLine() + 1, offsetCorrection);
					cell.setCellFormula(updatedFormula);
				}
			}
		}
	}

	private int getLengthDifferenceOfMatchInfosInRange(List<MatchInfo> matchInfos, int start, int end)
	{
		int lengthDifference = 0;
		for(MatchInfo matchInfo : matchInfos)
		{
			if((matchInfo.getStart() == start || matchInfo.getStart() > start) && (matchInfo.getEnd() == end || matchInfo.getEnd() < end))
			{
				lengthDifference = lengthDifference + (matchInfo.getLength() - matchInfo.getValue().length());
			}
		}
		return lengthDifference;
	}

	private void setupCellStyles(Map<Integer, CellStyle> cellStyles, Integer line)
	{
		if(line == null)
		{
			return;
		}
		Row headerRow = sheet.getRow(line);
		if(headerRow == null)
		{
			return;
		}
		// iterate over cells
		short lastCellNum = headerRow.getLastCellNum();
		for(int i = 0; i < lastCellNum; i++)
		{
			// get value
			Cell cell = headerRow.getCell(i);
			if(cell == null)
			{
				// empty cell
				continue;
			}
			CellStyle cellStyle = cell.getCellStyle();
			if(cellStyle != null)
			{
				cellStyles.put(i, cellStyle);
			}
		}
	}

	private void setupFormulas(Map<Integer, String> formulas, Integer line)
	{
		if(line == null)
		{
			return;
		}
		Row row = sheet.getRow(line);
		if(row == null)
		{
			return;
		}
		// iterate over cells
		short lastCellNum = row.getLastCellNum();
		for(int i = 0; i < lastCellNum; i++)
		{
			// get value
			Cell cell = row.getCell(i);
			if(cell == null)
			{
				// empty cell
				continue;
			}
			if(cell.getCellType() == Cell.CELL_TYPE_FORMULA)
			{
				// get the formula string
				String formula = cell.getCellFormula();
				if(formula != null)
				{
					formulas.put(i, formula);
				}
			}
		}
	}

	private class FontInfo
	{
		private int start;
		private int end;
		private short font;

		public FontInfo(int start, int end, short font)
		{
			super();
			this.start = start;
			this.end = end;
			this.font = font;
		}

		public int getEnd()
		{
			return end;
		}

		public short getFont()
		{
			return font;
		}

		public int getStart()
		{
			return start;
		}
	}

	private class MatchInfo
	{
		private int start;
		private int end;
		private String value;

		public MatchInfo(int start, int end, String value)
		{
			this.start = start;
			this.end = end;
			this.value = value;
		}

		public int getEnd()
		{
			return end;
		}

		public int getLength()
		{
			return end - start;
		}

		public int getStart()
		{
			return start;
		}

		public String getValue()
		{
			return value;
		}
	}
}