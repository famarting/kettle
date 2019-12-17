package io.kettle.api.k8s.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Table {
    private String apiVersion = "meta.k8s.io/v1beta1";
    private String kind = "Table";

    // private ListMeta metadata;
    private List<TableColumnDefinition> columnDefinitions;
    private List<TableRow> rows;

    private Map<String, Object> additionalProperties = new HashMap<>(0);

    // @JsonCreator
    public Table(
                 @JsonProperty("columnDefinitions") List<TableColumnDefinition> columnDefinitions,
                 @JsonProperty("rows") List<TableRow> rows) {
        // this.metadata = metadata;
        this.columnDefinitions = columnDefinitions;
        this.rows = rows;
    }

    // public ListMeta getMetadata() {
    //     return metadata;
    // }

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

    // public void setMetadata(ListMeta metadata) {
    //     this.metadata = metadata;
    // }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public List<TableColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public void setColumnDefinitions(List<TableColumnDefinition> columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
    }

    public void setRows(List<TableRow> rows) {
        this.rows = rows;
    }

}