package io.kettle.core.k8s;

import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * ApiResource
 */
@RegisterForReflection
public class ApiResource {

    private String kind;
    private String name;
    private boolean namespaced;
    private List<String> shortNames;
    private String singularName;
    private List<String> verbs;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNamespaced() {
        return namespaced;
    }

    public void setNamespaced(boolean namespaced) {
        this.namespaced = namespaced;
    }

    public List<String> getShortNames() {
        return shortNames;
    }

    public void setShortNames(List<String> shortNames) {
        this.shortNames = shortNames;
    }

    public String getSingularName() {
        return singularName;
    }

    public void setSingularName(String singularName) {
        this.singularName = singularName;
    }

    public List<String> getVerbs() {
        return verbs;
    }

    public void setVerbs(List<String> verbs) {
        this.verbs = verbs;
    }

}
