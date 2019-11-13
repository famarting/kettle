package io.kettle.api;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kettle.api.resource.Resource;
import io.kettle.api.resource.ResourceMetadata;
import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.resource.extension.ResourceNames;
import io.kettle.api.resource.extension.ResourceScope;
import io.kettle.api.resource.type.ResourceType;
import io.kettle.api.storage.ResourcesRepository;
import io.vertx.core.json.JsonObject;

@Singleton
public class ApiResourcesManager {

	public static final String DEFINITION_RESOURCE_KIND = "ResourceDefinition";
	public static final String CORE_API_GROUP = "core";
	public static final String CORE_API_VERSION = "v1beta1";

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private ApiServerRequestHandlerFactory defaultRequestHandlerFactory;
	private ApiExtensionRequestHandlerFactory apiExtensionRequestHandlerFactory;

	private ApiResourcesService apiResourcesService;
	private ResourcesRepository resourcesRepository;

	@Inject
	public ApiResourcesManager(ApiServerRequestHandlerFactory defaultRequestHandlerFactory,
			ApiExtensionRequestHandlerFactory apiExtensionRequestHandlerFactory,
			ApiResourcesService apiResourcesService, ResourcesRepository resourcesRepository) {
		super();
		this.defaultRequestHandlerFactory = defaultRequestHandlerFactory;
		this.apiExtensionRequestHandlerFactory = apiExtensionRequestHandlerFactory;
		this.apiResourcesService = apiResourcesService;
		this.resourcesRepository = resourcesRepository;
	}

	public void registerCoreResources() {
		registerNamespaceResource();
		registerExtensionResource();
	}

	// public void loadResourcesDefinitions() {
	// definitionsRepository.getAllDefinitions().forEach(definition->{
	// log.info("Creating route for existing resource definition {}", definition);
	// apiResourcesService.registerResourceRoute(definition,
	// defaultRequestHandlerFactory);
	// });
	// }

	private void registerNamespaceResource() {
		DefinitionResourceSpec spec = new DefinitionResourceSpec();
		spec.setGroup(CORE_API_GROUP);
		spec.setVersion(CORE_API_VERSION);
		spec.setScope(ResourceScope.Global);
		ResourceNames names = new ResourceNames();
		names.setKind("Namespace");
		names.setListKind("Namespaces");
		names.setPlural("namespaces");
		names.setSingular("namespace");
		spec.setNames(names);
		apiResourcesService.registerResourceRoute(spec, defaultRequestHandlerFactory);
		storeResource(spec);
	}

	private void registerExtensionResource() {
		DefinitionResourceSpec spec = new DefinitionResourceSpec();
		spec.setGroup(CORE_API_GROUP);
		spec.setVersion(CORE_API_VERSION);
		spec.setScope(ResourceScope.Global);
		ResourceNames names = new ResourceNames();
		names.setKind("ResourceDefinition");
		names.setListKind("ResourcesDefinitions");
		names.setPlural("resourcesdefinitions");
		names.setSingular("resourcedefinition");
		spec.setNames(names);
		apiResourcesService.registerResourceRoute(spec, apiExtensionRequestHandlerFactory);
		storeResource(spec);
	}

	private void storeResource(DefinitionResourceSpec spec) {
		Resource resource = new Resource();
		resource.setApiVersion(ApiServerUtils.formatApiVersion(CORE_API_GROUP, CORE_API_VERSION));
		resource.setKind(DEFINITION_RESOURCE_KIND);
		ResourceMetadata metadata = new ResourceMetadata();
		metadata.setName(spec.getNames().getPlural());
		resource.setMetadata(metadata);
		ObjectMapper mapper = new ObjectMapper();
		try {
			resource.setSpec(
					mapper.readValue(mapper.writeValueAsString(spec), new TypeReference<Map<String, Object>>() {
					}));
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
		resourcesRepository.createResource(ResourceType.global(), resource);
	}
	
}
