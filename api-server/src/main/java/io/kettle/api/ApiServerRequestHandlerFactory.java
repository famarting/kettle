package io.kettle.api;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.kettle.api.storage.DefinitionResourceRepository;
import io.kettle.api.storage.ResourcesRepository;

@ApplicationScoped
public class ApiServerRequestHandlerFactory implements RequestHandlerFactory{

	@Inject
	DefinitionResourceRepository definitionsRepository;
	@Inject
	ResourcesRepository resourcesRepository;

	public RequestHandler createRequestHandler() {
		return new ApiServerRequestHandler(definitionsRepository, resourcesRepository);
	}

}
