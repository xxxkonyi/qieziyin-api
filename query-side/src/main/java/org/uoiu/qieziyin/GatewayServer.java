package org.uoiu.qieziyin;

import com.github.aesteve.vertx.nubes.NubesServer;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import org.uoiu.qieziyin.common.Constants;
import org.uoiu.qieziyin.controllers.AccessTokenAuthProvider;
import org.uoiu.qieziyin.services.AccessTokenService;
import org.uoiu.qieziyin.services.CollectionService;
import org.uoiu.qieziyin.services.UserService;

import java.util.Objects;

public class GatewayServer extends NubesServer {

  protected MongoClient mongoService;
  protected MongoAuth authProvider;

  @Override
  public void start(Future<Void> future) {
    JsonObject mongoConfig = ((JsonObject) vertx.sharedData().getLocalMap(Constants.CONFIG_NAME).get("config"))
      .getJsonObject("mongo");
    String mongodbConnection = System.getenv(Constants.ENV_MONGODB_CONNECTION);
    if (Objects.nonNull(mongodbConnection)) {
      mongoConfig.put("connection_string", mongodbConnection);
    }
    String mongodbName = System.getenv(Constants.ENV_MONGODB_DB_NAME);
    if (Objects.nonNull(mongodbName)) {
      mongoConfig.put("db_name", mongodbName);
    }
    mongoService = MongoClient.createShared(vertx, mongoConfig);
    nubes.registerService(Constants.MONGO_SERVICE_NAME, mongoService);

    JsonObject authProperties = new JsonObject();
    authProvider = new AccessTokenAuthProvider(mongoService, authProperties, authProvider);
    nubes.registerService(Constants.AUTH_PROVIDER_SERVICE_NAME, authProvider);

    nubes.setAuthProvider(authProvider);

    registerService();

    super.start(future);
  }

  private void registerService() {
    nubes.registerService(UserService.SERVICE_NAME, new UserService(mongoService, authProvider));
    nubes.registerService(AccessTokenService.SERVICE_NAME, new AccessTokenService(mongoService));
    nubes.registerService(CollectionService.SERVICE_NAME, new CollectionService());
  }

}
