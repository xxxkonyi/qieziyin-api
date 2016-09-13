package org.uoiu.qieziyin.fixtures;

import com.github.aesteve.vertx.nubes.annotations.services.Service;
import com.github.aesteve.vertx.nubes.fixtures.Fixture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.uoiu.qieziyin.services.CollectionService;

public class CollectionFixture implements Fixture {
  private static final Logger log = LoggerFactory.getLogger(CollectionFixture.class);

  @Service(CollectionService.SERVICE_NAME)
  private CollectionService collectionService;


  @Override
  public int executionOrder() {
    return 1;
  }

  @Override
  public void startUp(Vertx vertx, Future<Void> future) {
    future.complete();
  }

  @Override
  public void tearDown(Vertx vertx, Future<Void> future) {
    future.complete();
  }
}
