package com.liugeng.zkutils.election;

import java.util.Set;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/18 17:40
 */
public interface LeaderElection {
	
	String start(boolean autoRelease) throws Exception;
	
	String start(boolean autoRelease, String instanceName) throws Exception;
	
	void release();
	
	void stop();
	
	boolean isLeader();
	
	Set<String> getParticipants();
	
	String getParticipantId();
	
	String getNamespace();
	
	Set<String> refreshParticipants() throws Exception;
}
