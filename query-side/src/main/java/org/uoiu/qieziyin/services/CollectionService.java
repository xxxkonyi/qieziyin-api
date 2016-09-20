package org.uoiu.qieziyin.services;

import com.github.aesteve.vertx.nubes.annotations.services.Consumer;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import org.bson.types.ObjectId;
import org.uoiu.qieziyin.api.CollectionEventType;
import org.uoiu.qieziyin.schemas.CollectionSchemaType;
import org.uoiu.qieziyin.schemas.SchemaType;

import java.time.Instant;
import java.util.Objects;

public class CollectionService implements com.github.aesteve.vertx.nubes.services.Service {
  private static final Logger log = LoggerFactory.getLogger(CollectionService.class);

  public static final String SERVICE_NAME = "collectionService";

  private MongoClient mongoService;
  private Vertx vertx;

  public CollectionService(MongoClient mongoService) {
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

  @Consumer(CollectionEventType.CREATE_COLLECTION)
  public void create(Message<JsonObject> eventMessage) {
    JsonObject payload = eventMessage.body();

    JsonObject collection = payload.copy();
    if (Objects.isNull(payload.getString(CollectionSchemaType._id))) {
      String collectionId = ObjectId.get().toHexString();
      collection.put(CollectionSchemaType._id, collectionId);
    }
//    collection.put(CollectionSchemaType.eventDate, new JsonObject().put("$date", payload.getInstant(CollectionSchemaType.eventDate)));
    collection.put(SchemaType.createdAt, payload.getInstant(SchemaType.createdAt, Instant.now()));
    collection.put(SchemaType.updatedAt, payload.getInstant(SchemaType.updatedAt, Instant.now()));


    mongoService.insert(CollectionSchemaType.COLLECTION_NAME, collection, result -> {
      if (result.succeeded()) {
        log.debug("create handle succeeded");
        eventMessage.reply(result.result());
      } else {
        log.error("create handle failed:{}", result.cause().getMessage());
        eventMessage.fail(500, result.cause().getMessage());
      }
    });

  }

  @Consumer(CollectionEventType.CHANGE_COLLECTION)
  public void change(Message<JsonObject> eventMessage) {
    JsonObject payload = eventMessage.body();

    String collectionId = payload.getString(CollectionSchemaType._id);
    JsonObject collection = payload.copy();
    collection.put(SchemaType.updatedAt, payload.getInstant(SchemaType.updatedAt, Instant.now()));

    JsonObject query = new JsonObject().put(CollectionSchemaType._id, collectionId);
    JsonObject update = new JsonObject().put("$set", collection);

    mongoService.updateCollection(CollectionSchemaType.COLLECTION_NAME, query, update, result -> {
      if (result.succeeded()) {
        log.debug("change handle succeeded");
        eventMessage.reply(null);
      } else {
        log.error("change handle failed:{}", result.cause().getMessage());
        eventMessage.fail(500, result.cause().getMessage());
      }
    });

  }

  @Consumer(CollectionEventType.DELETE_COLLECTION)
  public void delete(Message<JsonObject> eventMessage) {
    JsonObject payload = eventMessage.body();

    String collectionId = payload.getString(CollectionSchemaType._id);

    JsonObject query = new JsonObject().put(CollectionSchemaType._id, collectionId);

    mongoService.removeDocument(CollectionSchemaType.COLLECTION_NAME, query, result -> {
      if (result.succeeded()) {
        log.debug("delete handle succeeded");
        eventMessage.reply(null);
      } else {
        log.error("delete handle failed:{}", result.cause().getMessage());
        eventMessage.fail(500, result.cause().getMessage());
      }
    });

  }

  @Consumer(CollectionEventType.YOURS_COLLECTION)
  public void yours(Message<JsonObject> eventMessage) {
    String currentUserId = eventMessage.headers().get("currentUserId");
    JsonObject payload = eventMessage.body();

    String creatorId = payload.getString(CollectionSchemaType.creatorId);

    JsonObject query = new JsonObject().put(CollectionSchemaType.creatorId, creatorId);
    if (Objects.nonNull(currentUserId)) {
      if (!Objects.equals(creatorId, currentUserId)) {
        query.put(CollectionSchemaType.publicType, CollectionSchemaType.COLLECTION_PUBLIC_TYPE_PUBLIC);
      }
    }

    FindOptions options = new FindOptions(payload.getJsonObject("options"));

    mongoService.findWithOptions(CollectionSchemaType.COLLECTION_NAME, query, options, result -> {
      if (result.succeeded()) {
        log.debug("yours handle succeeded");
        eventMessage.reply(new JsonObject().put("content", result.result()));
      } else {
        log.error("yours handle failed:{}", result.cause().getMessage());
        eventMessage.fail(500, result.cause().getMessage());
      }
    });

  }

  @Consumer(CollectionEventType.ONE_COLLECTION)
  public void one(Message<JsonObject> eventMessage) {
    String currentUserId = eventMessage.headers().get("currentUserId");
    JsonObject payload = eventMessage.body();

    String profileId = payload.getString(CollectionSchemaType._id);

    JsonObject fields = Objects.isNull(payload.getJsonObject("fields")) ? null : payload.copy();
    JsonObject query = new JsonObject().put(CollectionSchemaType._id, profileId);

    mongoService.findOne(CollectionSchemaType.COLLECTION_NAME, query, fields, result -> {
      if (result.succeeded()) {
        JsonObject collection = result.result();
        if (Objects.nonNull(collection) && Objects.nonNull(currentUserId)) {
          String creatorId = collection.getString(CollectionSchemaType.creatorId);
          if (!Objects.equals(creatorId, currentUserId)) {
            if (Objects.equals(collection.getString(CollectionSchemaType.publicType),
              CollectionSchemaType.COLLECTION_PUBLIC_TYPE_PRIVATE)) {
              log.debug("one handle succeeded private");
              eventMessage.fail(400, "私有印集不能访问");
            }
          }
        }

        log.debug("one handle succeeded");
        eventMessage.reply(collection);
      } else {
        log.error("one handle failed:{}", result.cause().getMessage());
        eventMessage.fail(500, result.cause().getMessage());
      }
    });

  }

}
