package org.uoiu.qieziyin.fixtures;

import com.github.aesteve.vertx.nubes.annotations.services.Service;
import com.github.aesteve.vertx.nubes.fixtures.Fixture;
import com.google.common.collect.Lists;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import org.uoiu.qieziyin.common.Constants;
import org.uoiu.qieziyin.events.UserEventType;
import org.uoiu.qieziyin.schemas.ProfileSchemaType;
import org.uoiu.qieziyin.schemas.UserSchemaType;
import org.uoiu.qieziyin.services.UserService;

import java.util.List;

public class UserFixture implements Fixture {
  private static final Logger log = LoggerFactory.getLogger(UserFixture.class);

  @Service(Constants.MONGO_SERVICE_NAME)
  private MongoClient mongoService;
  @Service(UserService.SERVICE_NAME)
  private UserService userService;

  private Vertx vertx;

  @Override
  public int executionOrder() {
    return 0;
  }

  @Override
  public void startUp(Vertx vertx, Future<Void> future) {
    log.info("start");
    this.vertx = vertx;

    CompositeFuture.all(Lists.newArrayList(initTestUsers())).setHandler(result -> {
      if (result.succeeded()) {
        future.complete();
      } else {
        future.fail(result.cause());
      }
    });
  }

  @Override
  public void tearDown(Vertx vertx, Future<Void> future) {
    future.complete();
  }

  private List<Future<Void>> initTestUsers() {
    log.info("initTestUsers");
//    mongoService.dropCollection(UserSchemaType.COLLECTION_NAME, handler -> {
//    });
//    mongoService.dropCollection(ProfileSchemaType.COLLECTION_NAME, handler -> {
//    });

    List<JsonObject> users = createUserList();
    List<Future<Void>> futures = Lists.newArrayListWithCapacity(users.size());

    for (int i = 0; i < users.size(); i++) {
      futures.add(initUser(users.get(i)));
    }

    return futures;
  }

  private List<JsonObject> createUserList() {
    List<JsonObject> users = Lists.newArrayList();
    users.add(new JsonObject()
      .put(UserSchemaType.username, "yuanfang")
      .put(UserSchemaType.password, "123")
      .put(UserSchemaType.roles, new JsonArray().add(Constants.Role.USER))
      .put(ProfileSchemaType.name, "元芳")
    );
    users.add(new JsonObject()
      .put(UserSchemaType.username, "xuzu")
      .put(UserSchemaType.password, "123")
      .put(UserSchemaType.roles, new JsonArray().add(Constants.Role.USER))
      .put(ProfileSchemaType.name, "虚竹")
    );
    users.add(new JsonObject()
      .put(UserSchemaType.username, "xiaobao")
      .put(UserSchemaType.password, "123")
      .put(UserSchemaType.roles, new JsonArray().add(Constants.Role.USER))
      .put(ProfileSchemaType.name, "小宝")
    );
    users.add(new JsonObject()
      .put(UserSchemaType.username, "daqiao")
      .put(UserSchemaType.password, "123")
      .put(UserSchemaType.roles, new JsonArray().add(Constants.Role.USER))
      .put(ProfileSchemaType.name, "大乔")
    );

    users.add(new JsonObject()
      .put(UserSchemaType.username, "bangzhu")
      .put(UserSchemaType.password, "sa")
      .put(ProfileSchemaType.name, "帮主")
      .put(ProfileSchemaType.bio, "盖世 帮主")
      .put(UserSchemaType.roles, new JsonArray().add(Constants.Role.USER).add(Constants.Role.ADMIN))
    );
    return users;
  }

  private Future initUser(JsonObject user) {
    String username = user.getString(UserSchemaType.username);

    Future<Message<String>> future = Future.future();
    mongoService.findOne(UserSchemaType.COLLECTION_NAME,
      new JsonObject().put(UserSchemaType.username, username),
      new JsonObject().put(UserSchemaType.username, Constants.EMPTY_STRING),
      result -> {
        if (result.succeeded()) {
          if (result.result() == null) {
            vertx.eventBus().send(UserEventType.USER_CREATED, user, future.completer());
          } else {
            future.complete();
          }
        } else {
          future.fail(result.cause());
        }
      });
    return future;
  }

}
