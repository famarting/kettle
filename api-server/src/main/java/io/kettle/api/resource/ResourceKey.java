package io.kettle.api.resource;

import java.io.Serializable;

import io.kettle.api.resource.type.ResourceType;

public class ResourceKey implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 962971635186686490L;
	
	public String apiVersion;
	public String kind;
	public ResourceType type;
	public String name;
	
	public ResourceKey(String apiVersion, String kind, ResourceType type, String name) {
		this.apiVersion = apiVersion;
		this.kind = kind;
		this.type = type;
		this.name = name;
	}

}
