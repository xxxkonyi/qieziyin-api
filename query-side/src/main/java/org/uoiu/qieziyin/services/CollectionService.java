package org.uoiu.qieziyin.services;

import com.github.aesteve.vertx.nubes.annotations.services.Consumer;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import org.bson.types.ObjectId;
import org.uoiu.qieziyin.events.CollectionEventType;
import org.uoiu.qieziyin.schemas.CollectionSchemaType;

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

  @Consumer(CollectionEventType.COLLECTION_CREATED)
  public void created(Message<JsonObject> eventMessage) {
    JsonObject payload = eventMessage.body();

    JsonObject collection = payload.copy();
    if (Objects.isNull(payload.getString(CollectionSchemaType._id))) {
      String collectionId = ObjectId.get().toHexString();
      collection.put(CollectionSchemaType._id, collectionId);
    }
//    collection.put(CollectionSchemaType.eventDate, new JsonObject().put("$date", payload.getInstant(CollectionSchemaType.eventDate)));


    mongoService.insert(CollectionSchemaType.COLLECTION_NAME, collection, result -> {
      if (result.succeeded()) {
        log.debug("created handle succeeded");
        eventMessage.reply(collection.getString(CollectionSchemaType._id));
      } else {
        log.error("created handle failed:{}", result.cause().getMessage());
        eventMessage.fail(500, result.cause().getMessage());
      }
    });

  }

  @Consumer(CollectionEventType.COLLECTION_CHANGED)
  public void changed(Message<JsonObject> eventMessage) {
    JsonObject payload = eventMessage.body();

    String collectionId = payload.getString(CollectionSchemaType._id);
    JsonObject collection = payload.copy();

    JsonObject query = new JsonObject().put(CollectionSchemaType._id, collectionId);
    JsonObject update = new JsonObject().put("$set", collection);

    mongoService.updateCollection(CollectionSchemaType.COLLECTION_NAME, query, update, result -> {
      if (result.succeeded()) {
        log.debug("changed handle succeeded");
        eventMessage.reply(collectionId);
      } else {
        log.error("changed handle failed:{}", result.cause().getMessage());
        eventMessage.fail(500, result.cause().getMessage());
      }
    });

  }

  @Consumer(CollectionEventType.COLLECTION_DELETED)
  public void deleted(Message<JsonObject> eventMessage) {
    JsonObject payload = eventMessage.body();

    String collectionId = payload.getString(CollectionSchemaType._id);

    JsonObject query = new JsonObject().put(CollectionSchemaType._id, collectionId);

    mongoService.removeDocument(CollectionSchemaType.COLLECTION_NAME, query, result -> {
      if (result.succeeded()) {
        log.debug("deleted handle succeeded");
        eventMessage.reply(collectionId);
      } else {
        log.error("deleted handle failed:{}", result.cause().getMessage());
        eventMessage.fail(500, result.cause().getMessage());
      }
    });

  }

}
