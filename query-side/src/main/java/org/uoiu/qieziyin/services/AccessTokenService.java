package org.uoiu.qieziyin.services;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import org.bson.types.ObjectId;
import schemas.AccessTokenSchemaType;

import java.time.Instant;

public class AccessTokenService implements com.github.aesteve.vertx.nubes.services.Service {
  private static final Logger log = LoggerFactory.getLogger(AccessTokenService.class);

  public static final String SERVICE_NAME = "accessTokenService";

  private MongoClient mongoService;
  private Vertx vertx;

  public AccessTokenService(MongoClient mongoService) {
    this.mongoService = mongoService;
  }

  @Override
  public void init(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public void start(Future<Void> future) {
    future.complete();
  }

  @Override
  public void stop(Future<Void> future) {
    future.complete();
  }

  public void access(String clientIp, String userAgent, String username,
                     Handler<AsyncResult<String>> resultHandler) {
    String tokenId = ObjectId.get().toHexString();

    Future<Void> future = Future.future();
    checkDelete(clientIp, userAgent, username, future.completer());

    future.setHandler(v -> {
      JsonObject document = new JsonObject()
        .put(AccessTokenSchemaType.clientIp, clientIp)
        .put(AccessTokenSchemaType.userAgent, userAgent)
        .put(AccessTokenSchemaType.username, username)
        .put(AccessTokenSchemaType.accessedAt, Instant.now())
        .put(AccessTokenSchemaType.token, tokenId)
        .put(AccessTokenSchemaType._id, tokenId);

      mongoService.insert(AccessTokenSchemaType.COLLECTION_NAME,
        document,
        result -> {
          if (result.succeeded()) {
            resultHandler.handle(Future.succeededFuture(tokenId));
          } else {
            resultHandler.handle(Future.failedFuture(result.cause()));
          }
        });
    });
  }

  public void checkDelete(String clientIp, String userAgent, String username,
                          Handler<AsyncResult<Void>> resultHandler) {
    JsonObject query = new JsonObject()
      .put(AccessTokenSchemaType.clientIp, clientIp)
      .put(AccessTokenSchemaType.userAgent, userAgent)
      .put(AccessTokenSchemaType.username, username);

    mongoService.removeDocument(AccessTokenSchemaType.COLLECTION_NAME,
      query,
      result -> {
        if (result.succeeded()) {
          resultHandler.handle(Future.succeededFuture());
        } else {
          resultHandler.handle(Future.failedFuture(result.cause()));
        }
      });
  }

}
