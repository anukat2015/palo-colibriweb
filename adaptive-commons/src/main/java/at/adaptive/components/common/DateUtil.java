package at.adaptive.components.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.jboss.seam.annotations.Name;

/**
 * Utility class for handling date related tasks
 * 
 * @author Bernhard Hablesreiter
 * 
 */
@Name("dateUtil")
public class DateUtil
{
	private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd-HHmm", Locale.GERMAN);
	private static final SimpleDateFormat DAY_DATEFORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

	public static Date addDays(Date date, int daysToAdd)
	{
		Calendar calendar = getCalendar(date);
		calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);
		return calendar.getTime();
	}

	public static Date addMonths(Date date, int monthsToAdd)
	{
		Calendar calendar = getCalendar(date);
		calendar.add(Calendar.MONTH, monthsToAdd);
		return calendar.getTime();
	}

	public static Date subtractDays(Date date, int daysToSubtract)
	{
		daysToSubtract = daysToSubtract * -1;

		Calendar calendar = getCalendar(date);
		calendar.add(Calendar.DAY_OF_MONTH, daysToSubtract);
		return calendar.getTime();
	}

	/**
	 * Returns a {@link Calendar} instance representing a specified date
	 * 
	 * @param date
	 *            the date
	 * @return a {@link Calendar} instance representing a specified date
	 */
	public static Calendar getCalendar(Date date)
	{
		Calendar calendar = Calendar.getInstance(Locale.GERMAN);
		calendar.setTime(date);
		return calendar;
	}

	/**
	 * Returns a {@link DateRange} of a specified week in a specified year
	 * 
	 * @param week
	 *            the week
	 * @param year
	 *            the year
	 * @return a {@link DateRange} for the spcified week in the specified year
	 */
	public static DateRange getDateRangeWeekOfYear(int week, int year)
	{
		return getDateRangeWeekOfYear(week, year, false);
	}

	/**
	 * Returns a {@link DateRange} of a specified week in a specified year
	 * 
	 * @param week
	 *            the week
	 * @param year
	 *            the year
	 * @param workDaysOnly
	 *            work days only flag (MO - FR)
	 * @return a {@link DateRange} for the spcified week in the specified year
	 */
	public static DateRange getDateRangeWeekOfYear(int week, int year, boolean workDaysOnly)
	{
		Calendar calendar = getTrimmedToDayCalendar(new Date());
		calendar.setMinimalDaysInFirstWeek(7);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.WEEK_OF_YEAR, week);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		Date startDate = calendar.getTime();
		calendar = getTrimmedToDayCalendarLastMillisecond(startDate);
		calendar.setMinimalDaysInFirstWeek(7);
		// calendar.set(Calendar.YEAR, year);
		// calendar.set(Calendar.WEEK_OF_YEAR, week + 1);
		int numberOfDays = workDaysOnly ? 4 : 6;
		calendar.add(Calendar.DAY_OF_MONTH, numberOfDays);
		// calendar.set(Calendar.DAY_OF_WEEK, dayEnd);
		Date endDate = calendar.getTime();
		return new DateRange(startDate, endDate);
	}

	/**
	 * Returns the day of month value from a specified date
	 * 
	 * @param date
	 *            the date
	 * @return the day of month value of the specified date
	 */
	public static int getDayOfMonthFromDate(Date date)
	{
		return getCalendar(date).get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Returns the number of days between two calendars
	 * 
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 * @return the number of days between the specified calendars
	 */
	public static long getDaysBetween(Calendar startDate, Calendar endDate)
	{
		Calendar date = (Calendar)startDate.clone();
		long daysBetween = 0;
		while(date.before(endDate))
		{
			date.add(Calendar.DAY_OF_MONTH, 1);
			daysBetween++;
		}
		return daysBetween;
	}

	/**
	 * Returns the number of months between two calendars
	 * 
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 * @return the number of months between the specified calendars
	 */
	public static long getMonthsBetween(Calendar startDate, Calendar endDate)
	{
		Calendar date = (Calendar)startDate.clone();
		long monthsBetween = 0;
		while(date.before(endDate))
		{
			date.add(Calendar.MONTH, 1);
			monthsBetween++;
		}
		return monthsBetween - 1;
	}

	/**
	 * Returns the number of monts between two dates
	 * 
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 * @return the number of months between the specified dates
	 */
	public static long getMonthsBetween(Date startDate, Date endDate)
	{
		return getMonthsBetween(getTrimmedToDayCalendar(startDate), getTrimmedToDayCalendar(endDate));
	}

	/**
	 * Returns the number of days between two dates
	 * 
	 * @param startDate
	 *            the start date
	 * @param endDate
	 *            the end date
	 * @return the number of days between the specified dates
	 */
	public static long getDaysBetween(Date startDate, Date endDate)
	{
		return getDaysBetween(getTrimmedToDayCalendar(startDate), getTrimmedToDayCalendar(endDate));
	}

	public static Calendar getFirstDayInMonthCalendar(Date date)
	{
		Calendar calendar = getTrimmedToDayCalendar(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		return calendar;
	}

	public static Date getFirstDayInMonthDate(Date date)
	{
		return getFirstDayInMonthCalendar(date).getTime();
	}

	public static Calendar getFirstDayInYearCalendar(Integer year)
	{
		Calendar calendar = getTrimmedToDayCalendar(new Date());
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.DAY_OF_YEAR, 1);
		return calendar;
	}

	public static Date getFirstDayInYearDate(Integer year)
	{
		return getFirstDayInYearCalendar(year).getTime();
	}

	/**
	 * Returns a formatted date string for a specified date, using a specified pattern
	 * 
	 * @param date
	 *            the date
	 * @param pattern
	 *            the pattern to use
	 * @return a formatted date string for the specified date, using the specified pattern
	 */
	public static String getFormattedDateString(Date date, String pattern)
	{
		if(date == null || pattern == null)
		{
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.GERMAN);
		return sdf.format(date);
	}

	public static Calendar getLastDayInMonthCalendar(Date date)
	{
		Calendar calendar = getTrimmedToDayCalendarLastMillisecond(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		return calendar;
	}

	public static Date getLastDayInMonthDate(Date date)
	{
		return getLastDayInMonthCalendar(date).getTime();
	}

	public static Calendar getLastDayInYearCalendar(Integer year)
	{
		Calendar calendar = getTrimmedToDayCalendarLastMillisecond(new Date());
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
		return calendar;
	}

	public static Date getLastDayInYearDate(Integer year)
	{
		return getLastDayInYearCalendar(year).getTime();
	}

	/**
	 * Returns a formatted time string for a specified duration (pattern: HH:mm)
	 * 
	 * @param duration
	 *            duration in milliseconds
	 * @return the formatted time string for the specified duration
	 */
	public static String getMinutesTimeString(long duration)
	{
		long time = duration / 1000;
		String minutes = Integer.toString((int)((time % 3600) / 60));
		String hours = Integer.toString((int)(time / 3600));
		StringBuilder sb = new StringBuilder();
		if(StringUtil.isEmpty(hours) && StringUtil.isEmpty(minutes))
		{
			return null;
		}
		else
		{
			if(hours.length() == 1) hours = "0" + hours;
			if(minutes.length() == 1) minutes = "0" + minutes;
			if(!StringUtil.isEmpty(hours))
			{
				sb.append(hours);
				sb.append(":");
			}
			if(!StringUtil.isEmpty(minutes))
			{
				sb.append(minutes);
			}
		}
		return sb.toString();
	}

	/**
	 * Return the number of minutes between the time of the day of the dates.
	 * 
	 * @param start
	 *            The startdate
	 * @param end
	 *            The enddate
	 * @return Number of minutes between the dates
	 */
	public static long getMinutesBetween(Date start, Date end)
	{
		/*
		 * SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm"); long startTime = 0; long endTime = 0;
		 * 
		 * try { startTime = timeFormat.parse(timeFormat.format(start)).getTime(); endTime = timeFormat.parse(timeFormat.format(end)).getTime(); } catch(ParseException e) { e.printStackTrace(); }
		 */

		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(start);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(end);
		long startTime = cal1.get(Calendar.MINUTE) + cal1.get(Calendar.HOUR_OF_DAY) * 60;
		long endTime = cal2.get(Calendar.MINUTE) + cal2.get(Calendar.HOUR_OF_DAY) * 60;

		long diff = endTime - startTime;

		return diff;
	}

	/**
	 * Adds a specific amount of minutes to the date
	 * 
	 * @param date
	 *            the start
	 * @param minutes
	 *            aoumt of minutes to be added
	 * @return a <code>Date</code> representing the result
	 */
	public static Date addMinutesToDate(Date date, int minutes)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		c.add(Calendar.MINUTE, minutes);

		return c.getTime();
	}

	/**
	 * Returns the month value from a specified date
	 * 
	 * @param date
	 *            the date
	 * @return the month value of the specified date
	 */
	public static int getMonthFromDate(Date date)
	{
		return getCalendar(date).get(Calendar.MONTH) + 1;
	}

	/**
	 * Returns a simple string representation of the current date (pattern="yyyy-MM-dd-HHmm")
	 * 
	 * @param date
	 *            the date
	 * @return a simple string representation of the current date
	 */
	public static String getSimpleCurrentDateString()
	{
		return getSimpleDateString(new Date());
	}

	/**
	 * Returns a date object of a specified day with hours/minuts/seconds/milliseconds set to "0"
	 * 
	 * @param date
	 *            the date to modify
	 * @return a date object of the specified day with hours/minuts/seconds/milliseconds set to "0"
	 */
	public static Date getSimpleDate(Date date)
	{
		return getTrimmedToDayCalendar(date).getTime();
	}

	/**
	 * Returns a simple string representation of a specified date (pattern="yyyy-MM-dd-HHmm")
	 * 
	 * @param date
	 *            the date
	 * @return a simple string representation of the specified date
	 */
	public static String getSimpleDateString(Date date)
	{
		return DATEFORMAT.format(date);
	}

	/**
	 * Returns a simple string representation of a specified date (pattern="dd.MM.yyyy")
	 * 
	 * @param date
	 *            the date
	 * @return a simple string representation of the specified date
	 */
	public static String getSimpleDayDateString(Date date)
	{
		return DAY_DATEFORMAT.format(date);
	}

	/**
	 * Returns a {@link Calendar} instance representing a specified date, with everything below the day-value set to "0"
	 * 
	 * @param date
	 *            the date
	 * @return a {@link Calendar} instance representing the specified date, with everything below the day-value set to "0"
	 */
	public static Calendar getTrimmedToDayCalendar(Date date)
	{
		Calendar calendar = Calendar.getInstance(Locale.GERMAN);
		calendar.setTime(date);
		calendar = trimToDay(calendar);
		return calendar;
	}

	/**
	 * Returns a {@link Calendar} instance representing a specified date, with everything below the day-value set to the highest possible value
	 * 
	 * @param date
	 *            the date
	 * @return a {@link Calendar} instance representing the specified date, with everything below the day-value set to the highest possible value
	 */
	public static Calendar getTrimmedToDayCalendarLastMillisecond(Date date)
	{
		Calendar calendar = Calendar.getInstance(Locale.GERMAN);
		calendar.setTime(date);
		calendar = trimToDayLastMillisecond(calendar);
		return calendar;
	}

	/**
	 * Returns a {@link Date} instance representing a specified date, with everything below the day-value set to "0"
	 * 
	 * @param date
	 *            the date
	 * @return a {@link Date} instance representing the specified date, with everything below the day-value set to "0"
	 */
	public static Date getTrimmedToDayDate(Date date)
	{
		return getTrimmedToDayCalendar(date).getTime();
	}

	/**
	 * Returns a {@link Date} instance representing a specified date, with everything below the day-value set to the highest possible value
	 * 
	 * @param date
	 *            the date
	 * @return a {@link Date} instance representing the specified date, with everything below the day-value set to the highest possible value
	 */
	public static Date getTrimmedToDayDateLastMillisecond(Date date)
	{
		return getTrimmedToDayCalendarLastMillisecond(date).getTime();
	}

	/**
	 * Returns the year value from a specified date
	 * 
	 * @param date
	 *            the date
	 * @return the year value of the specified date
	 */
	public static int getYearFromDate(Date date)
	{
		return getCalendar(date).get(Calendar.YEAR);
	}

	/**
	 * Returns whether a date range overlaps with another date range
	 * 
	 * @param startDate
	 *            the start date of the first date range
	 * @param endDate
	 *            the end date of the first date range
	 * @param startDate2
	 *            the start date of the second date range
	 * @param endDate2
	 *            the end date of the second date range
	 * @return <code>true</code> if the given date ranges overlap; <code>false</code> otherwise
	 */
	public static boolean isDateRangeOverlapping(Date startDate, Date endDate, Date startDate2, Date endDate2)
	{
		if(startDate == null || endDate == null || startDate2 == null || endDate2 == null)
		{
			return false;
		}
		if(startDate.equals(startDate2) || endDate.equals(endDate2))
		{
			return true;
		}
		return ((startDate2.after(startDate) && startDate2.before(endDate)) || endDate2.after(startDate) && endDate2.before(endDate));
	}

	/**
	 * Returns whether the specified date is in the future
	 * 
	 * @param date
	 *            the date
	 * @return <code>true</code> if the specified date is in the future, <code>false</code> otherwise
	 */
	public static boolean isFuture(Date date)
	{
		return date.after(new Date());
	}

	/**
	 * Returns whether a date is within a specified date range
	 * 
	 * @param date
	 *            the date to check
	 * @param startDate
	 *            the start date of the date range
	 * @param endDate
	 *            the end date of the date range
	 * @return <code>true</code> if the specified date is withing the specified date range; <code>false</code> otherwise
	 */
	public static boolean isInRange(Date date, Date startDate, Date endDate)
	{
		return (date.equals(startDate) || date.after(startDate)) && (date.equals(endDate) || date.before(endDate));
	}

	/**
	 * Returns whether the specified date is in the past
	 * 
	 * @param date
	 *            the date
	 * @return <code>true</code> if the specified date is in the past, <code>false</code> otherwise
	 */
	public static boolean isPast(Date date)
	{
		return date.before(new Date());
	}

	public static boolean isToday(Date date)
	{
		if(date == null)
		{
			return false;
		}
		return getTrimmedToDayDate(date).equals(getTrimmedToDayDate(new Date()));
	}

	/**
	 * Returns whether a specified calendar is a weekend day
	 * 
	 * @param date
	 *            the date
	 * @return <code>true</code> if the specified calendar is a weekend day; <code>false</code> otherwise
	 */
	public static boolean isWeekend(Calendar calendar)
	{
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
	}

	/**
	 * Returns whether a specified date is a weekend day
	 * 
	 * @param date
	 *            the date
	 * @return <code>true</code> if the specified date is a weekend day; <code>false</code> otherwise
	 */
	public static boolean isWeekend(Date date)
	{
		return isWeekend(getCalendar(date));
	}

	public static void main(String[] args)
	{
		Calendar calendar = getCalendar(new Date());
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		Date start = calendar.getTime();
		calendar.set(Calendar.MONTH, Calendar.OCTOBER);
		calendar.set(Calendar.DAY_OF_MONTH, 31);
		System.err.println(getDaysBetween(start, calendar.getTime()));
	}

	public static Calendar trimToDay(Calendar calendar)
	{
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}

	private static Calendar trimToDayLastMillisecond(Calendar calendar)
	{
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar;
	}

	public Date getCurrentDate()
	{
		return new Date();
	}

	public Integer getCurrentYear()
	{
		return getYearFromDate(new Date());
	}
}
