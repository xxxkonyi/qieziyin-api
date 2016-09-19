package org.uoiu.qieziyin.init.services;

import com.google.common.collect.Lists;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import org.uoiu.qieziyin.api.UserEventType;
import org.uoiu.qieziyin.common.Constants;
import org.uoiu.qieziyin.schemas.CollectionSchemaType;
import org.uoiu.qieziyin.schemas.ProfileSchemaType;
import org.uoiu.qieziyin.schemas.UserSchemaType;
import org.uoiu.qieziyin.services.UserService;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class UserInitService implements com.github.aesteve.vertx.nubes.services.Service {
  private static final Logger log = LoggerFactory.getLogger(UserInitService.class);

  public static final String SERVICE_NAME = "userInitService";

  private MongoClient mongoService;
  private UserService userService;
  private CollectionInitService collectionInitService;

  private Vertx vertx;

  public UserInitService(MongoClient mongoService,
                         UserService userService,
                         CollectionInitService collectionInitService) {
    this.mongoService = mongoService;
    this.userService = userService;
    this.collectionInitService = collectionInitService;
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

  public void initTestUsers(Handler<AsyncResult<Void>> resultHandler) {
    log.info("initTestUsers");

    List<JsonObject> users = createUserList();
    List<Future> futures = Lists.newArrayListWithCapacity(users.size());

    for (JsonObject user : users) {
      Future<Boolean> future = Future.future();
      initUser(user, future.completer());
      futures.add(future);
      log.debug(user.toString());
    }

    CompositeFuture.all(futures).setHandler(result -> {
      if (result.succeeded()) {
        resultHandler.handle(Future.succeededFuture());
      } else {
        resultHandler.handle(Future.failedFuture(result.cause()));
      }
    });
  }

  private List<JsonObject> createUserList() {
    int size = 3;
    List<JsonObject> users = Lists.newArrayList();

    users.add(new JsonObject()
      .put(UserSchemaType._id, "bangzhu")
      .put(UserSchemaType.username, "bangzhu")
      .put(UserSchemaType.password, "sa")
      .put(ProfileSchemaType.name, "帮主")
      .put(ProfileSchemaType.bio, "盖世 帮主")
      .put(UserSchemaType.roles, new JsonArray().add(Constants.Role.USER).add(Constants.Role.ADMIN))
      .put(UserSchemaType.createdAt, Instant.parse("2016-05-01T10:15:30.00Z").plus(Duration.ofDays(1)))
    );

    for (int i = 0; i < size; i++) {
      String userId = MessageFormat.format("test-{0}", i);
      users.add(new JsonObject()
        .put(UserSchemaType._id, userId)
        .put(UserSchemaType.username, userId)
        .put(UserSchemaType.password, "123")
        .put(ProfileSchemaType.name, MessageFormat.format("测试-{0}", i))
        .put(ProfileSchemaType.bio, MessageFormat.format("我是测试 {0} ，我只是用来测试的。", i))
        .put(UserSchemaType.roles, new JsonArray().add(Constants.Role.USER))
        .put(UserSchemaType.createdAt, Instant.parse("2016-05-01T10:15:30.00Z").plus(Duration.ofDays(1)))
      );
    }
    return users;
  }

  private void initUser(JsonObject collection, Handler<AsyncResult<Boolean>> resultHandler) {
    String _id = collection.getString(CollectionSchemaType._id);

    Future<Void> startFuture = Future.future();
    startFuture
      .compose(v -> {

        Future<Boolean> future = Future.future();
        mongoService.findOne(UserSchemaType.COLLECTION_NAME,
          new JsonObject().put(UserSchemaType.username, _id),
          new JsonObject().put(UserSchemaType.username, true),
          result -> {
            if (result.succeeded()) {
              future.complete(Objects.isNull(result.result()));
            } else {
              future.fail(result.cause());
            }
          });
        return future;
      })
      .compose(v -> {
        Future<Void> future = Future.future();
        if (v) {
          vertx.eventBus().send(UserEventType.CREATE_USER, collection, result -> {
            if (result.succeeded()) {
              collectionInitService.initTestCollections(
                collection.getString(CollectionSchemaType._id),
                future.completer()
              );
            } else {
              future.fail(result.cause());
            }
          });
        } else {
          future.complete();
        }
        return future;
      })
      .setHandler(result -> {
        if (result.succeeded()) {
          resultHandler.handle(Future.succeededFuture());
        } else {
          resultHandler.handle(Future.failedFuture(result.cause()));
        }
      })
    ;

    startFuture.complete();
  }

}
