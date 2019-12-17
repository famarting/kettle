package io.kettle.api.k8s;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * GroupVersionForDiscovery
 */
@RegisterForReflection
public class GroupVersionForDiscovery {

    /**
     * groupVersion specifies the API group and version in the form "group/version"
     */
    private String groupVersion;

    private String version;

    public String getGroupVersion() {
        return groupVersion;
    }

    public void setGroupVersion(String groupVersion) {
        this.groupVersion = groupVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
}