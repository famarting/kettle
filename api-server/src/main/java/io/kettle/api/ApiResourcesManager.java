package io.kettle.api;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.resource.extension.ResourceNames;
import io.kettle.api.resource.extension.ResourceScope;
import io.kettle.api.storage.DefinitionResourceRepository;

@Singleton
public class ApiResourcesManager {

	private static final String CORE_API_GROUP = "core";
	private static final String CORE_API_VERSION = "v1beta1";
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	private ApiServerRequestHandlerFactory defaultRequestHandlerFactory;
	private ApiExtensionRequestHandlerFactory apiExtensionRequestHandlerFactory;
	
	private ApiResourcesService apiResourcesService;
	private DefinitionResourceRepository definitionsRepository;

	
	@Inject
	public ApiResourcesManager(ApiServerRequestHandlerFactory defaultRequestHandlerFactory,
			ApiExtensionRequestHandlerFactory apiExtensionRequestHandlerFactory, 
			ApiResourcesService apiResourcesService,
			DefinitionResourceRepository definitionsRepository) {
		super();
		this.defaultRequestHandlerFactory = defaultRequestHandlerFactory;
		this.apiExtensionRequestHandlerFactory = apiExtensionRequestHandlerFactory;
		this.apiResourcesService = apiResourcesService;
		this.definitionsRepository = definitionsRepository;
	}
	
	public void registerCoreResources() {
		registerNamespaceResource();
		registerExtensionResource();
	}

	public void loadResourcesDefinitions() {
		definitionsRepository.getAllDefinitions().forEach(definition->{
			log.info("Creating route for existing resource definition {}", definition);
			apiResourcesService.registerResourceRoute(definition, defaultRequestHandlerFactory);
		});
	}
	
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
		definitionsRepository.saveCoreDefinitionResource(spec);
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
		definitionsRepository.saveCoreDefinitionResource(spec);
	}
	
}
