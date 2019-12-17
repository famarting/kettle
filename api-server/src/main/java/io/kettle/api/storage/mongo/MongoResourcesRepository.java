package io.kettle.api.storage.mongo;

import javax.enterprise.context.ApplicationScoped;

import io.kettle.api.resource.Resource;
import io.quarkus.mongodb.panache.PanacheMongoRepository;

/**
 * MongoResourcesRepository
 */
@ApplicationScoped
public class MongoResourcesRepository implements PanacheMongoRepository<Resource> {

}
