package org.uoiu.qieziyin.controllers;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.auth.mongo.impl.MongoAuthImpl;
import io.vertx.ext.mongo.MongoClient;
import org.uoiu.qieziyin.schemas.AccessTokenSchemaType;
import org.uoiu.qieziyin.schemas.UserSchemaType;

import java.text.MessageFormat;
import java.util.Objects;

public class AccessTokenAuthProvider extends MongoAuthImpl {
  private static final Logger log = LoggerFactory.getLogger(AccessTokenAuthProvider.class);

  private MongoClient mongoService;
  private MongoAuth authProvider;

  public AccessTokenAuthProvider(MongoClient mongoClient, JsonObject config, MongoAuth authProvider) {
    super(mongoClient, config);

    this.mongoService = mongoClient;
    this.authProvider = authProvider;
  }

  @Override
  public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
    String accessToken = authInfo.getString("access_token");

    if (accessToken == null) {
      super.authenticate(authInfo, resultHandler);
      return;
    }

    Future<Void> startFuture = Future.future();
    startFuture
      .compose(v -> {
        log.debug("1 find accessToken");

        Future<String> future = Future.future();
        mongoService.findOne(AccessTokenSchemaType.COLLECTION_NAME,
          new JsonObject().put(AccessTokenSchemaType.token, accessToken),
          null,
          result -> {
            if (result.succeeded()) {
              if (Objects.isNull(result.result())) {
                future.fail("accessToken [" + accessToken + "] 不存在");
                return;
              }

              future.complete(result.result().getString(AccessTokenSchemaType.username));
            } else {
              future.fail(result.cause());
            }
          });
        return future;
      })
      .compose(username -> {
        log.debug("2 find user", username);

        Future<AccessTokenUser> future = Future.future();
        mongoService.findOne(UserSchemaType.COLLECTION_NAME,
          new JsonObject().put(UserSchemaType.username, username),
          null,
          result -> {
            if (result.succeeded()) {
              if (Objects.isNull(result.result())) {
                String message = MessageFormat.format("accessToken [{0}] user [{1}] 不存在", accessToken, username);
                log.error(message);
                future.fail(message);
                return;
              }

              future.complete(new AccessTokenUser(result.result(), authProvider));
            } else {
              future.fail(result.cause());
            }
          });
        return future;
      })
      .setHandler(result -> {
        if (result.succeeded()) {
          log.debug("over succeeded");
          resultHandler.handle(Future.succeededFuture(result.result()));
        } else {
          log.debug("over failed:{}", result.cause().getMessage());
          resultHandler.handle(Future.failedFuture(result.cause()));
        }
      })
    ;

    startFuture.complete();
  }

}
