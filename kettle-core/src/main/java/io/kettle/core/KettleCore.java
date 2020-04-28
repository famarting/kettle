package io.kettle.core;

import static io.kettle.core.KettleConstants.CORE_API_GROUP;
import static io.kettle.core.KettleConstants.CORE_API_VERSION;
import static io.kettle.core.KettleConstants.DEFINITION_RESOURCE_KIND;
import static io.kettle.core.KettleConstants.NAMESPACE_RESOURCE_KIND;

import java.util.Arrays;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kettle.core.resource.ResourceKey;
import io.kettle.core.resource.extension.DefinitionResourceSpec;
import io.kettle.core.resource.extension.ResourceNames;
import io.kettle.core.resource.extension.ResourceScope;
import io.kettle.core.resource.type.ResourceType;
import io.kettle.core.storage.ResourcesRepository;
import io.quarkus.runtime.StartupEvent;

@Singleton
public class KettleCore {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    Instance<KettleResourceService> services;

    @Inject
    ResourcesRepository resourcesRepository;


    public void init(@Observes StartupEvent ev) {
        log.info("Initializing kettle core resources");
        registerNamespaceResource();
        registerExtensionResource();
        services.forEach(s -> s.afterRegister());
    }

    private void registerNamespaceResource() {
        DefinitionResourceSpec spec = new DefinitionResourceSpec();
        spec.setVersion("v1");
        spec.setScope(ResourceScope.Global);
        ResourceNames names = new ResourceNames();
        names.setKind(NAMESPACE_RESOURCE_KIND);
        names.setListKind("Namespaces");
        names.setPlural("namespaces");
        names.setSingular("namespace");
        spec.setNames(names);
        spec.setShortNames(Arrays.asList("ns"));
        services.forEach(s -> s.register(spec));
        resourcesRepository.cacheCoreResource(spec);
    }

    private void registerExtensionResource() {
        DefinitionResourceSpec spec = new DefinitionResourceSpec();
        spec.setGroup(CORE_API_GROUP);
        spec.setVersion(CORE_API_VERSION);
        spec.setScope(ResourceScope.Global);
        ResourceNames names = new ResourceNames();
        names.setKind(DEFINITION_RESOURCE_KIND);
        names.setListKind("ResourcesDefinitions");
        names.setPlural("resourcesdefinitions");
        names.setSingular("resourcedefinition");
        spec.setNames(names);
        spec.setShortNames(Arrays.asList("rd", "rds", "crd"));// k8s :)
        services.forEach(s -> s.register(spec));
        resourcesRepository.cacheCoreResource(spec);
    }

}
