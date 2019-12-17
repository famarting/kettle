package io.kettle.api.k8s;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * APIGroupListSource
 */
@RegisterForReflection
public class APIGroupList {

    private String apiVersion;
    private String kind;
    private List<ApiGroup> groups;

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

    public List<ApiGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<ApiGroup> groups) {
        this.groups = groups;
    }
    
}