package com.boot.utils;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class NumberUtil {

	private static Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

	/**
	 * @param bigdecial to test
	 * @return whether passed bigdecimal is integer or not
	 * 
	 */
	public static boolean isIntegerValue(BigDecimal bd) {
		if (bd == null) {
			return false;
		}
		return bd.stripTrailingZeros().scale() <= 0;
	}

	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		return pattern.matcher(strNum).matches();
	}

}
