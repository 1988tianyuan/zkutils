package com.liugeng.zkutils.utils;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/18 18:00
 */
public class Assert {
	
	public static void isTrue(boolean expression, String message) {
		if (!expression) {
			throw new IllegalArgumentException(message);
		}
	}
	
	public static void notNull(Object object, String message) {
		if (object == null) {
			throw new IllegalArgumentException(message);
		}
	}
}
