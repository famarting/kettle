package io.kettle.ctl.config;

import java.util.stream.Stream;

public enum KettleConfigOperations {

    setCluster("set-cluster"),
    setContext("set-context"),
    useContext("use-context");

    private String word;

    private KettleConfigOperations(String word) {
        this.word = word;
    }

    public static KettleConfigOperations fromValue(String word) {
        return Stream.of(KettleConfigOperations.values())
            .filter(o -> o.word.equals(word))
            .findFirst()
            .orElse(null);
    }

}