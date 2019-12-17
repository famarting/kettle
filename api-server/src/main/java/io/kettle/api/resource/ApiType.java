package io.kettle.api.resource;

public enum ApiType {

    API_SERVICE("/api"),
    API_GROUP("/apis");

    private String basePath;

    private ApiType(String basePath) {
        this.basePath = basePath;
    }

    public String basePath() {
        return this.basePath;
    }

}