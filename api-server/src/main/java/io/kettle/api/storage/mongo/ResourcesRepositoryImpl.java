package io.kettle.api.storage.mongo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.kettle.api.ApiResourcesManager;
import io.kettle.api.ApiServerUtils;
import io.kettle.api.resource.Resource;
import io.kettle.api.resource.ResourceKey;
import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.resource.extension.ResourceScope;
import io.kettle.api.resource.type.ResourceType;
import io.kettle.api.storage.ResourcesRepository;
import io.quarkus.panache.common.Parameters;

/**
 * MongoResourcesRepository
 */
@ApplicationScoped
public class ResourcesRepositoryImpl implements ResourcesRepository {

    @Inject
    MongoResourcesRepository repository;

    private Map<String, DefinitionResourceSpec> coreResourcesCache = new HashMap<>();

    @Override
    public Resource deleteResource(ResourceKey key) {
        Resource resource = getResource(key);
        repository.delete(resource);
        return resource;
    }

    @Override
    public void createResource(ResourceType resourceType, Resource resource) {
        repository.persist(resource);
    }

    @Override
    public void updateResource(ResourceType resourceType, Resource resource) {
        repository.update(resource);
    }

    @Override
    public Resource getResource(ResourceKey key) {
        Parameters parameters = Parameters.with("apiVersion", key.apiVersion)
                .and("kind", key.kind)
                .and("name", key.name);
        String query = "apiVersion = :apiVersion and kind = :kind and metadata.name = :name";
        if ( key.type.scope() == ResourceScope.Namespaced ) {
            query += " and metadata.namespace = :namespace";
            parameters.and("namespace", key.type.namespace());
        }
        return repository.find(query, parameters).firstResult();
    }

    @Override
    public List<Resource> doNamespacedQuery(String apiVersion, String kind, String namespace) {
        Parameters parameters = Parameters.with("apiVersion", apiVersion)
                .and("kind", kind)
                .and("namespace", namespace);
        String query = "apiVersion = :apiVersion and kind = :kind and metadata.namespace = :namespace";
        return repository.find(query, parameters).list();
    }

    @Override
    public List<Resource> doGlobalQuery(String apiVersion, String kind) {
        Parameters parameters = Parameters.with("apiVersion", apiVersion)
                .and("kind", kind);
        String query = "apiVersion = :apiVersion and kind = :kind";
        return repository.find(query, parameters).list();
    }

    @Override
    public DefinitionResourceSpec getDefinitionResource(String pluralName) {
        DefinitionResourceSpec definition = coreResourcesCache.get(pluralName);
        if ( definition == null ) {
            Parameters parameters = Parameters.with("apiVersion", ApiServerUtils.formatApiVersion(ApiResourcesManager.CORE_API_GROUP, ApiResourcesManager.CORE_API_VERSION))
                    .and("kind", ApiResourcesManager.DEFINITION_RESOURCE_KIND)
                    .and("pluralName", pluralName);
            String query = "apiVersion = :apiVersion and kind = :kind and spec.names.plural = :pluralName";
            Resource resource = repository.find(query, parameters).firstResult();
            if ( resource != null ) {
                definition = new DefinitionResourceSpec(resource.getSpec());
            }
        }
        return definition;
    }

    @Override
    public void cacheCoreResource(DefinitionResourceSpec definition) {
        coreResourcesCache.put(definition.getNames().getPlural(), definition);
    }

}
