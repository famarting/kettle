package io.kettle.api.k8s;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * ApiGroup
 */
@RegisterForReflection
public class ApiGroup {

    private String name;
    private GroupVersionForDiscovery preferredVersion;
    private List<GroupVersionForDiscovery> versions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GroupVersionForDiscovery getPreferredVersion() {
        return preferredVersion;
    }

    public void setPreferredVersion(GroupVersionForDiscovery preferredVersion) {
        this.preferredVersion = preferredVersion;
    }

    public List<GroupVersionForDiscovery> getVersions() {
        return versions;
    }

    public void setVersions(List<GroupVersionForDiscovery> versions) {
        this.versions = versions;
    }

}
