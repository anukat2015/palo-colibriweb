package at.adaptive.components.datahandling.test;

import java.util.Date;

public class TestBean
{
	private String string;
	private Integer integer;
	private Date date;

	private TestBean child;

	public TestBean getChild()
	{
		if(child == null)
		{
			child = new TestBean();
		}
		return child;
	}

	public void setChild(TestBean child)
	{
		if(child == null)
		{
			child = new TestBean();
		}
		this.child = child;
	}

	/**
	 * @return the string
	 */
	public String getString()
	{
		return string;
	}

	/**
	 * @param string
	 *            the string to set
	 */
	public void setString(String string)
	{
		this.string = string;
	}

	/**
	 * @return the integer
	 */
	public Integer getInteger()
	{
		return integer;
	}

	/**
	 * @param integer
	 *            the integer to set
	 */
	public void setInteger(Integer integer)
	{
		this.integer = integer;
	}

	/**
	 * @return the date
	 */
	public Date getDate()
	{
		return date;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(Date date)
	{
		this.date = date;
	}
}