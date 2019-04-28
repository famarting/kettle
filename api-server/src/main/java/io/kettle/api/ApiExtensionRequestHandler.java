package io.kettle.api;

import io.kettle.api.resource.Resource;
import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.storage.DefinitionResourceRepository;
import io.kettle.api.storage.ResourcesRepository;
import io.vertx.core.json.JsonObject;

public class ApiExtensionRequestHandler extends ApiServerRequestHandler{

	private ApiResourcesService apiResourcesService;
	private ApiServerRequestHandlerFactory defaultRequestHandlerFactory;
	
	public ApiExtensionRequestHandler(ApiResourcesService apiResourcesService, ApiServerRequestHandlerFactory defaultRequestHandlerFactory, DefinitionResourceRepository definitionsRepository, ResourcesRepository resourcesRepository) {
		super(definitionsRepository, resourcesRepository);
		this.apiResourcesService = apiResourcesService;
		this.defaultRequestHandlerFactory = defaultRequestHandlerFactory;
	}

	@Override
	protected void create(ApiServerRequestContext requestContext, Resource resource) {
		super.create(requestContext, resource);
		DefinitionResourceSpec resourceSpec = new JsonObject(resource.getSpec()).mapTo(DefinitionResourceSpec.class);
		apiResourcesService.registerResourceRoute(resourceSpec, defaultRequestHandlerFactory);
		definitionsRepository.saveDefinitionResource(resourceSpec);
	}

}
