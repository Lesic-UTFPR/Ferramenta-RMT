package br.com.messages.members;

import java.io.Serializable;

public class Member implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String memberId;

	private final String host;

	private final String port;

	private final MemberType memberType;

	public Member() {
		this("", "", "", null);
	}

	public Member(String memberId, String host, String port, MemberType memberType) {
		this.memberId = memberId;
		this.host = host;
		this.port = port;
		this.memberType = memberType;
	}

	public String getMemberId() {
		return memberId;
	}

	public String getHost() {
		return host;
	}

	public String getPort() {
		return port;
	}

	public MemberType getMemberType() {
		return memberType;
	}

}
