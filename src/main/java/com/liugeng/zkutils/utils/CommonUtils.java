package com.liugeng.zkutils.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.RetryNTimes;

import com.google.common.base.Preconditions;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/21 10:19
 */
public class CommonUtils {
	
	public static void checkClient(CuratorFramework zkClient) {
		Preconditions.checkNotNull(zkClient, "CuratorFramework client should not be null!");
		synchronized (CommonUtils.class) {
			if (!CuratorFrameworkState.STARTED.equals(zkClient.getState())) {
				zkClient.start();
			}
		}
	}
	
	public static CuratorFramework makeClient(String zkAddress) throws Exception {
		return CuratorFrameworkFactory.builder()
			.retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 2000))
			.sessionTimeoutMs(60000)
			.connectString(zkAddress)
			.build();
	}
}
