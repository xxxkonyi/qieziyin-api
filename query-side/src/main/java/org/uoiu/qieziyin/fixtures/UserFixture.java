package org.uoiu.qieziyin.fixtures;

import com.github.aesteve.vertx.nubes.annotations.services.Service;
import com.github.aesteve.vertx.nubes.fixtures.Fixture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.uoiu.qieziyin.init.services.UserInitService;

import java.util.Objects;

public class UserFixture implements Fixture {
  private static final Logger log = LoggerFactory.getLogger(UserFixture.class);

  @Service(UserInitService.SERVICE_NAME)
  private UserInitService userInitService;

  private Vertx vertx;

  @Override
  public int executionOrder() {
    return 0;
  }

  @Override
  public void startUp(Vertx vertx, Future<Void> future) {
    log.info("start");
    this.vertx = vertx;

//    userInitService.initTestUsers(result -> {
//      if (result.succeeded()) {
//        future.complete();
//      } else {
//        future.fail(result.cause());
//      }
//    });
    future.complete();
  }

  @Override
  public void tearDown(Vertx vertx, Future<Void> future) {
    future.complete();
  }


}
