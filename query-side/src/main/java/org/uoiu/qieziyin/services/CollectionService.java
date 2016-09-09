package org.uoiu.qieziyin.services;

import com.github.aesteve.vertx.nubes.annotations.services.Service;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class CollectionService implements com.github.aesteve.vertx.nubes.services.Service {
  private static final Logger log = LoggerFactory.getLogger(CollectionService.class);

  public static final String SERVICE_NAME = "collectionService";

  @Service("mongo")
  private MongoClient mongo;
  @Service("eventBus")
  private EventBus eventBus;
  private Vertx vertx;

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


}
