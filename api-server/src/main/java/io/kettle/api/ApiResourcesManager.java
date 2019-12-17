package io.kettle.api;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.resource.extension.ResourceNames;
import io.kettle.api.resource.extension.ResourceScope;
import io.kettle.api.storage.ResourcesRepository;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@Singleton
public class ApiResourcesManager {

    public static final String DEFINITION_RESOURCE_KIND = "ResourceDefinition";
    public static final String NAMESPACE_RESOURCE_KIND = "Namespace";
    public static final String CORE_API_GROUP = "core";
    public static final String CORE_API_VERSION = "v1beta1";

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

    public void registerCoreResources() {
        // k8s compatibility
        registerApiVersions();
        registerApiGroups();
        // kettle core
        registerNamespaceResource();
        registerExtensionResource();
    }

    public void loadResourcesDefinitions() {
        resourcesRepository
                .doGlobalQuery(ApiServerUtils.formatApiVersion(ApiResourcesManager.CORE_API_GROUP,
                        ApiResourcesManager.CORE_API_VERSION), ApiResourcesManager.DEFINITION_RESOURCE_KIND)
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

    private void registerNamespaceResource() {
        DefinitionResourceSpec spec = new DefinitionResourceSpec();
        // spec.setGroup(CORE_API_GROUP);
        spec.setVersion("v1");
        spec.setScope(ResourceScope.Global);
        ResourceNames names = new ResourceNames();
        names.setKind(NAMESPACE_RESOURCE_KIND);
        names.setListKind("Namespaces");
        names.setPlural("namespaces");
        names.setSingular("namespace");
        spec.setNames(names);
        spec.setShortNames(Arrays.asList("ns"));
        apiResourcesService.registerApiServiceRoute(spec, defaultRequestHandlerFactory);
        resourcesRepository.cacheCoreResource(spec);
    }

    private void registerExtensionResource() {
        DefinitionResourceSpec spec = new DefinitionResourceSpec();
        spec.setGroup(CORE_API_GROUP);
        spec.setVersion(CORE_API_VERSION);
        spec.setScope(ResourceScope.Global);
        ResourceNames names = new ResourceNames();
        names.setKind(DEFINITION_RESOURCE_KIND);
        names.setListKind("ResourcesDefinitions");
        names.setPlural("resourcesdefinitions");
        names.setSingular("resourcedefinition");
        spec.setNames(names);
        spec.setShortNames(Arrays.asList("rd", "rds", "crd"));// k8s :)
        apiResourcesService.registerApiGroupRoute(spec, apiExtensionRequestHandlerFactory);
        resourcesRepository.cacheCoreResource(spec);
    }

}
