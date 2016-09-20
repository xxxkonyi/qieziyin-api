package org.uoiu.qieziyin.init.services;

import com.google.common.collect.Lists;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import org.apache.commons.lang3.RandomUtils;
import org.uoiu.qieziyin.api.CollectionEventType;
import org.uoiu.qieziyin.api.ImpressionEventType;
import org.uoiu.qieziyin.schemas.CollectionSchemaType;
import org.uoiu.qieziyin.schemas.ImpressionSchemaType;
import org.uoiu.qieziyin.services.CollectionService;
import org.uoiu.qieziyin.services.ImpressionService;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class CollectionInitService implements com.github.aesteve.vertx.nubes.services.Service {
  private static final Logger log = LoggerFactory.getLogger(CollectionInitService.class);

  public static final String SERVICE_NAME = "collectionInitService";

  private MongoClient mongoService;
  private CollectionService collectionService;
  private ImpressionService impressionService;

  private Vertx vertx;

  public CollectionInitService(MongoClient mongoService,
                               CollectionService collectionService, ImpressionService impressionService) {
    this.mongoService = mongoService;
    this.collectionService = collectionService;
    this.impressionService = impressionService;
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

  public void initTestCollections(String creatorId, Handler<AsyncResult<Void>> resultHandler) {
    log.info("initTestCollections");

    int size = RandomUtils.nextInt(0, 5);
    List<JsonObject> collections = createCollectionList(creatorId, size);
    List<Future> futures = Lists.newArrayListWithCapacity(collections.size());

    for (JsonObject collection : collections) {
      Future<Boolean> future = Future.future();
      initCollection(collection, future.completer());
      futures.add(future);
      log.debug(collection.toString());
    }

    CompositeFuture.all(futures).setHandler(result -> {
      if (result.succeeded()) {
        resultHandler.handle(Future.succeededFuture());
      } else {
        resultHandler.handle(Future.failedFuture(result.cause()));
      }
    });
  }

  private List<JsonObject> createCollectionList(String creatorId, int size) {
    List<JsonObject> collections = Lists.newArrayListWithCapacity(size);
    for (int i = 0; i < size; i++) {
      collections.add(new JsonObject()
        .put(ImpressionSchemaType._id, MessageFormat.format("{0}--{1}", creatorId, i))
        .put(CollectionSchemaType.title, MessageFormat.format("活动[{0}]的标题", i))
        .put(CollectionSchemaType.description, MessageFormat.format("这是一段活动[{0}]的描述", i))
        .put(CollectionSchemaType.cover, "https://images.unsplash.com/photo-1422651355218-53453822ebb8?dpr=1&auto=compress,format&crop=entropy&fit=max&w=576&q=80&cs=tinysrgb")
        .put(CollectionSchemaType.eventDate, Instant.parse("2016-06-01T10:15:30.00Z").plus(Duration.ofDays(i + 7)))
        .put(CollectionSchemaType.publicType,
          Boolean.valueOf(String.valueOf(RandomUtils.nextInt(0, 2)))
            ? CollectionSchemaType.COLLECTION_PUBLIC_TYPE_PUBLIC
            : CollectionSchemaType.COLLECTION_PUBLIC_TYPE_PRIVATE
        )
        .put(CollectionSchemaType.creatorId, creatorId)
      );
    }
    return collections;
  }

  private void initCollection(JsonObject collection, Handler<AsyncResult<Boolean>> resultHandler) {
    String _id = collection.getString(CollectionSchemaType._id);

    Future<Void> startFuture = Future.future();
    startFuture
      .compose(v -> {

        Future<Boolean> future = Future.future();
        mongoService.findOne(CollectionSchemaType.COLLECTION_NAME,
          new JsonObject().put(CollectionSchemaType._id, _id),
          new JsonObject().put(CollectionSchemaType._id, true),
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
          vertx.eventBus().send(CollectionEventType.CREATE_COLLECTION, collection, result -> {
            if (result.succeeded()) {
              initTestImpressions(
                collection.getString(CollectionSchemaType.creatorId),
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

  private void initTestImpressions(String creatorId, String collectionId, Handler<AsyncResult<Void>> resultHandler) {
    log.info("initTestImpressions");

    int size = RandomUtils.nextInt(5, 21);
    List<JsonObject> impressions = createImpressionList(creatorId, collectionId, size);
    List<Future> futures = Lists.newArrayListWithCapacity(impressions.size());

    for (JsonObject impression : impressions) {
      Future<Boolean> future = Future.future();
      initImpression(impression, future.completer());
      futures.add(future);
      log.debug(impression.toString());
    }

    CompositeFuture.all(futures).setHandler(result -> {
      if (result.succeeded()) {
        resultHandler.handle(Future.succeededFuture());
      } else {
        resultHandler.handle(Future.failedFuture(result.cause()));
      }
    });
  }

  private List<JsonObject> createImpressionList(String creatorId, String collectionId, int size) {

    List<JsonObject> impressions = Lists.newArrayListWithCapacity(size);
    for (int i = 0; i < size; i++) {
      impressions.add(new JsonObject()
        .put(ImpressionSchemaType._id, MessageFormat.format("{0}--{1}--{2}", creatorId, collectionId, i))
        .put(CollectionSchemaType.title, MessageFormat.format("发生[{0}]的标题", i))
        .put(CollectionSchemaType.description, MessageFormat.format("这是一段发生[{0}]的描述", i))
        .put(ImpressionSchemaType.content, "https://images.unsplash.com/photo-1422651355218-53453822ebb8?dpr=1&auto=compress,format&crop=entropy&fit=max&w=576&q=80&cs=tinysrgb")
        .put(ImpressionSchemaType.eventDate, Instant.parse("2016-06-01T10:15:30.00Z").plus(Duration.ofHours(i + 1)))
        .put(ImpressionSchemaType.creatorId, creatorId)
        .put(ImpressionSchemaType.collectionId, collectionId)
      );
    }
    return impressions;
  }

  private void initImpression(JsonObject impression, Handler<AsyncResult<Boolean>> resultHandler) {
    String _id = impression.getString(ImpressionSchemaType._id);

    Future<Void> startFuture = Future.future();
    startFuture
      .compose(v -> {

        Future<Boolean> future = Future.future();
        mongoService.findOne(ImpressionSchemaType.COLLECTION_NAME,
          new JsonObject().put(ImpressionSchemaType._id, _id),
          new JsonObject().put(ImpressionSchemaType._id, true),
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
        Future<Boolean> future = Future.future();
        if (v) {
          vertx.eventBus().send(ImpressionEventType.CREATE_IMPRESSION, impression, result -> {
            if (result.succeeded()) {
              future.complete(v);
            } else {
              future.fail(result.cause());
            }
          });
        } else {
          future.complete(v);
        }
        return future;
      })
      .setHandler(result -> {
        if (result.succeeded()) {
          resultHandler.handle(Future.succeededFuture(result.result()));
        } else {
          resultHandler.handle(Future.failedFuture(result.cause()));
        }
      })
    ;

    startFuture.complete();
  }

}
