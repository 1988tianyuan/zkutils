package com.liugeng.zkutils.election;


/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/14 17:44
 */
public interface LeaderElectedWatcher {
	
	void doTask();

	void stopTask();
}
