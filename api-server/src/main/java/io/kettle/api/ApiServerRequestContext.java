package io.kettle.api;

import java.util.Optional;

import io.kettle.api.resource.type.ResourceType;
import io.vertx.ext.web.RoutingContext;

public class ApiServerRequestContext {

	private String group;
	private String version;
	private String kind;
	private RoutingContext httpContext;
	private ResourceType resourceType;
	private Optional<String> resourceName;
	
	public ApiServerRequestContext(String group, String version, String kind, RoutingContext httpContext, ResourceType resourceType, Optional<String> resourceName) {
		this.group = group;
		this.version = version;
		this.kind = kind;
		this.httpContext = httpContext;
		this.resourceType = resourceType;
		this.resourceName = resourceName;
	}

	public String group() {
		return group;
	}
	
	public String version() {
		return version;
	}

	public String kind() {
		return kind;
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

	public void setResourceName(String name) {
		this.resourceName = Optional.of(name);
	}
}