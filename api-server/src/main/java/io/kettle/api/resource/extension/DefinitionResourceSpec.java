package io.kettle.api.resource.extension;

import java.io.Serializable;
import java.util.Map;

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
		this.group = (String) spec.get("group");
		this.version = (String) spec.get("version");
		this.scope = ResourceScope.valueOf((String) spec.get("scope"));
		Map<String, String> names = (Map<String, String>) spec.get("names");
		ResourceNames resourceNames = new ResourceNames();
		resourceNames.setKind(names.get("kind"));
		resourceNames.setListKind(names.get("listKind"));
		resourceNames.setPlural(names.get("plural"));
		resourceNames.setSingular(names.get("singular"));
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
