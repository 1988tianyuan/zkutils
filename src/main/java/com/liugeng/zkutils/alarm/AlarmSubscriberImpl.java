package com.liugeng.zkutils.alarm;

import static com.liugeng.zkutils.alarm.AlarmUtils.*;
import static com.liugeng.zkutils.utils.CommonUtils.*;
import static org.apache.curator.utils.ZKPaths.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liugeng.zkutils.exception.AlarmException;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/21 9:47
 */
public class AlarmSubscriberImpl implements AlarmSubscriber, ConnectionStateListener {
	
	private final String rootPath;
	private final CuratorFramework zkClient;
	private final ConcurrentHashMap<String, AlarmCuratorWatcher> watcherMap = new ConcurrentHashMap<String, AlarmCuratorWatcher>();
	private static final Logger log = LoggerFactory.getLogger(AlarmSubscriber.class);
	
	public AlarmSubscriberImpl(String rootPath, CuratorFramework zkClient) {
		checkValid(rootPath);
		checkClient(zkClient);
		this.rootPath = rootPath;
		this.zkClient = zkClient;
		this.zkClient.getConnectionStateListenable().addListener(this);
	}
	
	@Override
	public void setWatcher(String alarmKey, AlarmWatcher alarmWatcher) {
		String path = makePath(rootPath, alarmKey);
		Watcher watcher = putWatcher(path, alarmWatcher);
		internalSetWatch(path, watcher);
	}
	
	private Watcher putWatcher(String path, AlarmWatcher alarmWatcher) {
		AlarmCuratorWatcher watcher = watcherMap.get(path);
		if (watcher != null) {
			watcher.setWatcher(alarmWatcher);
		} else {
			watcher = new AlarmCuratorWatcher(path, alarmWatcher);
			watcherMap.put(path, watcher);
		}
		return watcher;
	}
	
	@Override
	public void removeWatcher(String alarmKey) {
		String path = makePath(rootPath, alarmKey);
		AlarmCuratorWatcher watcher = watcherMap.remove(path);
		if (watcher != null) {
			watcher.setNeedWatch(false);
			zkClient.clearWatcherReferences(watcher);
		}
	}
	
	private void internalSetWatch(final String path, final Watcher watcher) {
		try {
			ZKPaths.mkdirs(zkClient.getZookeeperClient().getZooKeeper(), path);
			zkClient.getData().usingWatcher(watcher).forPath(path);
		} catch (Exception e) {
			log.error("failed to set alarm watcher: {}", e.getMessage());
			throw new AlarmException("failed to set alarm watcher, the path is " + path, e, path);
		}
	}
	
	private class AlarmCuratorWatcher implements Watcher {
		
		private String path;
		private AlarmWatcher alarmWatcher;
		private volatile boolean needWatch = true;
		
		public AlarmCuratorWatcher(String path, AlarmWatcher watcher) {
			this.path = path;
			this.alarmWatcher = watcher;
		}
		
		@Override
		public void process(WatchedEvent event) {
			Event.EventType eventType = event.getType();
			if (needWatch) {
				if (Event.EventType.NodeDataChanged.equals(eventType)) {
					alarmWatcher.process();
				}
				internalSetWatch(path, this);
			}
		}
		
		public boolean isNeedWatch() {
			return needWatch;
		}
		
		public void setNeedWatch(boolean needWatch) {
			this.needWatch = needWatch;
		}
		
		public String getPath() {
			return path;
		}
		
		public void setPath(String path) {
			this.path = path;
		}
		
		public AlarmWatcher getWatcher() {
			return alarmWatcher;
		}
		
		public void setWatcher(AlarmWatcher watcher) {
			this.alarmWatcher = watcher;
		}
	}
	
	@Override
	public void stateChanged(CuratorFramework client, ConnectionState newState) {
		if (newState == ConnectionState.RECONNECTED && !watcherMap.isEmpty()) {
			// reset all the watchers after re-connected
			for (Map.Entry<String, AlarmCuratorWatcher> item : watcherMap.entrySet()) {
				try {
					zkClient.getData().usingWatcher(item.getValue()).forPath(item.getKey());
				} catch (Exception e) {
					log.error("failed to re-set watcher for path: {}.", item.getKey(), e);
				}
			}
		}
	}
}
