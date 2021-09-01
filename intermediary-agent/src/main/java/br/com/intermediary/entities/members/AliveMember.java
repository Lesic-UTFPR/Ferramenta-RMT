package br.com.intermediary.entities.members;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import br.com.messages.members.Member;

public class AliveMember implements Serializable {

	private static final long serialVersionUID = 1L;

	public static int REMAINING_SECONDS = 15;

	private final Member member;

	private LocalDateTime dueDate = LocalDateTime.now().plusSeconds(REMAINING_SECONDS);

	public AliveMember(Member member) {
		this.member = member;
	}

	public String getMemberId() {
		return this.member.getMemberId();
	}

	public Member getMember() {
		return this.member;
	}

	public void renew() {
		dueDate = LocalDateTime.now().plusSeconds(10);
	}

	public boolean isExpired(LocalDateTime current) {
		return !current.isBefore(dueDate);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AliveMember) {
			final AliveMember am = (AliveMember) obj;

			return new EqualsBuilder().append(this.member.getMemberId(), am.member.getMemberId()).build();
		}

		return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.member.getMemberId()).build();
	}

}
