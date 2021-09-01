package br.com.detection.domain.identity;

import java.util.UUID;

import br.com.messages.members.Member;
import br.com.messages.members.MemberType;

public class Identity {

	public static final String ID = UUID.randomUUID().toString();

	public static final String HOST = "localhost";

	public static final String PORT = "8080";

	private static final String AGENT_PATH = "detection-agent";

	public static Member getAsMember() {
		return new Member(Identity.ID, Identity.HOST, Identity.PORT, MemberType.PATTERNS_SPOTS_DETECTOR);
	}

}
