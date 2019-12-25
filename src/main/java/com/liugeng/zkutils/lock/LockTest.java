package com.liugeng.zkutils.lock;

import static com.liugeng.zkutils.utils.CommonUtils.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/26 14:28
 */
public class LockTest {
	
	private static final String zkAddress = "10.106.151.187:2181";
	
	public static void main(String[] args) throws Exception {
		CuratorFramework client = makeClient(zkAddress);
		client.start();
		final CountDownLatch latch = new CountDownLatch(1);
		final InterProcessMutex lock = new InterProcessMutex(client, "/test/lock");
		lock.makeRevocable(forLock -> {
			System.out.println("this lock should be released.");
			try {
				latch.countDown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
			@Override
			public void stateChanged(CuratorFramework client, ConnectionState newState) {
				switch (newState) {
					case SUSPENDED:
					case LOST:
						System.out.println("zk connection is lost, release the lock");
						latch.countDown();
				}
			}
		});
		lock.acquire();
		System.out.println("acquired lock, time is " + SimpleDateFormat.getInstance().format(new Date()));
		latch.await(60, TimeUnit.SECONDS);
		try {
			lock.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("release lock, time is " + SimpleDateFormat.getInstance().format(new Date()));
		client.close();
	}
}
