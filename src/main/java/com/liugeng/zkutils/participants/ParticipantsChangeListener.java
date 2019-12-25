package com.liugeng.zkutils.participants;

import java.util.Set;

/**
 * @author Liu Geng liu.geng@navercorp.com
 * @date 2019/11/19 18:00
 */
public interface ParticipantsChangeListener {
	
	void onParticipantsChange(Set<String> participants);
}
