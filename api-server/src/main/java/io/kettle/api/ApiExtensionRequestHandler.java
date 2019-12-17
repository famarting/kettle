package io.kettle.api;

import java.util.HashMap;
import java.util.Optional;

import io.kettle.api.resource.Resource;
import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.resource.extension.ResourceScope;
import io.kettle.api.storage.ResourcesRepository;

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
        DefinitionResourceSpec definition = new DefinitionResourceSpec(resource.getSpec());
        String link;
        if ( definition.getScope() == ResourceScope.Global ) {
            link = String.format("/apis/%s/%s/%s/", definition.getGroup(), definition.getVersion(), definition.getNames().getPlural());
        } else {
            link = String.format("/apis/%s/%s/namespaces/:namespace/%s/", definition.getGroup(), definition.getVersion(), definition.getNames().getPlural());
        }
        resource.setStatus(Optional.ofNullable(resource.getStatus()).orElseGet(HashMap::new));
        resource.getStatus().put("link", link);
        super.create(requestContext, resource);
        apiResourcesService.registerApiGroupRoute(definition, defaultRequestHandlerFactory);
    }

    @Override
    protected Resource delete(ApiServerRequestContext requestContext) {
        Resource resource = super.delete(requestContext);
        DefinitionResourceSpec resourceSpec = new DefinitionResourceSpec(resource.getSpec());
        apiResourcesService.removeResourceRoute(resourceSpec);
        return resource;
    }

    // TODO extension resources cannot be updated


}
