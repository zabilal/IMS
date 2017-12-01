package com.nova.services;

import com.nova.data.UserApi;
import com.nova.data.UserDAO;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

public class RestServer extends AbstractVerticle {

    public static final String APPLICATION_JSON = "application/json";
    private HttpServer server;
    private MongoClient mongo;

    private UserDAO userDAO;
    private UserApi userApi;
    private JsonObject config;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        config = context.config();
    }

    @Override
    public void start(Future<Void> future) {

        mongo = MongoClient.createShared(vertx, config.getJsonObject("mongo"));

        userDAO = new UserDAO(mongo);
        userApi = new UserApi(userDAO);

        server = vertx.createHttpServer(createOptions());
        server.requestHandler(createRouter()::accept);
        server.listen(result -> {
            if (result.succeeded()) {
                future.complete();
                System.out.println("REST Server Running on PORT : " + server.actualPort());
            } else {
                future.fail(result.cause());
            }
        });
    }

    @Override
    public void stop(Future<Void> future) {
        if (server == null) {
            future.complete();
            return;
        }
        server.close(future.completer());
    }

    private static HttpServerOptions createOptions() {
        HttpServerOptions options = new HttpServerOptions();
        options.setHost("localhost");
        options.setPort(9000);
        return options;
    }

    private Router createRouter() {

        Router router = Router.router(vertx);
        router.route().failureHandler(ErrorHandler.create(true));

		/* Session / cookies for users */
        router.route().handler(CookieHandler.create());
        SessionStore sessionStore = LocalSessionStore.create(vertx);
        SessionHandler sessionHandler = SessionHandler.create(sessionStore);
        router.route().handler(sessionHandler);

		/* API */
        router.mountSubRouter("/api/v1", apiRouter());


        return router;
    }

    private Router apiRouter() {

        Router router = Router.router(vertx);
        router.route().consumes(APPLICATION_JSON);
        router.route().produces(APPLICATION_JSON);
        router.route().handler(BodyHandler.create());
        router.route().handler(context -> {
            context.response().headers().add(CONTENT_TYPE, APPLICATION_JSON);
            context.next();
        });

        router.post("/user").handler(userApi::newUserApi);
        router.get("/user").handler(userApi::getAllUserApi);

        return router;
    }
}
