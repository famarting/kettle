package io.kettle.ctl;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@Tag("mongodb")
@QuarkusTest
@QuarkusTestResource(MongoTestResource.class)
public class KettleMongodbSmokeTest extends SmokeTestBase {

    private static final Logger log = Logger.getLogger(KettleMongodbSmokeTest.class);

    @Override
    String kettleConfigPath() {
        return "src/test/resources/kettle-config.yaml";
    }

    @Test
    void testKettle() throws Exception{
        doTestKettle();
    }

    @Test
    void testContextNoExists() throws Exception {
        doTestContextNoExists();
    }

    @Test
    void testUseContext() throws Exception {
        doTestUseContext("mongodb://localhost:27017");
    }

}