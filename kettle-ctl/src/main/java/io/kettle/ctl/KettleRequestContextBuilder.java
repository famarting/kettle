package io.kettle.ctl;

import java.util.Optional;

import io.kettle.core.resource.Resource;
import io.kettle.core.resource.type.ResourceType;
import io.kettle.ctl.config.KettleConfig;

public class KettleRequestContextBuilder {

    private KettleProcessedArgs args;
    private KettleOperations op;
    private String kettleConfigPath;
    private KettleConfig kettleConfig;

    public KettleRequestContext build(String group, String version, String kind, ResourceType resourceType, Optional<String> resourceName, Optional<Resource> resource) {
        return new KettleRequestContext(kettleConfigPath, kettleConfig, args, op, group, version, kind, resourceType, resourceName, resource);
    }

    public KettleRequestContextBuilder setArgs(KettleProcessedArgs args) {
        this.args = args;
        return this;
    }

    public KettleRequestContextBuilder setKettleConfig(String path, KettleConfig kettleConfig) {
        this.kettleConfigPath = path;
        this.kettleConfig = kettleConfig;
        return this;
    }

    public KettleRequestContextBuilder setOp(KettleOperations op) {
        this.op = op;
        return this;
    }

    public KettleProcessedArgs getArgs() {
        return args;
    }

    public KettleOperations getOp() {
        return op;
    }

    public String getKettleConfigPath() {
        return kettleConfigPath;
    }

    public KettleConfig getKettleConfig() {
        return kettleConfig;
    }

}
