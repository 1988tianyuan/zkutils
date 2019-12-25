package com.liugeng.zkutils.exception;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/21 11:27
 */
public class AlarmException extends RuntimeException {
	
	private String path;
	
	public AlarmException(String message, Throwable cause, String path) {
		super(message, cause);
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
}
