package br.com.client.module.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.zeroturnaround.zip.ZipUtil;

import br.com.client.module.utils.ClientTarget;
import br.com.messages.members.api.intermediary.IntermediaryAgentCoreApi;
import br.com.messages.members.api.intermediary.IntermediaryAgentProjectsApi;
import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.utils.CustomParametrizedType;
import br.com.messages.utils.FileUtils;

public class IntermediaryRepositoryImpl implements IntermediaryRepository {

	private ClientTarget clientTarget = new ClientTarget();

	@Override
	public String register(String projectName, Path projectSrc) throws Exception {

		final Path tmpZip = FileUtils.createOrOverrite("tmp.zip");
		try {
			ZipUtil.pack(projectSrc.toFile(), tmpZip.toFile());

			final Entity<InputStream> entity = Entity.entity(new FileInputStream(tmpZip.toFile()),
					MediaType.APPLICATION_OCTET_STREAM);

			final String registrationPath = String.format("%s%s%s/%s/%s", IntermediaryAgentCoreApi.AGENT_PATH,
					IntermediaryAgentProjectsApi.ROOT, IntermediaryAgentProjectsApi.REGISTRATION, projectName,
					Base64.getEncoder().encodeToString(FileUtils.getContentType(projectSrc).getBytes("UTF-8")));

			return this.clientTarget.get(registrationPath).request().post(entity, String.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			Files.delete(tmpZip);
		}
	}

	@Override
	public List<RefactoringCandidadeDTO> evaluate(String projectId) throws ServiceExeption {

		final String evalPath = String.format("%s%s%s/%s", IntermediaryAgentCoreApi.AGENT_PATH,
				IntermediaryAgentProjectsApi.ROOT, IntermediaryAgentProjectsApi.EVALUATE, projectId);

		return this.clientTarget.get(evalPath).request().get(new GenericType<List<RefactoringCandidadeDTO>>(
				new CustomParametrizedType(List.class, RefactoringCandidadeDTO.class)));
	}

	@Override
	public byte[] refactor(String projectId, List<RefactoringCandidadeDTO> candidates) {
		final String path = String.format("%s%s%s/%s", IntermediaryAgentCoreApi.AGENT_PATH,
				IntermediaryAgentProjectsApi.ROOT, IntermediaryAgentProjectsApi.APPLY_PATTERNS, projectId);

		final Response response = clientTarget.get(path).request().post(Entity.json(candidates));
		
		return response.readEntity(byte[].class);
	}

}
