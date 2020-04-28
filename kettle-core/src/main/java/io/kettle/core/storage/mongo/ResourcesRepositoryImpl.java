package io.kettle.core.storage.mongo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoQueryException;

import io.kettle.core.KettleConstants;
import io.kettle.core.KettleUtils;
import io.kettle.core.resource.Resource;
import io.kettle.core.resource.ResourceKey;
import io.kettle.core.resource.extension.DefinitionResourceSpec;
import io.kettle.core.resource.extension.ResourceScope;
import io.kettle.core.resource.type.ResourceType;
import io.kettle.core.storage.ResourcesRepository;
import io.quarkus.panache.common.Parameters;

/**
 * MongoResourcesRepository
 */
@ApplicationScoped
public class ResourcesRepositoryImpl implements ResourcesRepository {

    private Logger log = LoggerFactory.getLogger(this.getClass());

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
            Parameters parameters = Parameters.with("apiVersion", KettleUtils.formatApiVersion(KettleConstants.CORE_API_GROUP, KettleConstants.CORE_API_VERSION))
                    .and("kind", KettleConstants.DEFINITION_RESOURCE_KIND)
                    .and("pluralName", pluralName);
            String[] queries = new String[] {
                            "apiVersion = :apiVersion and kind = :kind and spec.names.plural = :pluralName",
                            "apiVersion = :apiVersion and kind = :kind and spec.names.kind = :pluralName",
                            "apiVersion = :apiVersion and kind = :kind and spec.names.singular = :pluralName",
                            "apiVersion = :apiVersion and kind = :kind and spec.shortNames = :pluralName"
                            };
            for (String q : queries) {
                log.info("Trying query {}", q);
                Resource resource = repository.find(q, parameters).firstResult();
                if ( resource != null ) {
                    return new DefinitionResourceSpec(resource.getSpec());
                }
            }
            return null;
//            Resource resource = repository.find(query, parameters).firstResult();
//            if ( resource != null ) {
//                definition = new DefinitionResourceSpec(resource.getSpec());
//            } else {
//                query = "apiVersion = :apiVersion and kind = :kind and spec.names.kind = :pluralName";
//                resource = repository.find(query, parameters).firstResult();
//                if ( resource != null ) {
//                    definition = new DefinitionResourceSpec(resource.getSpec());
//                }
//            }
        }
        return definition;
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

}
