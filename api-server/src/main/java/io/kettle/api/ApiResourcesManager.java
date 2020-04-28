package io.kettle.api;

import static io.kettle.core.KettleConstants.CORE_API_GROUP;
import static io.kettle.core.KettleConstants.CORE_API_VERSION;
import static io.kettle.core.KettleConstants.DEFINITION_RESOURCE_KIND;
import static io.kettle.core.KettleConstants.NAMESPACE_RESOURCE_KIND;

import java.util.function.BiConsumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.kettle.core.KettleResourceService;
import io.kettle.core.KettleUtils;
import io.kettle.core.resource.extension.DefinitionResourceSpec;
import io.kettle.core.storage.ResourcesRepository;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@Singleton
public class ApiResourcesManager implements KettleResourceService {

    private ObjectMapper jsonMapper = new ObjectMapper().setSerializationInclusion(Include.NON_EMPTY)
            .enable(SerializationFeature.INDENT_OUTPUT);

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ApiServerRequestHandlerFactory defaultRequestHandlerFactory;
    private ApiExtensionRequestHandlerFactory apiExtensionRequestHandlerFactory;
    private ApiGroupsService apiGroupsService;
    private ApiResourcesService apiResourcesService;
    private ResourcesRepository resourcesRepository;

    @Inject
    public ApiResourcesManager(ApiServerRequestHandlerFactory defaultRequestHandlerFactory,
            ApiExtensionRequestHandlerFactory apiExtensionRequestHandlerFactory, ApiGroupsService apiGroupsService,
            ApiResourcesService apiResourcesService, ResourcesRepository resourcesRepository) {
        this.defaultRequestHandlerFactory = defaultRequestHandlerFactory;
        this.apiExtensionRequestHandlerFactory = apiExtensionRequestHandlerFactory;
        this.apiGroupsService = apiGroupsService;
        this.apiResourcesService = apiResourcesService;
        this.resourcesRepository = resourcesRepository;
    }

    public void registerKubernetesCompatApis() {
        // k8s compatibility
        registerApiVersions();
        registerApiGroups();
    }

    public void loadResourcesDefinitions() {
        resourcesRepository
                .doGlobalQuery(KettleUtils.formatApiVersion(CORE_API_GROUP,
                        CORE_API_VERSION), DEFINITION_RESOURCE_KIND)
                .forEach(resource -> {
                    DefinitionResourceSpec definition = new DefinitionResourceSpec(resource.getSpec());
                    log.info("Creating route for existing resource definition {}", definition);
                    apiResourcesService.registerApiGroupRoute(definition, defaultRequestHandlerFactory);
                });
    }

    private void registerApiVersions() {
        JsonObject resource = new JsonObject();
        resource.put("kind", "APIVersions");
        resource.put("versions", new JsonArray().add("v1"));

        apiResourcesService.registerCoreRoute(route -> {
            route.path("/api").method(HttpMethod.GET).handler(ctx -> {
                ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(resource.encode());
            });
        });
    }

    private void registerApiGroups() {
        apiResourcesService.registerCoreRoute(route -> {
            route.path("/apis").method(HttpMethod.GET).handler(ctx -> {
                try {
                    String list = jsonMapper.writeValueAsString(apiGroupsService.getApiGroupList());
                    ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(list);
                } catch ( JsonProcessingException e ) {
                    log.error("", e);
                    ctx.response().setStatusCode(500).end();
                }
            });
        });
    }

    @Override
    public void register(DefinitionResourceSpec resource) {
        RequestHandlerFactory requestHandlerFactory = defaultRequestHandlerFactory;
        BiConsumer<DefinitionResourceSpec, RequestHandlerFactory> registrar = apiResourcesService::registerApiGroupRoute;
        if (resource.getNames().getKind().equals(DEFINITION_RESOURCE_KIND)) {
            requestHandlerFactory = apiExtensionRequestHandlerFactory;
        } else if (resource.getNames().getKind().equals(NAMESPACE_RESOURCE_KIND)) {
            registrar = apiResourcesService::registerApiServiceRoute;
        }
        registrar.accept(resource, requestHandlerFactory);
    }

    @Override
    public void afterRegister() {
        registerKubernetesCompatApis();
        loadResourcesDefinitions();
    }

}
