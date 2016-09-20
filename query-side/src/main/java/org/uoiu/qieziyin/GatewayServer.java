package org.uoiu.qieziyin;

import com.github.aesteve.vertx.nubes.NubesServer;
import com.google.common.collect.Sets;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.auth.mongo.impl.MongoAuthImpl;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.handler.CorsHandler;
import org.uoiu.qieziyin.common.Constants;
import org.uoiu.qieziyin.init.services.CollectionInitService;
import org.uoiu.qieziyin.init.services.UserInitService;
import org.uoiu.qieziyin.services.*;

import java.util.Objects;

public class GatewayServer extends NubesServer {
  private static final Logger log = LoggerFactory.getLogger(GatewayMain.class);

  protected MongoClient mongoService;
  protected MongoAuth authProvider;

  @Override
  public void start(Future<Void> future) {
    options.setLogActivity(true);

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
    log.info("mongo config:{}", mongoConfig);
    mongoService = MongoClient.createShared(vertx, mongoConfig);
    nubes.registerService(Constants.MONGO_SERVICE_NAME, mongoService);

    JsonObject authProperties = new JsonObject();
    authProvider = new MongoAuthImpl(mongoService, authProperties);
    nubes.registerService(Constants.AUTH_PROVIDER_SERVICE_NAME, authProvider);


    JsonObject config = new JsonObject().put("keyStore", new JsonObject()
      .put("path", "keystore.jceks")
      .put("type", "jceks")
      .put("password", "secret"));
    JWTAuth jwtAuth = JWTAuth.create(vertx, config);
    nubes.registerService(Constants.JWT_AUTH_SERVICE_NAME, jwtAuth);
    nubes.setAuthProvider(jwtAuth);

    registerService();

    nubes.addGlobalHandler(CorsHandler.create("*")
      .allowedMethods(Sets.newHashSet(HttpMethod.POST, HttpMethod.GET)));

    super.start(future);
  }

  private void registerService() {
    nubes.registerService(CommandGatewayService.SERVICE_NAME, new CommandGatewayService());
    nubes.registerService(UserService.SERVICE_NAME, new UserService(mongoService, authProvider));
    nubes.registerService(ProfileService.SERVICE_NAME, new ProfileService(mongoService));
    nubes.registerService(CollectionService.SERVICE_NAME, new CollectionService(mongoService));
    nubes.registerService(ImpressionService.SERVICE_NAME, new ImpressionService(mongoService));

    nubes.registerService(CollectionInitService.SERVICE_NAME, new CollectionInitService(mongoService,
      (CollectionService) nubes.getService(CollectionService.SERVICE_NAME),
      (ImpressionService) nubes.getService(ImpressionService.SERVICE_NAME)));
    nubes.registerService(UserInitService.SERVICE_NAME, new UserInitService(mongoService,
      (UserService) nubes.getService(UserService.SERVICE_NAME),
      (CollectionInitService) nubes.getService(CollectionInitService.SERVICE_NAME)));

  }

}
