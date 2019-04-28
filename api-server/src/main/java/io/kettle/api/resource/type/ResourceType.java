package io.kettle.api.resource.type;

import java.io.Serializable;

import io.kettle.api.resource.extension.ResourceScope;

public class ResourceType implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2771892261898599189L;
	
	private ResourceScope scope;
	private String namespace;
	
	public ResourceType(ResourceScope scope) {
		this.scope = scope;
	}
	
	public ResourceType(String namespace) {
		this.scope = ResourceScope.Namespaced;
		this.namespace = namespace;
		
	}
	
	public ResourceType(ResourceScope scope, String namespace) {
		this.scope = scope;
		this.namespace = namespace;
	}

	public ResourceScope scope() {
		return this.scope;
	}
	
	public String namespace() {
		return this.namespace;
	}
	
}
