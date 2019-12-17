package io.kettle.api;

import io.vertx.ext.web.RoutingContext;

public interface RequestHandler {

    public void handle(RoutingContext ctx);


}
