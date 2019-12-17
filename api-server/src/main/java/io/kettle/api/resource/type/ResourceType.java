package io.kettle.api.resource.type;

import java.io.Serializable;

import io.kettle.api.resource.extension.ResourceScope;

public class ResourceType implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2771892261898599189L;

    private ResourceScope scope;
    private String namespace;

    public static ResourceType global() {
        return new ResourceType(ResourceScope.Global, null);
    }

    public static ResourceType namespaced(String namespace) {
        return new ResourceType(ResourceScope.Namespaced, namespace);
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
