package com.liugeng.zkutils.alarm;

import com.google.common.base.Preconditions;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/21 17:48
 */
public class AlarmUtils {
	
	public static void checkValid(String rootPath) {
		Preconditions.checkNotNull(rootPath, "rootPath should not be null.");
		Preconditions.checkArgument(
			rootPath.startsWith("/") && !rootPath.endsWith("/"),
			"Invalid rootPath, should begin with '/' and not end with '/'."
		);
	}
}
