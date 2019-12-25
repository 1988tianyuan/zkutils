package com.liugeng.zkutils.election;

import org.apache.curator.framework.CuratorFramework;

import com.liugeng.zkutils.participants.ParticipantsChangeListener;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/18 17:38
 */
public class LeaderElectionFactory {
	
	public static LeaderElection createHandler(LeaderElectedWatcher leaderElectedWatcher, CuratorFramework client,
		String namespace) throws Exception {
		return new LeaderElectionHandler(leaderElectedWatcher, client, namespace);
	}
	
	public static LeaderElection createHandler(LeaderElectedWatcher leaderElectedWatcher, CuratorFramework client,
		String namespace, ParticipantsChangeListener listener) throws Exception {
		return new LeaderElectionHandler(leaderElectedWatcher, client, namespace, listener);
	}
	
	public static LeaderElection createHandler(LeaderElectedWatcher leaderElectedWatcher, CuratorFramework client) throws
		Exception {
		return new LeaderElectionHandler(leaderElectedWatcher, client, "");
	}
}
