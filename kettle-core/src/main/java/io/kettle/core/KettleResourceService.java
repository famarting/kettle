package io.kettle.core;

import io.kettle.core.resource.extension.DefinitionResourceSpec;

public interface KettleResourceService {

    public void register(DefinitionResourceSpec resource);

    public void afterRegister();

}
