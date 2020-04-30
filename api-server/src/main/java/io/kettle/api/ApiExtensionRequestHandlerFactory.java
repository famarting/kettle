package io.kettle.api;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.kettle.core.storage.ResourcesService;

@ApplicationScoped
public class ApiExtensionRequestHandlerFactory implements RequestHandlerFactory {

    @Inject
    ApiResourcesService apiResourcesmanager;
    @Inject
    ApiServerRequestHandlerFactory apiServerRequestHandlerFactory;
    @Inject
    ResourcesService resourcesRepository;

    public RequestHandler createRequestHandler() {
        return new ApiExtensionRequestHandler(apiResourcesmanager, apiServerRequestHandlerFactory, resourcesRepository);
    }

}
