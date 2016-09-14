package org.uoiu.qieziyin.services;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class CommandGatewayService implements com.github.aesteve.vertx.nubes.services.Service {
  private static final Logger log = LoggerFactory.getLogger(CommandGatewayService.class);

  public static final String SERVICE_NAME = "commandGatewayService";

  private Vertx vertx;

  public CommandGatewayService() {

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

  public <T> void sendAndWait(String address, Object message, DeliveryOptions options, Handler<AsyncResult<Message<T>>> replyHandler) {
    vertx.eventBus().send(address, message, options, replyHandler);
  }

}
