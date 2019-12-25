package com.liugeng.zkutils.alarm;

import org.apache.curator.framework.CuratorFramework;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/21 20:17
 */
public class AlarmFactory {
	
	public static AlarmPublisher defaultPublisher(String rootPath, CuratorFramework zkClient) {
		return new AlarmPublisherImpl(rootPath, zkClient);
	}
	
	public static AlarmSubscriber defaultSubscriber(String rootPath, CuratorFramework zkClient) {
		return new AlarmSubscriberImpl(rootPath, zkClient);
	}
}
