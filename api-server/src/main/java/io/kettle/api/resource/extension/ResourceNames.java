package io.kettle.api.resource.extension;

import java.io.Serializable;

public class ResourceNames implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8803033944458199179L;
	
	private String kind;
	private String listKind;
	private String singular;
	private String plural;
	
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	public String getListKind() {
		return listKind;
	}
	public void setListKind(String listKind) {
		this.listKind = listKind;
	}
	public String getSingular() {
		return singular;
	}
	public void setSingular(String singular) {
		this.singular = singular;
	}
	public String getPlural() {
		return plural;
	}
	public void setPlural(String plural) {
		this.plural = plural;
	}
	
}
