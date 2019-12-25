package com.liugeng.zkutils.example.election;

import static com.liugeng.zkutils.utils.CommonUtils.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.curator.framework.CuratorFramework;

import com.liugeng.zkutils.election.LeaderElectedWatcher;
import com.liugeng.zkutils.election.LeaderElection;
import com.liugeng.zkutils.election.LeaderElectionFactory;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/25 16:29
 */
public class LeaderElectionExample {
	
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
		}, client, "/test/election");
		leaderElection.start(false, "test1");
		
		Thread.sleep(5000);
		
		if (leaderElection.isLeader()) {
			leaderElection.release();
		}
		
		Thread.sleep(10000);
		
		leaderElection.stop();
		client.close();
	}
}
