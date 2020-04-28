package io.kettle.core.storage.mongo;

import javax.enterprise.context.ApplicationScoped;

import io.kettle.core.resource.Resource;
import io.quarkus.mongodb.panache.PanacheMongoRepository;

/**
 * MongoResourcesRepository
 */
@ApplicationScoped
public class MongoResourcesRepository implements PanacheMongoRepository<Resource> {

}
