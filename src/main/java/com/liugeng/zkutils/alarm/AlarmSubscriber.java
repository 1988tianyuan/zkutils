package com.liugeng.zkutils.alarm;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/20 21:26
 */
public interface AlarmSubscriber {
	
	void setWatcher(String alarmKey, AlarmWatcher watcher);
	
	void removeWatcher(String alarmKey);
}
