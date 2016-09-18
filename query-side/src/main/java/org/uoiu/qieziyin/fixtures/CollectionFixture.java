package org.uoiu.qieziyin.fixtures;

import com.github.aesteve.vertx.nubes.annotations.services.Service;
import com.github.aesteve.vertx.nubes.fixtures.Fixture;
import com.google.common.collect.Lists;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;
import org.uoiu.qieziyin.common.Constants;
import org.uoiu.qieziyin.api.CollectionEventType;
import org.uoiu.qieziyin.schemas.CollectionSchemaType;
import org.uoiu.qieziyin.services.CollectionService;

import java.time.Instant;
import java.util.List;

public class CollectionFixture implements Fixture {
  private static final Logger log = LoggerFactory.getLogger(CollectionFixture.class);

  @Service(Constants.MONGO_SERVICE_NAME)
  private MongoClient mongoService;
  @Service(CollectionService.SERVICE_NAME)
  private CollectionService collectionService;

  private Vertx vertx;

  @Override
  public int executionOrder() {
    return 1;
  }

  @Override
  public void startUp(Vertx vertx, Future<Void> future) {
    log.info("start");
    this.vertx = vertx;

    CompositeFuture.all(Lists.newArrayList(initTestCollections())).setHandler(result -> {
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

  private List<Future<Void>> initTestCollections() {
    log.info("initTestCollections");

    List<JsonObject> collections = createCollectionList();
    List<Future<Void>> futures = Lists.newArrayListWithCapacity(collections.size());

    for (int i = 0; i < collections.size(); i++) {
      futures.add(initCollection(collections.get(i)));
      log.debug(collections.get(i).toString());
    }

    return futures;
  }

  private List<JsonObject> createCollectionList() {

    List<JsonObject> collections = Lists.newArrayList();
    collections.add(new JsonObject()
      .put(CollectionSchemaType._id, "1")
      .put(CollectionSchemaType.title, "呼伦贝尔草原行")
      .put(CollectionSchemaType.description, "")
      .put(CollectionSchemaType.cover, "https://images.unsplash.com/photo-1422651355218-53453822ebb8?dpr=1&auto=compress,format&crop=entropy&fit=max&w=576&q=80&cs=tinysrgb")
      .put(CollectionSchemaType.eventDate, Instant.now())
      .put(CollectionSchemaType.publicType, CollectionSchemaType.COLLECTION_PUBLIC_TYPE_PUBLIC)
      .put(CollectionSchemaType.creatorId, "bangzhu")
    );
    collections.add(new JsonObject()
      .put(CollectionSchemaType._id, "2")
      .put(CollectionSchemaType.title, "呼伦贝尔草原行")
      .put(CollectionSchemaType.description, "")
      .put(CollectionSchemaType.cover, "https://images.unsplash.com/photo-1473700216830-7e08d47f858e?dpr=1&auto=compress,format&crop=entropy&fit=max&w=576&q=80&cs=tinysrgb")
      .put(CollectionSchemaType.eventDate, Instant.now())
      .put(CollectionSchemaType.publicType, CollectionSchemaType.COLLECTION_PUBLIC_TYPE_PUBLIC)
      .put(CollectionSchemaType.creatorId, "bangzhu")
    );
    collections.add(new JsonObject()
      .put(CollectionSchemaType._id, "3")
      .put(CollectionSchemaType.title, "呼伦贝尔草原行")
      .put(CollectionSchemaType.description, "")
      .put(CollectionSchemaType.cover, "https://images.unsplash.com/photo-1473122430480-d00e6dd25ba8?dpr=1&auto=compress,format&crop=entropy&fit=max&w=576&q=80&cs=tinysrgb")
      .put(CollectionSchemaType.eventDate, Instant.now())
      .put(CollectionSchemaType.publicType, CollectionSchemaType.COLLECTION_PUBLIC_TYPE_PUBLIC)
      .put(CollectionSchemaType.creatorId, "bangzhu")
    );
    collections.add(new JsonObject()
      .put(CollectionSchemaType._id, "4")
      .put(CollectionSchemaType.title, "呼伦贝尔草原行")
      .put(CollectionSchemaType.description, "")
      .put(CollectionSchemaType.cover, "https://images.unsplash.com/photo-1434434319959-1f886517e1fe?dpr=1&auto=compress,format&crop=entropy&fit=max&w=576&q=80&cs=tinysrgb")
      .put(CollectionSchemaType.eventDate, Instant.now())
      .put(CollectionSchemaType.publicType, CollectionSchemaType.COLLECTION_PUBLIC_TYPE_PUBLIC)
      .put(CollectionSchemaType.creatorId, "bangzhu")
    );
    collections.add(new JsonObject()
      .put(CollectionSchemaType._id, "5")
      .put(CollectionSchemaType.title, "呼伦贝尔草原行")
      .put(CollectionSchemaType.description, "")
      .put(CollectionSchemaType.cover, "https://images.unsplash.com/photo-1459478309853-2c33a60058e7?dpr=1&auto=compress,format&crop=entropy&fit=max&w=576&q=80&cs=tinysrgb")
      .put(CollectionSchemaType.eventDate, Instant.now())
      .put(CollectionSchemaType.publicType, CollectionSchemaType.COLLECTION_PUBLIC_TYPE_PUBLIC)
      .put(CollectionSchemaType.creatorId, "bangzhu")
    );
    collections.add(new JsonObject()
      .put(CollectionSchemaType._id, "6")
      .put(CollectionSchemaType.title, "呼伦贝尔草原行")
      .put(CollectionSchemaType.description, "")
      .put(CollectionSchemaType.cover, "https://images.unsplash.com/photo-1443890484047-5eaa67d1d630?dpr=1&auto=compress,format&crop=entropy&fit=max&w=576&q=80&cs=tinysrgb")
      .put(CollectionSchemaType.eventDate, Instant.now())
      .put(CollectionSchemaType.publicType, CollectionSchemaType.COLLECTION_PUBLIC_TYPE_PUBLIC)
      .put(CollectionSchemaType.creatorId, "bangzhu")
    );
    collections.add(new JsonObject()
      .put(CollectionSchemaType._id, "7")
      .put(CollectionSchemaType.title, "呼伦贝尔草原行123")
      .put(CollectionSchemaType.description, "")
      .put(CollectionSchemaType.cover, "https://images.unsplash.com/photo-1472723650373-ac4ad70e38d6?dpr=1&auto=compress,format&crop=entropy&fit=max&w=576&q=80&cs=tinysrgb")
      .put(CollectionSchemaType.eventDate, Instant.now())
      .put(CollectionSchemaType.publicType, CollectionSchemaType.COLLECTION_PUBLIC_TYPE_PUBLIC)
      .put(CollectionSchemaType.creatorId, "bangzhu")
    );
    return collections;
  }

  private Future initCollection(JsonObject collection) {
    String _id = collection.getString(CollectionSchemaType._id);

    Future<Message<String>> future = Future.future();
    mongoService.findOne(CollectionSchemaType.COLLECTION_NAME,
      new JsonObject().put(CollectionSchemaType._id, _id),
      new JsonObject().put(CollectionSchemaType._id, true),
      result -> {
        if (result.succeeded()) {
          if (result.result() == null) {
            vertx.eventBus().send(CollectionEventType.CREATE_COLLECTION, collection, future.completer());
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
