package com.liugeng.zkutils.alarm;

import static com.liugeng.zkutils.alarm.AlarmUtils.*;
import static com.liugeng.zkutils.utils.CommonUtils.*;
import static org.apache.curator.utils.ZKPaths.*;

import java.util.Random;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liugeng.zkutils.exception.AlarmException;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/21 13:11
 */
public class AlarmPublisherImpl implements AlarmPublisher {

	private final String rootPath;
	private final CuratorFramework zkClient;
	private static final Random random = new Random();
	private static final Logger log = LoggerFactory.getLogger(AlarmPublisher.class);
	
	public AlarmPublisherImpl(String rootPath, CuratorFramework zkClient) {
		checkValid(rootPath);
		checkClient(zkClient);
		this.rootPath = rootPath;
		this.zkClient = zkClient;
	}
	
	@Override
	public void alarm(String alarmKey) {
		String path = makePath(rootPath, alarmKey);
		try {
			ZKPaths.mkdirs(zkClient.getZookeeperClient().getZooKeeper(), path);
			zkClient.setData().forPath(path, makeRandomData());
		} catch (Exception e) {
			log.error("failed to set alarm on path: {}", path);
			throw new AlarmException("failed to set alarm on path: " + path, e, path);
		}
	}
	
	private byte[] makeRandomData() {
		return String.valueOf(random.nextLong()).getBytes();
	}
}
