package br.com.intermediary.managers.members;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Local;

import br.com.intermediary.managers.members.exceptions.pulses.PulseException;
import br.com.messages.members.Member;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.pulses.Pulse;

@Local
public interface MembersManager extends Serializable {

	void register(Member member) throws PulseException;

	void renewAvailability(Pulse pulse) throws PulseException;

	List<Member> getAliveMembers();
	
	List<Reference> getDetectorReferences(Member member);

	Member getNextDetector();
	
	Member getNextMetrics();

}
