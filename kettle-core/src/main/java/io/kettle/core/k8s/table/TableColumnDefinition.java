package io.kettle.core.k8s.table;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableColumnDefinition {
    private final String description;
    private final String format;
    private final String name;
    private final int priority;
    private final String type;
    private Map<String, Object> additionalProperties = new HashMap<>(0);

    @JsonCreator
    public TableColumnDefinition(@JsonProperty("description") String description,
            @JsonProperty("format") String format,
            @JsonProperty("name") String name,
            @JsonProperty("priority") int priority,
            @JsonProperty("type") String type) {
        this.description = description;
        this.format = format;
        this.name = name;
        this.priority = priority;
        this.type = type;
    }

    public TableColumnDefinition(String name, String type) {
        this(name, "", name, 0, type);
    }

    public String getDescription() {
        return description;
    }

    public String getFormat() {
        return format;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public String getType() {
        return type;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
