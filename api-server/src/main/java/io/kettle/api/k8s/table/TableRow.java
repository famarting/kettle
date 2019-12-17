package io.kettle.api.k8s.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableRow {
    private final List<Object> cells;
    private final PartialObjectMetadata object;
    private Map<String, Object> additionalProperties = new HashMap<>(0);

    @JsonCreator
    public TableRow(@JsonProperty("cells") List<Object> cells,
                    @JsonProperty("object") PartialObjectMetadata object) {
        this.cells = cells;
        this.object = object;
    }

    public List<Object> getCells() {
        return cells;
    }

    public PartialObjectMetadata getObject() {
        return object;
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