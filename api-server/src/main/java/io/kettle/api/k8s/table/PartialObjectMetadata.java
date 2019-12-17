package io.kettle.api.k8s.table;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.kettle.api.resource.ResourceMetadata;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartialObjectMetadata {
    private String apiVersion = "meta.k8s.io/v1beta1";
    private String kind = "PartialObjectMetadata";
    private ResourceMetadata metadata;
    private Map<String, Object> additionalProperties = new HashMap<>(0);

    @JsonCreator
    public PartialObjectMetadata(@JsonProperty("metadata") ResourceMetadata metadata) {
        this.metadata = metadata;
    }

    public ResourceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ResourceMetadata metadata) {
        this.metadata = metadata;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

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
}