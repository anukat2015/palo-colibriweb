package at.adaptive.components.datahandling.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XLSFormulaParser
{
	private static Pattern cellLinkPattern = Pattern.compile("\\$?([A-Za-z]{1,2})\\$?([\\d]+)");

	public static String parseFormula(String formula, int currentLine, int writtenLines, int firstLine, boolean replaceFixedCellLinks)
	{
		Matcher matcher = cellLinkPattern.matcher(formula);
		while(matcher.find())
		{
			try
			{
				String match = matcher.group();
				boolean isFixedCellLink = (match.indexOf("$") >= 0);
				String column = matcher.group(1);
				int lineInFormula = Integer.parseInt(matcher.group(2));
				int diff = currentLine - (firstLine + 1);
				int lineToSet;
				int beginOffset = matcher.start();
				int endOffset = matcher.end();
				StringBuilder sb = new StringBuilder();
				sb.append(formula.substring(0, beginOffset));
				String subFormula = formula.substring(beginOffset, endOffset);
				int len1 = subFormula.length();
				String tail = formula.substring(endOffset);
				if(isFixedCellLink && replaceFixedCellLinks)
				{
					lineToSet = lineInFormula + (writtenLines - 1);
					subFormula = subFormula.replaceAll(String.valueOf(lineInFormula), String.valueOf(lineToSet));
					// subFormula = util.substitute("s/\\$" + lineInFormula + "/\\$" + lineToSet + "/g", subFormula);
				}
				else
				{
					if(diff == 0)
					{
						continue;
					}
					lineToSet = lineInFormula + diff;
					// subFormula = util.substitute("s/" + match + "/" + column + lineToSet + "/g", subFormula);
					subFormula = subFormula.replaceAll(match, String.valueOf(column + lineToSet));
				}
				sb.append(subFormula);
				sb.append(tail);
				formula = sb.toString();
				int len2 = subFormula.length();
				int newBeginOffset = endOffset + (len2 - len1);
				// int newLength = formula.length() - newBeginOffset;
				// matcher.setInput(formula, newBeginOffset, newLength);
				matcher.reset(formula);
				matcher = matcher.region(newBeginOffset, formula.length());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return formula;
	}

	public static String parseSumFormula(String formula, int writtenLines, int firstLine, int sumLine, int offsetCorrection)
	{
		String modifiedFormulaString = formula.replaceAll("\\$", "");
		Matcher matcher = cellLinkPattern.matcher(modifiedFormulaString);
		StringBuilder sb = new StringBuilder();
		int index = 0;
		boolean nextRangeUpdate = false;
		while(matcher.find())
		{
			int start1 = matcher.start(2);
			int end1 = matcher.end(2);
			int value = Integer.parseInt(matcher.group(2));
			if((value - 1) > firstLine)
			{
				if(value == sumLine)
				{
					value = value + writtenLines - 1;
					value = value + offsetCorrection;
				}
				else
				{
					value = value + writtenLines - 1;
				}
			}
			else if((value - 1) == firstLine && nextRangeUpdate)
			{
				value = value + writtenLines - 1;
				nextRangeUpdate = false;
			}
			else if((value - 1) == firstLine)
			{
				nextRangeUpdate = true;
			}
			sb.append(modifiedFormulaString.substring(index, start1));
			sb.append(String.valueOf(value));
			index = matcher.end(0);
			sb.append(modifiedFormulaString.substring(end1, index));
		}
		sb.append(modifiedFormulaString.substring(index));
		return sb.toString();
	}
}
