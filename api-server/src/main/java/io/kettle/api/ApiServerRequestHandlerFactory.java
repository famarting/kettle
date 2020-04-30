package io.kettle.api;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.kettle.core.storage.ResourcesService;

@ApplicationScoped
public class ApiServerRequestHandlerFactory implements RequestHandlerFactory {

    @Inject
    ResourcesService resourcesRepository;

    public RequestHandler createRequestHandler() {
        return new ApiServerRequestHandler(resourcesRepository);
    }

}
