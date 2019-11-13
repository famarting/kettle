package io.kettle.api;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.kettle.api.storage.ResourcesRepository;

@ApplicationScoped
public class ApiExtensionRequestHandlerFactory implements RequestHandlerFactory {

	@Inject
	ApiResourcesService apiResourcesmanager;
	@Inject
	ApiServerRequestHandlerFactory apiServerRequestHandlerFactory;
	@Inject
	ResourcesRepository resourcesRepository;

	public RequestHandler createRequestHandler() {
		return new ApiExtensionRequestHandler(apiResourcesmanager, apiServerRequestHandlerFactory, resourcesRepository);
	}
	
}
