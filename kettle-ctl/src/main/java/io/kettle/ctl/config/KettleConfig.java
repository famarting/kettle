package io.kettle.ctl.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@JsonInclude(Include.NON_NULL)
public class KettleConfig {

    @RegisterForReflection
    @JsonInclude(Include.NON_NULL)
    public static class Cluster {
        @JsonProperty("connection-string")
        public String connectionString;
    }

    @JsonInclude(Include.NON_NULL)
    @RegisterForReflection
    public static class ClusterReference {
        public String name;
        public Cluster cluster;
    }

    @JsonInclude(Include.NON_NULL)
    @RegisterForReflection
    public static class ContextReference {
        public Context context;
        public String name;
    }

    @JsonInclude(Include.NON_NULL)
    @RegisterForReflection
    public static class Context {
        public String cluster;
        public String namespace;
        public String user;
    }

    @JsonInclude(Include.NON_NULL)
    @RegisterForReflection
    public static class UserReference {
        public String name;
        public User user;
    }

    @JsonInclude(Include.NON_NULL)
    @RegisterForReflection
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

    @JsonInclude(Include.NON_NULL)
    @RegisterForReflection
    public static class AuthProviderReference {
        public String name;
        public AuthProvider config;
    }

    @JsonInclude(Include.NON_NULL)
    @RegisterForReflection
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
        throw new IllegalStateException("There is no cluster called '" + ctx.cluster + "'.");
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
