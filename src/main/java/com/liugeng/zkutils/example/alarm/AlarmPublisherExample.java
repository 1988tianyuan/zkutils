package com.liugeng.zkutils.example.alarm;

import static com.liugeng.zkutils.utils.CommonUtils.*;

import com.liugeng.zkutils.alarm.AlarmFactory;
import com.liugeng.zkutils.alarm.AlarmPublisher;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/26 14:16
 */
public class AlarmPublisherExample {
	
	private static final String zkAddress = "10.106.151.187:2181";
	
	public static void main(String[] args) throws Exception {
		AlarmPublisher publisher = AlarmFactory.defaultPublisher("/test/alarm", makeClient(zkAddress));
		publisher.alarm("test-key");
	}
}
