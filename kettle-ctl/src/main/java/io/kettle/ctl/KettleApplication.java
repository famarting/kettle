package io.kettle.ctl;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kettle.core.storage.ResourcesService;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class KettleApplication implements QuarkusApplication{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    ResourcesService repo;

    @Override
    public int run(String... args) throws Exception {
        log.info("Running from CLI");
        return new KettleRequestHandler(repo).handle(args) ? 0 : 1;
    }
}