package io.kettle.ctl.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KettleConfig {

    public static class Cluster {
        @JsonProperty("certificate-authority")
        public String certificateAuthority;

        @JsonProperty("certificate-authority-data")
        public String certificateAuthorityData;

        public String server;

        public String getServer() {
            return server;
        }
    }

    public static class ClusterReference {
        public String name;
        public Cluster cluster;
    }

    public static class ContextReference {
        public Context context;
        public String name;
    }

    public static class Context {
        public String cluster;
        public String namespace;
        public String user;
    }

    public static class UserReference {
        public String name;
        public User user;
    }

    public static class User {
        @JsonProperty("client-certificate")
        public String clientCertificate;

        @JsonProperty("client-certificate-data")
        public String clientCertificateData;

        @JsonProperty("client-key")
        public String clientKey;

        @JsonProperty("client-key-data")
        public String clientKeyData;

        @JsonProperty("auth-provider")
        public AuthProviderReference authProvider;

        @JsonProperty("as-user-extra")
        public Map<String, Object> asUserExtra;
    }

    public static class AuthProviderReference {
        public String name;
        public AuthProvider config;
    }

    public static class AuthProvider {
        @JsonProperty("access-token")
        public String accessToken;

        @JsonProperty("cmd-args")
        public String cmdArgs;

        @JsonProperty("cmd-path")
        public String cmdPath;

        public String expiry;

        @JsonProperty("expiry-key")
        public String expiryKey;

        @JsonProperty("token-key")
        public String tokenKey;
    }


    public String apiVersion;

    public List<ClusterReference> clusters = new ArrayList<>();

    public List<ContextReference> contexts = new ArrayList<>();

    @JsonProperty("current-context")
    public String currentContext;

    public String kind;

    public Map preferences = new HashMap<>();

    public List<UserReference> users = new ArrayList<>();

    @JsonIgnore
    public Context getCurrentContext() {
        for (ContextReference cr : contexts)
            if (cr.name.equals(currentContext))
                return cr.context;
        throw new IllegalStateException("There is no context called '" + currentContext + "'.");
    }

    @JsonIgnore
    public Cluster getCluster() {
        Context ctx = getCurrentContext();
        for (ClusterReference cr : clusters)
            if (cr.name.equals(ctx.cluster))
                return cr.cluster;
        throw new IllegalStateException("There is no context called '" + ctx.cluster + "'.");
    }

    @JsonIgnore
    public User getUser() {
        Context ctx = getCurrentContext();
        if (ctx.user == null)
            return null;
        for (UserReference cr : users)
            if (cr.name.equals(ctx.user))
                return cr.user;
        throw new IllegalStateException("There is no user called '" + ctx.user + "'.");
    }

}