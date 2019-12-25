package com.liugeng.zkutils.example.alarm;

import static com.liugeng.zkutils.utils.CommonUtils.*;

import java.util.concurrent.CountDownLatch;

import com.liugeng.zkutils.alarm.AlarmFactory;
import com.liugeng.zkutils.alarm.AlarmSubscriber;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/26 14:19
 */
public class AlarmSubscribeExample {
	
	private static final String zkAddress = "10.106.151.187:2181";
	
	public static void main(String[] args) throws Exception {
		final String alarmKey = "test-key";
		final CountDownLatch latch = new CountDownLatch(1);
		AlarmSubscriber subscriber = AlarmFactory.defaultSubscriber("/test/alarm", makeClient(zkAddress));
		subscriber.setWatcher(alarmKey, () -> {
			latch.countDown();
			System.out.println("receive alarm, key is：" + alarmKey);
		});
		latch.await();
	}
}
