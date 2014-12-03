package at.adaptive.components.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import at.adaptive.components.common.StringUtil;

public class AssociationPath implements Serializable
{
	private List<String> entries = new ArrayList<String>(1);
	private static final char PROPERTY_SEPARATOR = '.';
	private String group;

	public String getGroup()
	{
		return group;
	}

	public void setGroup(String group)
	{
		this.group = group;
	}

	public AssociationPath()
	{
		addEntry(CriteriaWrapper.CRITERIA_ROOT_NAME);
	}

	public AssociationPath(String... entries)
	{
		this();
		this.entries = Arrays.asList(entries);
	}

	public AssociationPath(String entry)
	{
		this();
		addEntries(entry, null);
	}

	public void addEntry(String entry)
	{
		entries.add(entry);
	}

	public List<String> getEntries()
	{
		return entries;
	}

	public void addEntries(String entriesToSplit, List<String> embeddedProperties)
	{
		TreeMap<Integer, String> propertyMap = new TreeMap<Integer, String>();
		String originalEntriesToSplit = entriesToSplit;
		if(embeddedProperties != null)
		{
			for(String embeddedProperty : embeddedProperties)
			{
				if(entriesToSplit.contains(embeddedProperty))
				{
					// get position
					int index = originalEntriesToSplit.indexOf(embeddedProperty);
					String subString = originalEntriesToSplit.substring(0, index);
					int separatorCount = StringUtil.countChars(subString, PROPERTY_SEPARATOR);
					index = entriesToSplit.indexOf(embeddedProperty);
					propertyMap.put(separatorCount, embeddedProperty);
					String prefix = entriesToSplit.substring(0, index);
					String suffix;
					if(entriesToSplit.length() > (index + 1 + embeddedProperty.length()))
					{
						suffix = entriesToSplit.substring(index + 1 + embeddedProperty.length());
					}
					else
					{
						suffix = "";
					}
					entriesToSplit = prefix.concat(suffix);
				}
			}
		}
		String[] splits = entriesToSplit.split("\\" + PROPERTY_SEPARATOR);
		int index = 0;
		for(int i = 0; i < splits.length; i++)
		{
			String split = splits[i];
			if(StringUtil.isEmpty(split))
			{
				continue;
			}
			while(propertyMap.get(index) != null)
			{
				index++;
			}
			propertyMap.put(index, splits[i]);
		}
		for(Iterator<Integer> iterator = propertyMap.keySet().iterator(); iterator.hasNext();)
		{
			addEntry(propertyMap.get(iterator.next()));
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(Iterator<String> iterator = iterator(); iterator.hasNext();)
		{
			sb.append(iterator.next());
			if(iterator.hasNext())
			{
				sb.append(".");
			}
		}
		return sb.toString();
	}

	public Iterator<String> iterator()
	{
		return entries.iterator();
	}
}
