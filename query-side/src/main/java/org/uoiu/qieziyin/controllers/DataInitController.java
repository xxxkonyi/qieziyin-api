package org.uoiu.qieziyin.controllers;

import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.auth.Auth;
import com.github.aesteve.vertx.nubes.annotations.mixins.ContentType;
import com.github.aesteve.vertx.nubes.annotations.routing.http.POST;
import com.github.aesteve.vertx.nubes.annotations.services.Service;
import com.github.aesteve.vertx.nubes.auth.AuthMethod;
import com.github.aesteve.vertx.nubes.marshallers.Payload;
import com.google.common.collect.Lists;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.collections.CollectionUtils;
import org.uoiu.qieziyin.common.Constants;
import org.uoiu.qieziyin.init.services.UserInitService;

import java.util.List;

@Controller("/dataInit")
@ContentType("application/json")
public class DataInitController {
  private static final Logger log = LoggerFactory.getLogger(DataInitController.class);

  @Service(Constants.MONGO_SERVICE_NAME)
  private MongoClient mongoService;
  @Service(UserInitService.SERVICE_NAME)
  private UserInitService userInitService;

  @POST("/dropCollection")
  @Auth(authority = Constants.Role.SUPER_ADMIN, method = AuthMethod.JWT)
  public void dropCollection(Payload<JsonObject> payload, RoutingContext context) {
    Future<Void> startFuture = Future.future();
    startFuture
      .compose(v -> {
        log.debug("1 find collections");

        Future<List<String>> future = Future.future();
        mongoService.getCollections(future.completer());
        return future;
      })
      .compose(v -> {
        log.debug("2 drop collections [{}]", v);

        Future<Void> future = Future.future();
        if (CollectionUtils.isEmpty(v)) {
          future.complete();
          return future;
        }
        List<Future> dropCollectionFutures = Lists.newArrayListWithCapacity(v.size());

        v.stream().forEach(t -> {
          Future<Void> dropCollectionFuture = Future.future();
          mongoService.dropCollection(t, dropCollectionFuture.completer());
          dropCollectionFutures.add(dropCollectionFuture);
        });

        CompositeFuture.all(dropCollectionFutures).setHandler(result -> {
          if (result.succeeded()) {
            future.complete();
          } else {
            future.fail(result.cause());
          }
        });

        return future;
      })
      .setHandler(result -> {
        if (result.succeeded()) {
          log.debug("over succeeded");
          context.next();
        } else {
          log.error("over failed:{}", result.cause().getMessage());
          context.fail(result.cause());
        }
      })
    ;

    startFuture.complete();

  }

  @POST("/init")
  @Auth(authority = Constants.Role.SUPER_ADMIN, method = AuthMethod.JWT)
  public void init(Payload<JsonObject> payload, RoutingContext context) {
    Future<Void> startFuture = Future.future();
    startFuture
      .compose(v -> {
        log.debug("1 init");

        Future<Void> future = Future.future();
        userInitService.initTestUsers(future.completer());
        return future;
      })
      .setHandler(result -> {
        if (result.succeeded()) {
          log.debug("over succeeded");
          context.next();
        } else {
          log.error("over failed:{}", result.cause().getMessage());
          context.fail(result.cause());
        }
      })
    ;

    startFuture.complete();

  }

}