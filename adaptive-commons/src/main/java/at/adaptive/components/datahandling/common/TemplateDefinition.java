package at.adaptive.components.datahandling.common;


public class TemplateDefinition
{
	/**
	 * The sheet position. Default = 0
	 */
	private Integer sheetPosition = 0;

	/**
	 * The header line. Default = 0
	 */
	private Integer headerLine = 0;

	/**
	 * The start line of the excel sheet. Default = 1
	 */
	private Integer startLine = 1;

	/**
	 * The second line. If <code>true</code>, all "even" rows will be formatted like this row. Default is <code>false</code>
	 */
	private boolean secondLine = false;

	/**
	 * The sum line. Default is <code>null</code> (no sums)
	 */
	private Integer sumLine = null;

	/**
	 * The column definition container
	 */
	private ColumnDefinitionContainer columnDefinitionContainer;

	public TemplateDefinition(ColumnDefinitionContainer columnDefinitionContainer)
	{
		this.columnDefinitionContainer = columnDefinitionContainer;
	}

	/**
	 * @return the sheetPosition
	 */
	public Integer getSheetPosition()
	{
		return sheetPosition;
	}

	/**
	 * @param sheetPosition
	 *            the sheetPosition to set
	 */
	public void setSheetPosition(Integer sheetPosition)
	{
		this.sheetPosition = sheetPosition;
	}

	/**
	 * @return the headerLine
	 */
	public Integer getHeaderLine()
	{
		return headerLine;
	}

	/**
	 * @param headerLine
	 *            the headerLine to set
	 */
	public void setHeaderLine(Integer headerLine)
	{
		this.headerLine = headerLine;
	}

	/**
	 * @return the startLine
	 */
	public Integer getStartLine()
	{
		return startLine;
	}

	/**
	 * @param startLine
	 *            the startLine to set
	 */
	public void setStartLine(Integer startLine)
	{

		this.startLine = startLine;
	}

	/**
	 * @return the columnDefinitionContainer
	 */
	public ColumnDefinitionContainer getColumnDefinitionContainer()
	{
		return columnDefinitionContainer;
	}

	/**
	 * @param columnDefinitionContainer
	 *            the columnDefinitionContainer to set
	 */
	public void setColumnDefinitionContainer(ColumnDefinitionContainer columnDefinitionContainer)
	{
		this.columnDefinitionContainer = columnDefinitionContainer;
	}

	/**
	 * @return the sumLine
	 */
	public Integer getSumLine()
	{
		return sumLine;
	}

	/**
	 * @param sumLine
	 *            the sumLine to set
	 */
	public void setSumLine(Integer sumLine)
	{
		this.sumLine = sumLine;
	}

	/**
	 * @return the secondLine
	 */
	public boolean isSecondLine()
	{
		return secondLine;
	}

	/**
	 * @param secondLine
	 *            the secondLine to set
	 */
	public void setSecondLine(boolean secondLine)
	{
		this.secondLine = secondLine;
	}
}
