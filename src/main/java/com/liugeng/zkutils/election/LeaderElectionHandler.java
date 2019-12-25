package com.liugeng.zkutils.election;

import static com.liugeng.zkutils.utils.CommonUtils.*;
import static com.liugeng.zkutils.utils.Constants.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.recipes.leader.Participant;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liugeng.zkutils.participants.ParticipantsChangeListener;
import com.liugeng.zkutils.utils.Assert;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/18 17:37
 */
public class LeaderElectionHandler extends LeaderSelectorListenerAdapter implements LeaderElection {
	
	private static final Logger log = LoggerFactory.getLogger(LeaderElectionHandler.class);
	
	private final String uuidName = UUID.randomUUID().toString();
	
	private volatile ElectionStatus status = ElectionStatus.READY;
	
	private boolean autoRelease = false;
	
	private final LeaderSelector leaderSelector;
	
	private final LeaderElectedWatcher leaderElectedWatcher;
	
	private final CuratorFramework zkClient;
	
	private volatile CountDownLatch leaderLock;
	
	private String participantId = uuidName;
	
	private String namespace;
	
	private ParticipantsChangeListener participantsChangeListener;
	
	private ConnectChangeListener connectChangeListener = new ConnectChangeListener();
	
	private volatile Set<String> participants;
	
	@Override
	public void takeLeadership(CuratorFramework zkClient) throws Exception {
		try {
			status = ElectionStatus.ELECTED;
			log.info("current instance has been elected as leader, begin leaderTask.");
			doLeaderTask();
			if (!autoRelease) {
				leaderLock.await();
			}
		} finally {
			stopLeaderTask();
		}
	}
	
	@Override
	public String start(boolean autoRelease) throws Exception {
		Assert.isTrue(status.equals(ElectionStatus.READY), "LeaderElectionHandler is already started or stopped.");
		status = ElectionStatus.NOT_ELECTED;
		this.autoRelease = autoRelease;
		zkClient.getConnectionStateListenable().addListener(connectChangeListener);
		leaderLock = new CountDownLatch(1);
		leaderSelector.setId(participantId);
		leaderSelector.autoRequeue();
		leaderSelector.start();
		attachParticipantWatcher();
		return leaderSelector.getId();
	}
	
	@Override
	public String start(boolean autoRelease, String instanceName) throws Exception {
		this.participantId = instanceName + "_" + uuidName;
		return start(autoRelease);
	}
	
	@Override
	public void release() { 
		if (leaderLock != null && leaderLock.getCount() > 0) {
			leaderLock.countDown();
			leaderLock = new CountDownLatch(1);
		}
	}
	
	@Override
	public void stop() {
		log.info("stop LeaderElectionHandler and finish leaderTask.");
		try {
			stopLeaderTask();
			release();
			status = ElectionStatus.STOPPED;
		} finally {
			zkClient.getConnectionStateListenable().removeListener(connectChangeListener);
			leaderSelector.close();
		}
	}
	
	/* refresh participant list and call user's participantsChangeListener */
	public Set<String> refreshParticipants() throws Exception {
		internalParticipants();
		return participants;
	}
	
	public LeaderElectionHandler(LeaderElectedWatcher leaderElectedWatcher, CuratorFramework zkClient, String namespace, 
		ParticipantsChangeListener listener) throws Exception {
		this(leaderElectedWatcher, zkClient, namespace);
		participantsChangeListener = listener;
	}
	
	public LeaderElectionHandler(LeaderElectedWatcher leaderElectedWatcher, CuratorFramework zkClient, String namespace) throws
		Exception {
		Assert.notNull(leaderElectedWatcher, "leaderElectedWatcher should not be null!");
		Assert.notNull(namespace, "namespace should not be null!");
		checkClient(zkClient);
		this.namespace = namespace + DEFAULT_ELECTION_NAMESPACE;
		this.leaderElectedWatcher = leaderElectedWatcher;
		this.zkClient = zkClient;
		ZKPaths.mkdirs(zkClient.getZookeeperClient().getZooKeeper(), this.namespace);
		leaderSelector = new LeaderSelector(zkClient, this.namespace, this);
		leaderSelector.autoRequeue();
	}
	
	private class ConnectChangeListener implements ConnectionStateListener {
		@Override
		public void stateChanged(CuratorFramework client, ConnectionState newState) {
			switch (newState) {
				case SUSPENDED:
				case LOST:
					/* release leaderShip after connection loss */
					release();
					if (isLeader()) {
						log.info("leader disconnected from zookeeper, change to status: {}", ElectionStatus.NOT_ELECTED);
						stopLeaderTask();
					}
					break;
				case RECONNECTED:
					try {
						attachParticipantWatcher(); // refresh participants after reconnected
					} catch (Exception e) {
						log.error("failed to get participants.", e);
					}
			}
		}
	}
	
	private void attachParticipantWatcher() throws Exception {
		zkClient.getChildren().usingWatcher(participantsChangeWatcher).forPath(namespace);
	}
	
	private void internalParticipants() throws Exception {
		attachParticipantWatcher();
		Collection<Participant> pList = leaderSelector.getParticipants();
		Set<String> participantIds = new HashSet<String>();
		for (Participant p : pList) {
			participantIds.add(p.getId());
		}
		this.participants = participantIds;
		log.info("fetch participants list successfully: {}", participantIds);
		if (participantsChangeListener != null) {
			participantsChangeListener.onParticipantsChange(participantIds);
		}
	}
	
	private CuratorWatcher participantsChangeWatcher = new CuratorWatcher() {
		@Override
		public void process(final WatchedEvent e) throws Exception {
			if(Watcher.Event.EventType.NodeChildrenChanged.equals(e.getType())) {
				internalParticipants();
			}
		}
	};
	
	@Override
	public boolean isLeader() {
		return ElectionStatus.ELECTED.equals(status);
	}
	
	@Override
	public Set<String> getParticipants() {
		return participants;
	}
	
	@Override
	public String getParticipantId() {
		return participantId;
	}
	
	@Override
	public String getNamespace() {
		return namespace;
	}
	
	private void doLeaderTask() {
		leaderElectedWatcher.doTask();
	}
	
	private synchronized void stopLeaderTask() {
		if (isLeader()) {
			leaderElectedWatcher.stopTask();
			status = ElectionStatus.NOT_ELECTED;
		}
	}
	
	public enum ElectionStatus {
		READY, ELECTED, NOT_ELECTED, STOPPED
	}
}
