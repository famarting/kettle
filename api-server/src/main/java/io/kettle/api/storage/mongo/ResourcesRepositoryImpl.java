package io.kettle.api.storage.mongo;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kettle.api.ApiServerRequestContext;
import io.kettle.api.ApiServerUtils;
import io.kettle.api.resource.Resource;
import io.kettle.api.resource.ResourceKey;
import io.kettle.api.resource.extension.ResourceScope;
import io.kettle.api.resource.type.ResourceType;
import io.kettle.api.storage.ResourcesRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;

/**
 * MongoResourcesRepository
 */
@ApplicationScoped
public class ResourcesRepositoryImpl implements ResourcesRepository {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
    @Inject
    MongoResourcesRepository repository;

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
        if (key.type.scope() == ResourceScope.Namespaced) {
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

    
}