package io.kettle.ctl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@Tag("filesystem")
@QuarkusTest
public class KettleFilesystemSmokeTest extends SmokeTestBase {

    private static final Logger log = Logger.getLogger(KettleFilesystemSmokeTest.class);

    @Override
    String kettleConfigPath() {
        return "src/test/resources/kettle-config-filesystem.yaml";
    }

    @BeforeEach
    private void createStorageDir() throws IOException {
        Files.createDirectories(Paths.get("/tmp/kettle-test/"));
    }

    @AfterEach
    private void cleanup() throws IOException {
        FileUtils.deleteDirectory(Paths.get("/tmp/kettle-test/").toFile());
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
        doTestUseContext("file:///tmp/kettle-test");
    }

}