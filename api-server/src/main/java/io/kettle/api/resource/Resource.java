package io.kettle.api.resource;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Resource implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6569454416678033811L;
	
	private String apiVersion;
	private String kind;
	private ResourceMetadata metadata;
	private Map<String, Object> spec;
	private Map<String, Object> status;
	
	public String getApiVersion() {
		return apiVersion;
	}
	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	public ResourceMetadata getMetadata() {
		return metadata;
	}
	public void setMetadata(ResourceMetadata metadata) {
		this.metadata = metadata;
	}
	public Map<String, Object> getSpec() {
		return spec;
	}
	public void setSpec(Map<String, Object> spec) {
		this.spec = spec;
	}
	public Map<String, Object> getStatus() {
		return status;
	}
	public void setStatus(Map<String, Object> status) {
		this.status = status;
	}
	
}
