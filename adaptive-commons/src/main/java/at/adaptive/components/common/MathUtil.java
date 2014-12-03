package at.adaptive.components.common;

import java.math.BigDecimal;

public class MathUtil
{
	public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, int scale) throws ArithmeticException, IllegalArgumentException
	{
		if(dividend == null || divisor == null)
		{
			return null;
		}
		return dividend.divide(divisor, scale, BigDecimal.ROUND_HALF_UP);
	}

	public static Double divide(Double dividend, Double divisor, int scale) throws ArithmeticException, IllegalArgumentException
	{
		if(dividend == null || divisor == null)
		{
			return null;
		}
		return divide(new BigDecimal(dividend), new BigDecimal(divisor), scale).doubleValue();
	}
}
