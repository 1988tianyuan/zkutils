package com.liugeng.zkutils.lock;

import static com.liugeng.zkutils.utils.CommonUtils.*;

import java.util.Collection;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.Revoker;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/26 14:47
 */
public class RevokerTest {
	
	private static final String zkAddress = "10.106.151.187:2181";
	
	public static void main(String[] args) throws Exception {
		CuratorFramework client = makeClient(zkAddress);
		client.start();
		final InterProcessMutex lock = new InterProcessMutex(client, "/test/lock");
		Collection<String> ps = lock.getParticipantNodes();
		for (String p : ps) {
			Revoker.attemptRevoke(client, p);
		}
	}
}
