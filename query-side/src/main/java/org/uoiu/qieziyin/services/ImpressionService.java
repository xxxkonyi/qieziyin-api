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
import org.uoiu.qieziyin.api.ImpressionEventType;
import org.uoiu.qieziyin.schemas.CollectionSchemaType;
import org.uoiu.qieziyin.schemas.ImpressionSchemaType;

import java.util.List;
import java.util.Objects;

public class ImpressionService implements com.github.aesteve.vertx.nubes.services.Service {
  private static final Logger log = LoggerFactory.getLogger(ImpressionService.class);

  public static final String SERVICE_NAME = "impressionService";

  private MongoClient mongoService;
  private Vertx vertx;

  public ImpressionService(MongoClient mongoService) {
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

  @Consumer(ImpressionEventType.CREATE_IMPRESSION)
  public void create(Message<JsonObject> eventMessage) {
    JsonObject payload = eventMessage.body();

    JsonObject impression = payload.copy();
    if (Objects.isNull(payload.getString(ImpressionSchemaType._id))) {
      String impressionId = ObjectId.get().toHexString();
      impression.put(ImpressionSchemaType._id, impressionId);
    }
//    collection.put(ImpressionSchemaType.eventDate, new JsonObject().put("$date", payload.getInstant(ImpressionSchemaType.eventDate)));


    mongoService.insert(ImpressionSchemaType.COLLECTION_NAME, impression, result -> {
      if (result.succeeded()) {
        log.debug("create handle succeeded");
        eventMessage.reply(result.result());
      } else {
        log.error("create handle failed:{}", result.cause().getMessage());
        eventMessage.fail(500, result.cause().getMessage());
      }
    });

  }

  @Consumer(ImpressionEventType.CHANGE_IMPRESSION)
  public void change(Message<JsonObject> eventMessage) {
    JsonObject payload = eventMessage.body();

    String impressionId = payload.getString(ImpressionSchemaType._id);
    JsonObject impression = payload.copy();

    JsonObject query = new JsonObject().put(ImpressionSchemaType._id, impressionId);
    JsonObject update = new JsonObject().put("$set", impression);

    mongoService.updateCollection(ImpressionSchemaType.COLLECTION_NAME, query, update, result -> {
      if (result.succeeded()) {
        log.debug("change handle succeeded");
        eventMessage.reply(null);
      } else {
        log.error("change handle failed:{}", result.cause().getMessage());
        eventMessage.fail(500, result.cause().getMessage());
      }
    });

  }

  @Consumer(ImpressionEventType.DELETE_IMPRESSION)
  public void delete(Message<JsonObject> eventMessage) {
    JsonObject payload = eventMessage.body();

    String impressionId = payload.getString(ImpressionSchemaType._id);

    JsonObject query = new JsonObject().put(ImpressionSchemaType._id, impressionId);

    mongoService.removeDocument(ImpressionSchemaType.COLLECTION_NAME, query, result -> {
      if (result.succeeded()) {
        log.debug("delete handle succeeded");
        eventMessage.reply(null);
      } else {
        log.error("delete handle failed:{}", result.cause().getMessage());
        eventMessage.fail(500, result.cause().getMessage());
      }
    });

  }

  @Consumer(ImpressionEventType.COLLECTION_IMPRESSION)
  public void collection(Message<JsonObject> eventMessage) {
    String currentUserId = eventMessage.headers().get("currentUserId");
    JsonObject payload = eventMessage.body();

    String collectionId = payload.getString(ImpressionSchemaType.collectionId);

    Future<Void> startFuture = Future.future();
    startFuture
      .compose(v -> {
        log.debug("1 validate collection [{}]", collectionId);

        Future<String> future = Future.future();
        mongoService.findOne(CollectionSchemaType.COLLECTION_NAME,
          new JsonObject().put(CollectionSchemaType._id, collectionId),
          new JsonObject().put(CollectionSchemaType._id, true)
            .put(CollectionSchemaType.creatorId, true)
            .put(CollectionSchemaType.publicType, true),
          result -> {
            if (result.succeeded()) {
              JsonObject collection = result.result();
              if (Objects.isNull(collection)) {
                log.debug("one handle succeeded not found");
                future.fail("印集不存在");
              }

              if (!CollectionSchemaType.checkPubilcType(collection, currentUserId)) {
                log.debug("one handle succeeded checkPubilcType");
                future.fail("私有印集不能访问");
              }

              log.debug("one handle succeeded");
              future.complete();
            } else {
              log.error("one handle failed:{}", result.cause().getMessage());
              eventMessage.fail(500, result.cause().getMessage());
            }
          }

        );
        return future;
      })
      .compose(v -> {
        log.debug("2 find impression [{}]", collectionId);

        Future<List<JsonObject>> future = Future.future();
        JsonObject query = new JsonObject().put(ImpressionSchemaType.collectionId, collectionId);
        mongoService.find(ImpressionSchemaType.COLLECTION_NAME, query, future.completer());
        return future;
      })
      .setHandler(result -> {
        if (result.succeeded()) {
          log.debug("over succeeded");
          eventMessage.reply(result.result());
        } else {
          log.error("over failed:{}", result.cause().getMessage());
          eventMessage.fail(500, result.cause().getMessage());
        }
      })
    ;

    startFuture.complete();

  }

}
