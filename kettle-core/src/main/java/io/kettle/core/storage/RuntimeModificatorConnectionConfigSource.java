package io.kettle.core.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeModificatorConnectionConfigSource implements ConfigSource {

    private static final Logger log = LoggerFactory.getLogger(RuntimeModificatorConnectionConfigSource.class);

    private static Map<String, String> persistenceProperties;

    public static void setupProperties(String persistenceCfg) {
        log.info("Evaluating persistence config");
        Map<String, String> cfg = new HashMap<>();
        if (persistenceCfg.startsWith("mongodb://")) {
            cfg.put("quarkus.mongodb.connection-string", persistenceCfg);
        }
        persistenceProperties = Collections.unmodifiableMap(cfg);
        log.info("New persistence cfg: {}", persistenceProperties.toString());
    }

    @Override
    public Map<String, String> getProperties() {
        if (persistenceProperties == null) {
            return Collections.emptyMap();
        }
        return persistenceProperties;
    }

    @Override
    public String getValue(String propertyName) {
        return getProperties().get(propertyName);
    }

    @Override
    public String getName() {
        return "runtime-config-modifier";
    }

}
