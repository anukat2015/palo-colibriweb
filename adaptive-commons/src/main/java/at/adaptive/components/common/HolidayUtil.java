package at.adaptive.components.common;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Calculate austrian holidays. Valid since the year 1584.
 */
public class HolidayUtil
{
	private final int yearMap;
	private final Map<Calendar, String> holidaysMap = new TreeMap<Calendar, String>();

	/**
	 * Get the date representing easter sunday
	 * 
	 * @return the date representing the easter sunday in the current year
	 */
	private Calendar getEasterSunday()
	{
		int a = yearMap % 19;
		int b = yearMap / 100;
		int c = yearMap % 100;
		int d = b / 4;
		int e = b % 4;
		int f = (b + 8) / 25;
		int g = (b - f + 1) / 3;
		int h = (19 * a + b - d - g + 15) % 30;
		int i = c / 4;
		int k = c % 4;
		int l = (32 + 2 * e + 2 * i - h - k) % 7;
		int m = (a + 11 * h + 22 * l) / 451;
		int n = (h + l - 7 * m + 114) / 31;
		int p = (h + l - 7 * m + 114) % 31 + 1;
		return new GregorianCalendar(yearMap, n - 1, p);
	}

	/**
	 * Add a new holiday
	 * 
	 * @param nDay
	 *            the day: 1-31
	 * @param nMonth
	 *            the month: 1-12
	 * @param sWhich
	 *            name of the holiday
	 */
	private void addHoliday(int nDay, int nMonth, String sWhich)
	{
		Calendar calendar = new GregorianCalendar(yearMap, nMonth - 1, nDay);
		calendar = DateUtil.trimToDay(calendar);
		holidaysMap.put(calendar, sWhich);
	}

	/**
	 * Add a new holiday
	 * 
	 * @param aCal
	 *            the date to add
	 * @param sWhich
	 *            name of the holiday
	 */
	private void addHoliday(Calendar aCal, String sWhich)
	{
		aCal = DateUtil.trimToDay(aCal);
		holidaysMap.put(aCal, sWhich);
	}

	/**
	 * Helper function to add/subtract days from a given date
	 * 
	 * @param c
	 *            the source calendar
	 * @param nDiffDays
	 *            the number of days to roll (signed!)
	 * @return the resulting calendar
	 */
	private Calendar addDays(Calendar c, int nDiffDays)
	{
		Calendar c2 = (Calendar)c.clone();
		c2.add(Calendar.DAY_OF_YEAR, nDiffDays);
		return c2;
	}

	public static Map<Calendar, String> getHolidaysMap(int year)
	{
		HolidayUtil holidayUtil = new HolidayUtil(year);
		return holidayUtil.holidaysMap;
	}

	public static Map<Calendar, String> getHolidaysMap(Date startDate, Date endDate)
	{
		int startYear = DateUtil.getYearFromDate(startDate);
		int endYear = DateUtil.getYearFromDate(endDate);
		int years = endYear - startYear;
		Map<Calendar, String> holidaysMap = new HashMap<Calendar, String>();
		for(int i = 0; i <= years; i++)
		{
			holidaysMap.putAll(HolidayUtil.getHolidaysMap(startYear + i));
		}
		return holidaysMap;
	}

	private HolidayUtil(int year)
	{
		if(year <= 1583) throw new IllegalArgumentException("Das Jahr muss > als 1583 sein!");
		yearMap = year;
		// Add fixed holidays
		addHoliday(1, 1, "Neujahr");
		addHoliday(6, 1, "Heilige 3 Könige");
		addHoliday(1, 5, "Tag der Arbeit");
		addHoliday(15, 8, "Maria Himmelfahrt");
		// _addHoliday (3, 10, "Tag der deutschen Einheit");
		addHoliday(26, 10, "Nationalfeiertag");
		// _addHoliday (31, 10, "Reformationstag");
		addHoliday(1, 11, "Allerheiligen");
		addHoliday(8, 12, "Maria Empfängnis");
		// addHoliday(24, 12, "Heiligabend");
		addHoliday(25, 12, "Christtag");
		addHoliday(26, 12, "Stefanitag");
		// addHoliday(31, 12, "Silvester");
		// Add holidays relative to easter sunday
		final Calendar aEaster = getEasterSunday();
		addHoliday(aEaster, "Ostersonntag");
		// _addHoliday (_addDays (aEaster, -2), "Karfreitag");
		addHoliday(addDays(aEaster, 1), "Ostermontag");
		addHoliday(addDays(aEaster, 39), "Christi Himmelfahrt");
		addHoliday(addDays(aEaster, 49), "Pfingstsonntag");
		addHoliday(addDays(aEaster, 50), "Pfingstmontag");
		addHoliday(addDays(aEaster, 60), "Fronleichnam");
		// if(false)
		// {
		// // Gets 3rd Wednesday in November
		// final Calendar aBBDay = new GregorianCalendar(m_nYear, 11 - 1, 1);
		// _addHoliday(_addDays(aBBDay, ((11 - aBBDay.get(Calendar.DAY_OF_WEEK)) % 7) + 14), "Buß- und Bettag");
		// }
	}

	public static Integer getNumberOfWorkdays(Date startDate, Date endDate)
	{
		return getNumberOfWorkdays(startDate, endDate, null);
	}

	public static Integer getNumberOfWorkdays(Date startDate, Date endDate, List<DateRange> dateRanges)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		HolidayUtil holidayUtil = new HolidayUtil(calendar.get(Calendar.YEAR));
		Map<Calendar, String> holidaysMap = holidayUtil.holidaysMap;
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(startDate);
		startCal = DateUtil.trimToDay(startCal);
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(endDate);
		endCal = DateUtil.trimToDay(endCal);
		int workDays = 0;
		if(startCal.getTimeInMillis() > endCal.getTimeInMillis())
		{
			// startCal.setTime(endDate);
			// endCal.setTime(startDate);
			return 0;
		}
		while(startCal.getTimeInMillis() <= endCal.getTimeInMillis())
		{
			if((startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) && !holidaysMap.containsKey(startCal) && inDateRange(dateRanges, startCal.getTime()))
			{
				workDays++;
			}
			startCal.add(Calendar.DAY_OF_MONTH, 1);
		}
		return workDays;
	}

	private static boolean inDateRange(List<DateRange> dateRanges, Date date)
	{
		if(dateRanges == null)
		{
			return true;
		}
		for(DateRange dateRange : dateRanges)
		{
			if(dateRange.isInRange(date))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean isHoliday(Date date)
	{
		return getHolidayValue(date) != null;
	}

	public static String getHolidayValue(Date date)
	{
		if(date != null)
		{
			Map<Calendar, String> holidayMap = getHolidaysMap(DateUtil.getYearFromDate(date));
			return holidayMap.get(DateUtil.getTrimmedToDayCalendar(date));
		}
		return null;
	}
}