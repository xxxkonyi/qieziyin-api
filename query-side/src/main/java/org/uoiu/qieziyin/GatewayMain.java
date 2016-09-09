package org.uoiu.qieziyin;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import org.uoiu.qieziyin.common.Constants;

import java.util.Objects;

public class GatewayMain {

  static {
    System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
  }

  private static final Logger log = LoggerFactory.getLogger(GatewayMain.class);

  public final static int NB_INSTANCES = 1;

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    JsonObject config = vertx.fileSystem().readFileBlocking("config.json").toJsonObject();

    DeploymentOptions options = new DeploymentOptions();
    options.setInstances(NB_INSTANCES);

    JsonObject serverConfig = config.getJsonObject("server");
    String host = System.getenv(Constants.ENV_APP_HOST);
    if (Objects.nonNull(host)) {
      serverConfig.put("host", host);
    }
    String port = System.getenv(Constants.ENV_APP_PORT);
    if (Objects.nonNull(port)) {
      serverConfig.put("port", port);
    }
    log.info("server config:{}", serverConfig);
    options.setConfig(serverConfig);

    vertx.sharedData().getLocalMap(Constants.CONFIG_NAME).put("config", config);

    vertx.deployVerticle(GatewayServer.class.getName(), options,
      result -> {
        if (result.failed()) {
          log.error(result.cause());
          return;
        }
        log.info("deploy : {}", result.result());
        log.info("Listening at http://{}:{}", serverConfig.getString("host"), serverConfig.getInteger("port"));
      }
    );

  }

}
