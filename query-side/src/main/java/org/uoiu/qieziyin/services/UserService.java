package org.uoiu.qieziyin.services;

import com.github.aesteve.vertx.nubes.annotations.services.Consumer;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.mongo.HashSaltStyle;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.auth.mongo.impl.DefaultHashStrategy;
import io.vertx.ext.auth.mongo.impl.MongoUser;
import io.vertx.ext.mongo.MongoClient;
import org.bson.types.ObjectId;
import org.uoiu.qieziyin.events.UserEventType;
import org.uoiu.qieziyin.schemas.ProfileSchemaType;
import org.uoiu.qieziyin.schemas.UserSchemaType;

public class UserService implements com.github.aesteve.vertx.nubes.services.Service {
  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  public static final String SERVICE_NAME = "userService";

  private MongoClient mongoService;
  private MongoAuth authProvider;
  private Vertx vertx;

  public UserService(MongoClient mongoService, MongoAuth authProvider) {
    this.mongoService = mongoService;
    this.authProvider = authProvider;
  }

  @Override
  public void init(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
  }

  @Override
  public void start(Future<Void> future) {
    log.info("start");

    future.complete();
  }

  @Override
  public void stop(Future<Void> future) {
    future.complete();
  }

  @Consumer(UserEventType.USER_CREATED)
  public void saveUser(Message<JsonObject> eventMessage) {
    JsonObject payload = eventMessage.body();

    String userId = ObjectId.get().toHexString();
    String username = payload.getString(UserSchemaType.username);

    Future<Void> startFuture = Future.future();
    startFuture
      .compose(v -> {
        log.debug("1 insertUser [{}]", username);

        Future<String> future = Future.future();
        insertUser(userId, payload, future.completer());
        return future;
      })
      .compose(v -> {
        log.debug("2 insertProfile [{}]", username);

        Future<String> future = Future.future();
        insertProfile(userId, payload, future.completer());
        return future;
      })
      .setHandler(result -> {
        if (result.succeeded()) {
          log.debug("over succeeded");
        } else {
          log.debug("over failed:{}", result.cause().getMessage());
        }
      })
    ;

    startFuture.complete();

  }

  public void insertUser(String userId, JsonObject payload,
                         Handler<AsyncResult<String>> resultHandler) {
    String password = payload.getString(UserSchemaType.password);

    JsonObject principal = new JsonObject()
      .put(UserSchemaType.username, payload.getString(UserSchemaType.username))
      .put(UserSchemaType.password, password)
      .put(UserSchemaType.roles, payload.getJsonArray(UserSchemaType.roles))
      .put(UserSchemaType.permissions, payload.getJsonArray(UserSchemaType.permissions))
      .put(UserSchemaType._id, userId);

    MongoUser user = new MongoUser(principal, authProvider);

    if (authProvider.getHashStrategy().getSaltStyle() == HashSaltStyle.COLUMN) {
      principal.put(UserSchemaType.salt, DefaultHashStrategy.generateSalt());
    }

    String cryptPassword = authProvider.getHashStrategy().computeHash(password, user);
    principal.put(UserSchemaType.password, cryptPassword);

    mongoService.insert(UserSchemaType.COLLECTION_NAME, user.principal(), resultHandler);
  }

  public void insertProfile(String userId, JsonObject payload,
                            Handler<AsyncResult<String>> resultHandler) {
    JsonObject profile = new JsonObject()
      .put(ProfileSchemaType.name, payload.getString(ProfileSchemaType.name))
      .put(ProfileSchemaType.bio, payload.getString(ProfileSchemaType.bio))
      .put(ProfileSchemaType._id, userId);

    mongoService.insert(ProfileSchemaType.COLLECTION_NAME, profile, resultHandler);
  }

}
