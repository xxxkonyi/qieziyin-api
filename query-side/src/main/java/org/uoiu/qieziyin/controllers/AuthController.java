package org.uoiu.qieziyin.controllers;

import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.auth.Auth;
import com.github.aesteve.vertx.nubes.annotations.auth.User;
import com.github.aesteve.vertx.nubes.annotations.mixins.ContentType;
import com.github.aesteve.vertx.nubes.annotations.params.Param;
import com.github.aesteve.vertx.nubes.annotations.routing.http.GET;
import com.github.aesteve.vertx.nubes.annotations.services.Service;
import com.github.aesteve.vertx.nubes.auth.AuthMethod;
import com.github.aesteve.vertx.nubes.handlers.impl.DefaultErrorHandler;
import com.github.aesteve.vertx.nubes.marshallers.Payload;
import io.vertx.core.Future;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.auth.jwt.impl.JWTUser;
import io.vertx.ext.auth.mongo.AuthenticationException;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;
import org.uoiu.qieziyin.common.Constants;
import org.uoiu.qieziyin.schemas.ProfileSchemaType;
import org.uoiu.qieziyin.schemas.UserSchemaType;

import java.util.Objects;

@Controller("/auth")
@ContentType("application/json")
public class AuthController {
  private static final Logger log = LoggerFactory.getLogger(AuthController.class);

  @Service("authProvider")
  private MongoAuth authProvider;
  @Service(Constants.MONGO_SERVICE_NAME)
  private MongoClient mongoService;
  @Service("jwtAuth")
  private JWTAuth jwtAuth;

  @GET("/login")
  public void login(@Param("username") String username,
                    @Param("password") String password,
                    Payload<JsonObject> payload, RoutingContext context) {

    Future<JsonObject> startFuture = Future.future();
    startFuture
      .compose(v -> {
        log.debug("1 authenticate");

        Future<io.vertx.ext.auth.User> future = Future.future();
        authProvider.authenticate(v, future.completer());
        return future;
      })
      .compose(v -> {
        log.debug("2 accessToken 生成");

        Future<String> future = Future.future();
        String clientIp = context.request().remoteAddress().host();

        log.info("user [{}] ip [{}] login", username, clientIp);

        JsonObject user = v.principal();

        JsonObject claims = new JsonObject()
          .put(UserSchemaType.username, user.getString(UserSchemaType.username));
        JWTOptions options = new JWTOptions()
          .setSubject(user.getString(UserSchemaType._id))
          .setPermissions(user.getJsonArray("roles").getList());
        String token = jwtAuth.generateToken(claims, options);
        future.complete(token);

        return future;
      })
      .setHandler(result -> {
        if (result.succeeded()) {
          log.debug("over succeeded");
          payload.set(new JsonObject().put("token", result.result()));
          context.next();
        } else {
          log.debug("over failed:{}", result.cause().getMessage());

          Throwable cause = result.cause();
          if (cause instanceof AuthenticationException) {
            if (cause.getMessage().startsWith("Invalid username/password")) {
              DefaultErrorHandler.badRequest(context, ("用户名或密码错误"));
              return;
            }
          }

          context.fail(cause);
        }
      })
    ;

    JsonObject authInfo = new JsonObject()
      .put("username", username)
      .put("password", password);
    startFuture.complete(authInfo);

  }

  @GET("/current")
  @Auth(authority = Constants.Role.USER, method = AuthMethod.JWT)
  public void getApi(@User JWTUser user,
                     Payload<JsonObject> payload, RoutingContext context) {
    String userId = user.principal().getString("sub");
    mongoService.findOne(ProfileSchemaType.COLLECTION_NAME,
      new JsonObject().put(ProfileSchemaType._id, userId),
      null,
      result -> {
        if (result.succeeded()) {
          if (Objects.isNull(result.result())) {
            context.fail(new NoStackTraceThrowable("userId [" + userId + "] 不存在"));
            return;
          }

          payload.set(result.result());
          context.next();
        }
      });
  }

}
