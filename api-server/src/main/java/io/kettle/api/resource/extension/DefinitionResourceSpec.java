package io.kettle.api.resource.extension;

import java.io.Serializable;
import java.util.Map;

import io.vertx.core.json.JsonObject;

public class DefinitionResourceSpec implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5149576156364942388L;
	
	private String group;
	private String version;
	private ResourceScope scope;
	private ResourceNames names;
	
	public DefinitionResourceSpec() {
		//empty
	}

	public DefinitionResourceSpec(Map<String, Object> spec) {
		JsonObject json = new JsonObject(spec);
		this.group = json.getString("group");
		this.version = json.getString("version");
		this.scope = ResourceScope.valueOf(json.getString("scope"));
		JsonObject names = json.getJsonObject("names");
		ResourceNames resourceNames = new ResourceNames();
		resourceNames.setKind(names.getString("kind"));
		resourceNames.setListKind(names.getString("listKind"));
		resourceNames.setPlural(names.getString("plural"));
		resourceNames.setSingular(names.getString("singular"));
		this.names = resourceNames;
	}

	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public ResourceScope getScope() {
		return scope;
	}
	public void setScope(ResourceScope scope) {
		this.scope = scope;
	}
	public ResourceNames getNames() {
		return names;
	}
	public void setNames(ResourceNames names) {
		this.names = names;
	}
	
	@Override
	public String toString() {
		return "[group=" + group + ", version=" + version + ", kind=" + names.getKind() + "]";
	}
	
}
