package io.kettle.api;

import io.kettle.api.resource.Resource;
import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.storage.ResourcesRepository;
import io.vertx.core.json.JsonObject;

public class ApiExtensionRequestHandler extends ApiServerRequestHandler {

	private ApiResourcesService apiResourcesService;
	private ApiServerRequestHandlerFactory defaultRequestHandlerFactory;

	public ApiExtensionRequestHandler(ApiResourcesService apiResourcesService,
			ApiServerRequestHandlerFactory defaultRequestHandlerFactory, ResourcesRepository resourcesRepository) {
		super(resourcesRepository);
		this.apiResourcesService = apiResourcesService;
		this.defaultRequestHandlerFactory = defaultRequestHandlerFactory;
	}

	@Override
	protected void create(ApiServerRequestContext requestContext, Resource resource) {
		super.create(requestContext, resource);
		DefinitionResourceSpec resourceSpec = new JsonObject(resource.getSpec()).mapTo(DefinitionResourceSpec.class);
		apiResourcesService.registerResourceRoute(resourceSpec, defaultRequestHandlerFactory);
	}

	@Override
	protected Resource delete(ApiServerRequestContext requestContext) {
		Resource resource = super.delete(requestContext);
		DefinitionResourceSpec resourceSpec = new JsonObject(resource.getSpec()).mapTo(DefinitionResourceSpec.class);
		apiResourcesService.removeResourceRoute(resourceSpec);
		return resource;
	}

	//TODO extension resources cannot be updated


}
