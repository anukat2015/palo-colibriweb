package at.adaptive.components.geo;

import java.text.Collator;
import java.util.Comparator;

import at.adaptive.components.common.LocaleUtil;

public class GeoItemComparator implements Comparator<GeoItem>
{
	private Collator collator = Collator.getInstance(LocaleUtil.LOCALE_AT);

	public int compare(GeoItem o1, GeoItem o2)
	{
		int i = compareStrings(o1.getLocality(), o2.getLocality());
		if(i != 0)
		{
			return i;
		}
		return compareStrings(o1.getAddress(), o2.getAddress());
	}

	private int compareStrings(String str1, String str2)
	{
		if(str1 == null && str2 == null)
		{
			return 0;
		}
		if(str1 != null && str2 == null)
		{
			return 1;
		}
		if(str1 == null && str2 != null)
		{
			return -1;
		}
		return collator.compare(str1, str2);
	}

}
