package io.kettle.core.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.kettle.core.resource.Resource;
import io.kettle.core.resource.ResourceKey;
import io.kettle.core.resource.extension.DefinitionResourceSpec;
import io.kettle.core.resource.type.ResourceType;

@ApplicationScoped
public class KettleResourcesService implements ResourcesService {

    private Map<String, DefinitionResourceSpec> coreResourcesCache = new HashMap<>();

    @Inject
    Instance<ResourcesRepository> repositories;

    private ResourcesRepository repository;
    private String persistenceConfig;

    @Override
    public void setupPersistence(String connectionString) {
//        if (persistenceConfig != null && !persistenceConfig.equals(connectionString)) {
//            throw new IllegalStateException("Persistence config cannot be changed");
//        } else if (persistenceConfig != null && persistenceConfig.equals(connectionString) && repository != null) {
//            return;
//        }
        if (persistenceConfig != null && persistenceConfig.equals(connectionString)) {
            return;
        }
        persistenceConfig = connectionString;
        RuntimeModificatorConnectionConfigSource.setupProperties(persistenceConfig);
        List<ResourcesRepository> repos = repositories.stream()
            .filter(r -> r.isCompatible(persistenceConfig))
            .collect(Collectors.toList());
        if (repos.isEmpty()) {
            throw new IllegalStateException("There is no repository for persistence config " + persistenceConfig);
        } else if (repos.size() > 1) {
            throw new IllegalStateException("There is more than one matching repo for config " + persistenceConfig);
        } else {
            repository = repos.get(0);
        }
    }

    private ResourcesRepository repository() {
        if (persistenceConfig == null) {
            throw new IllegalStateException("Persistence configuration is not set");
        }
        return repository;
    }

    @Override
    public void cacheCoreResource(DefinitionResourceSpec definition) {
        coreResourcesCache.put(definition.getNames().getPlural(), definition);
        coreResourcesCache.put(definition.getNames().getKind(), definition);
        coreResourcesCache.put(definition.getNames().getSingular(), definition);
        Optional.ofNullable(definition.getShortNames())
            .orElseGet(Collections::emptyList)
            .forEach(s -> {
                coreResourcesCache.put(s, definition);
            });
    }

    @Override
    public DefinitionResourceSpec getDefinitionResource(String name) {
        DefinitionResourceSpec definition = coreResourcesCache.get(name);
        if ( definition == null ) {
            return repository().findDefinitionResourceByNames(name);
        }
        return definition;
    }

    @Override
    public void createResource(ResourceType resourceType, Resource resource) {
        repository().createResource(resourceType, resource);
    }

    @Override
    public void updateResource(ResourceType resourceType, Resource resource) {
        repository().updateResource(resourceType, resource);
    }

    @Override
    public Resource deleteResource(ResourceKey key) {
        return repository().deleteResource(key);
    }

    @Override
    public Resource getResource(ResourceKey key) {
        return repository().getResource(key);
    }

    @Override
    public List<Resource> doNamespacedQuery(String apiVersion, String kind, String namespace) {
        return repository().list(apiVersion, kind, namespace);
    }

    @Override
    public List<Resource> doGlobalQuery(String apiVersion, String kind) {
        return repository().list(apiVersion, kind, null);
    }

}
