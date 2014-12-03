package com.proclos.colibriweb.session.system;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TypeConversionUtil
{

	public static Class<?> lookupPrimitive(String name) {
		if (name.equalsIgnoreCase("boolean")) return Boolean.class;
		if (name.equalsIgnoreCase("int")) return Integer.class;
		if (name.equalsIgnoreCase("double")) return Double.class;
		if (name.equalsIgnoreCase("long")) return Long.class;
		if (name.equalsIgnoreCase("short")) return Short.class;
		if (name.equalsIgnoreCase("byte")) return Byte.class;
		if (name.equalsIgnoreCase("float")) return Float.class;
		return null;
	}
	
	/**
	 * converts the value of the input column to an object of the type defined by {@link IColumn#getValueType()}
	 * 
	 * @param column
	 *            the input column
	 * @return an object of the type defined by {@link IColumn#getValueType()}
	 * @throws FunctionException
	 */
	public static Object convert(Object value, Class<?> clazz) throws Exception
	{
		if(value == null) return null;
		if (clazz.isPrimitive()) clazz = lookupPrimitive(clazz.getName());
		if(clazz.isInstance(value)) // value is of type class or can be cast directly. Everything is fine.
		return clazz.cast(value);
		if(clazz.equals(String.class)) return value.toString();
		if(clazz.equals(Boolean.class)) return value.toString().equalsIgnoreCase("true");
		if(clazz.equals(Double.class)) return Double.valueOf(convertToNumeric(value.toString())); // recognize numbers like 55,3 as a double
		if(clazz.equals(Integer.class)) return Integer.valueOf(Double.valueOf(convertToNumeric(value.toString().trim())).intValue());
		if(clazz.equals(Long.class)) return Long.valueOf(Double.valueOf(convertToNumeric(value.toString().trim())).longValue());
		if(clazz.equals(Byte.class)) return Byte.valueOf(Double.valueOf(convertToNumeric(value.toString().trim())).byteValue());
		if(clazz.equals(Float.class)) return Float.valueOf(convertToNumeric(value.toString()));
		if(clazz.equals(Short.class)) return Short.valueOf(Double.valueOf(convertToNumeric(value.toString().trim())).shortValue());
		if(clazz.equals(BigInteger.class)) return new BigInteger(value.toString());
		if(clazz.equals(BigDecimal.class)) return new BigDecimal(value.toString());
		if(clazz.equals(Date.class.getCanonicalName()))
		{
			SimpleDateFormat in = new SimpleDateFormat("dd.MM.yyyy");
			return in.parse(value.toString());
		}
		return value;
	}

	/**
	 * fast test if a given input data is numeric or not
	 * 
	 * @param inputData
	 *            the data to be tested for being numeric
	 * @return true, if numeric, false otherwise.
	 */
	public static boolean isNumeric(String inputData)
	{
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(inputData, pos);
		return inputData.length() == pos.getIndex();
	}

	/**
	 * converts input data to a numerical format with "." as decimal separator and without grouping separator e.g. 1.234,56 to 1234.56 Default value is "0" for initial inputs
	 * 
	 * @param inputData
	 *            the data to be converted
	 * @return the converted string
	 */
	public static String convertToNumeric(String inputData)
	{
		if(inputData == null) return "0";
		if(inputData.equalsIgnoreCase(Boolean.TRUE.toString())) return "1";
		if(inputData.equalsIgnoreCase(Boolean.FALSE.toString())) return "0";
		if(inputData.isEmpty()) return "0";
		if(inputData.contains(",") && inputData.indexOf(',') != inputData.length() - 1 && inputData.indexOf(',') == inputData.lastIndexOf(','))
		{
			// replace the group separator
			// replace the comma with dot so that java will recognize the string as double
			return inputData.replaceAll("\\.", "").replace(",", ".");
		}
		else return inputData;
	}
}

