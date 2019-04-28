package io.kettle.api;

import java.util.Optional;

import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.resource.type.ResourceType;
import io.vertx.ext.web.RoutingContext;

public class ApiServerRequestContext {

	private DefinitionResourceSpec definition;
	private RoutingContext httpContext;
	private ResourceType resourceType;
	private Optional<String> resourceName;
	
	public ApiServerRequestContext(DefinitionResourceSpec definition, RoutingContext httpContext, ResourceType resourceType, Optional<String> resourceName) {
		this.definition = definition;
		this.httpContext = httpContext;
		this.resourceType = resourceType;
		this.resourceName = resourceName;
	}

	public DefinitionResourceSpec definition() {
		return definition;
	}
	
	public RoutingContext httpContext() {
		return httpContext;
	}
	
	public ResourceType resourceType() {
		return resourceType;
	}
	
	public Optional<String> resourceName() {
		return resourceName;
	}
}