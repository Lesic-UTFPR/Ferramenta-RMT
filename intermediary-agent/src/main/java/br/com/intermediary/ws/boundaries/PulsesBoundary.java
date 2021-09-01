package br.com.intermediary.ws.boundaries;

import java.io.Serializable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.builder.ToStringBuilder;

import br.com.intermediary.managers.members.MembersManager;
import br.com.intermediary.managers.members.exceptions.pulses.PulseException;
import br.com.messages.members.Member;
import br.com.messages.members.api.intermediary.IntermediaryAgentPulsesApi;
import br.com.messages.pulses.Pulse;

@Stateless
@Path(IntermediaryAgentPulsesApi.ROOT)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PulsesBoundary implements Serializable {

	private static final long serialVersionUID = 1L;

	private @Inject MembersManager membersManager;

	@GET
	@Path(IntermediaryAgentPulsesApi.TEST)
	public Integer test() {

		System.out.println("Test!");

		return 1;
	}

	@POST
	@Path(IntermediaryAgentPulsesApi.RENEW)
	public Response renew(Pulse pulse) {

		try {
			System.out.println(String.format("Recebido pulso %s.", ToStringBuilder.reflectionToString(pulse)));
			
			membersManager.renewAvailability(pulse);

			return Response.ok().build();
		} catch (PulseException e) {
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
		}
	}

	@POST
	@Path(IntermediaryAgentPulsesApi.REGISTRATION)
	public Response register(Member member) {

		try {
			System.out.println(String.format("Membro %s registrado.", ToStringBuilder.reflectionToString(member)));
			
			membersManager.register(member);

			return Response.ok().build();
		} catch (PulseException e) {
			return Response.status(Response.Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
		}
	}

}
