package io.kettle.api.resource;

import java.io.Serializable;

public class MetadataProperty implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3859311414959283628L;

	private String key;
	private String value;
	
	public MetadataProperty(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
