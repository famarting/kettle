package io.kettle.api.resource.extension;

import java.io.Serializable;

public class DefinitionResourceKey implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -460982838135053343L;

    public String group;
    public String version;
    public String kind;

    public DefinitionResourceKey(DefinitionResourceSpec resource) {
        this.group = resource.getGroup();
        this.version = resource.getVersion();
        this.kind = resource.getNames().getKind();
    }

    public DefinitionResourceKey(String group, String version, String kind) {
        super();
        this.group = group;
        this.version = version;
        this.kind = kind;
    }

    @Override
    public String toString() {
        return "DefinitionResourceKey [group=" + group + ", version=" + version + ", kind=" + kind + "]";
    }

}
