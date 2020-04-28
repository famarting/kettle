package io.kettle.ctl;

import java.util.Optional;

import io.kettle.core.resource.Resource;
import io.kettle.core.resource.type.ResourceType;
import io.kettle.ctl.config.KettleConfig;

public class KettleRequestContext {

    private final String kettleConfigPath;
    private final KettleConfig kettleConfig;

    private final KettleProcessedArgs args;

    private final KettleOperations op;

    private boolean result;

    private final Optional<Resource> resource;

    private final String group;
    private final String version;
    private final String kind;
    private final ResourceType resourceType;
    private final Optional<String> resourceName;

    public KettleRequestContext(String kettleConfigPath, KettleConfig kettleConfig, KettleProcessedArgs args, KettleOperations op, String group, String version, String kind, ResourceType resourceType, Optional<String> resourceName, Optional<Resource> resource) {
        this.kettleConfigPath = kettleConfigPath;
        this.kettleConfig = kettleConfig;
        this.group = group;
        this.version = version;
        this.kind = kind;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.result = false;
        this.args = args;
        this.op = op;
        this.resource = resource;
    }

    public static KettleRequestContextBuilder builder() {
        return new KettleRequestContextBuilder();
    }

    public String kettleConfigPath() {
        return kettleConfigPath;
    }

    public KettleConfig kettleConfig() {
        return kettleConfig;
    }

    public String group() {
        return group;
    }

    public String version() {
        return version;
    }

    public String kind() {
        return kind;
    }

    public boolean result() {
        return result;
    }

    public ResourceType resourceType() {
        return resourceType;
    }

    public Optional<String> resourceName() {
        return resourceName;
    }

//    public void setResourceName(String name) {
//        this.resourceName = Optional.of(name);
//    }

    public String arg(int index) {
        return args.arg(index);
    }

    public KettleOperations op() {
        return op;
    }

    public Optional<Resource> resource() {
        return resource;
    }

    public String flag(String flag) {
        return args.flag(flag);
    }

}