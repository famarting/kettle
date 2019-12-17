package io.kettle.api;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;

@Singleton
public class ApiServer {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ApiResourcesManager apiResourcesManager;

    @Inject
    public ApiServer(ApiResourcesManager apiResourcesManager) {
        this.apiResourcesManager = apiResourcesManager;
    }

    public void init(@Observes StartupEvent ev) {
        log.info("Initializing api-server");
        apiResourcesManager.registerCoreResources();
        apiResourcesManager.loadResourcesDefinitions();
    }


}
