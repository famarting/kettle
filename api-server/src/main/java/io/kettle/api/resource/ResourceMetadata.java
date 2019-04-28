package io.kettle.api.resource;

import java.io.Serializable;
import java.util.List;

public class ResourceMetadata implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4576358949572319677L;
	
	private String uid;
	private String name;
	private String namespace;
	private String selfLink;
	private String creationTimestamp;
	private List<MetadataProperty> labels;
	private List<MetadataProperty> annotations;
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getSelfLink() {
		return selfLink;
	}
	public void setSelfLink(String selfLink) {
		this.selfLink = selfLink;
	}
	public String getCreationTimestamp() {
		return creationTimestamp;
	}
	public void setCreationTimestamp(String creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}
	public List<MetadataProperty> getLabels() {
		return labels;
	}
	public void setLabels(List<MetadataProperty> labels) {
		this.labels = labels;
	}
	public List<MetadataProperty> getAnnotations() {
		return annotations;
	}
	public void setAnnotations(List<MetadataProperty> annotations) {
		this.annotations = annotations;
	}
	
}