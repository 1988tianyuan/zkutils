package com.liugeng.zkutils.example.election;

import static com.liugeng.zkutils.utils.CommonUtils.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.apache.curator.framework.CuratorFramework;

import com.liugeng.zkutils.election.LeaderElectedWatcher;
import com.liugeng.zkutils.election.LeaderElection;
import com.liugeng.zkutils.election.LeaderElectionFactory;
import com.liugeng.zkutils.participants.ParticipantsChangeListener;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/25 16:29
 */
public class GetParticipantListExample {
	
	private static final String zkAddress = "10.106.151.187:2181";
	
	public static void main(String[] args) throws Exception {
		CuratorFramework client = makeClient(zkAddress);
		LeaderElection leaderElection = LeaderElectionFactory.createHandler(new LeaderElectedWatcher() {
			@Override
			public void doTask() {
				System.out.println("I'm leader, time: " + SimpleDateFormat.getInstance().format(new Date()));
			}
			
			@Override
			public void stopTask() {
				System.out.println("I'm no longer a leader.");
			}
		}, client, "/test/election", new ChangeListener());
		leaderElection.start(false, UUID.randomUUID().toString());
		Thread.sleep(10000);
		leaderElection.stop();
		client.close();
	}
	
	private static class ChangeListener implements ParticipantsChangeListener {
		@Override
		public void onParticipantsChange(Set<String> participants) {
			System.out.println("the participants are: " + participants);
		}
	}
}
