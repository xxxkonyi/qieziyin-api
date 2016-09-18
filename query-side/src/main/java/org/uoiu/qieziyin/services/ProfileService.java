package org.uoiu.qieziyin.services;

import com.github.aesteve.vertx.nubes.annotations.services.Consumer;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import org.uoiu.qieziyin.api.ProfileEventType;
import org.uoiu.qieziyin.common.Constants;
import org.uoiu.qieziyin.schemas.ProfileSchemaType;

import java.util.Objects;

public class ProfileService implements com.github.aesteve.vertx.nubes.services.Service {
  private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

  public static final String SERVICE_NAME = "profileService";

  private MongoClient mongoService;
  private MongoAuth authProvider;
  private Vertx vertx;

  public ProfileService(MongoClient mongoService, MongoAuth authProvider) {
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

  @Consumer(ProfileEventType.CHANGE_PROFILE)
  public void change(Message<JsonObject> eventMessage) {
    JsonObject payload = eventMessage.body();

    String profileId = payload.getString(ProfileSchemaType._id);
    JsonObject profile = payload.copy();

    JsonObject query = new JsonObject().put(ProfileSchemaType._id, profileId);
    JsonObject update = new JsonObject().put("$set", profile);

    mongoService.updateCollection(ProfileSchemaType.COLLECTION_NAME, query, update, result -> {
      if (result.succeeded()) {
        log.debug("change handle succeeded");
        eventMessage.reply(null);
      } else {
        log.error("change handle failed:{}", result.cause().getMessage());
        eventMessage.fail(500, result.cause().getMessage());
      }
    });

  }

  @Consumer(ProfileEventType.ONE_PROFILE)
  public void one(Message<JsonObject> eventMessage) {
    JsonObject payload = eventMessage.body();

    String profileId = payload.getString(ProfileSchemaType._id);

    JsonObject fields = Objects.isNull(payload) && payload.size() == 0 ? null : payload.copy();
    JsonObject query = new JsonObject().put(ProfileSchemaType._id, profileId);

    mongoService.findOne(ProfileSchemaType.COLLECTION_NAME, query, fields, result -> {
      if (result.succeeded()) {
        log.debug("one handle succeeded");
        eventMessage.reply(result.result());
      } else {
        log.error("one handle failed:{}", result.cause().getMessage());
        eventMessage.fail(500, result.cause().getMessage());
      }
    });

  }

}
