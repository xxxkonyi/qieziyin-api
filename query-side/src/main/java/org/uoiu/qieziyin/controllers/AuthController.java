package org.uoiu.qieziyin.controllers;

import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.auth.Auth;
import com.github.aesteve.vertx.nubes.annotations.auth.User;
import com.github.aesteve.vertx.nubes.annotations.mixins.ContentType;
import com.github.aesteve.vertx.nubes.annotations.params.Header;
import com.github.aesteve.vertx.nubes.annotations.params.Param;
import com.github.aesteve.vertx.nubes.annotations.routing.http.GET;
import com.github.aesteve.vertx.nubes.annotations.services.Service;
import com.github.aesteve.vertx.nubes.auth.AuthMethod;
import com.github.aesteve.vertx.nubes.handlers.impl.DefaultErrorHandler;
import com.github.aesteve.vertx.nubes.marshallers.Payload;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.mongo.AuthenticationException;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;
import org.uoiu.qieziyin.services.AccessTokenService;
import org.uoiu.qieziyin.common.Constants;
import org.uoiu.qieziyin.schemas.ProfileSchemaType;
import org.uoiu.qieziyin.schemas.UserSchemaType;

import java.util.Objects;

@Controller("/auth")
@ContentType("application/json")
public class AuthController {
  private static final Logger log = LoggerFactory.getLogger(AuthController.class);

  @Service(AccessTokenService.SERVICE_NAME)
  private AccessTokenService accessTokenService;
  @Service("authProvider")
  private MongoAuth authProvider;
  @Service(Constants.MONGO_SERVICE_NAME)
  private MongoClient mongoService;

  @GET("/login")
  public void login(@Param("username") String username,
                    @Param("password") String password,
                    @Header("User-Agent") String userAgent,
                    Payload<JsonObject> payload, RoutingContext context) {

    Future<JsonObject> startFuture = Future.future();
    startFuture
      .compose(v -> {
        log.debug("1 authenticate");

        Future<Void> future = Future.future();
        authProvider.authenticate(v, result -> {
          if (result.succeeded()) {
            future.complete();
          } else {
            Throwable cause = result.cause();
            if (cause instanceof AuthenticationException) {
              if (cause.getMessage().startsWith("Invalid username/password")) {
                future.fail("用户名或密码错误");
                return;
              }
            }

            future.fail(cause);
          }
        });
        return future;
      })
      .compose(value -> {
        log.debug("2 accessToken 生成");

        Future<String> future = Future.future();
        String clientIp = context.request().remoteAddress().host();
        accessTokenService.access(clientIp, userAgent, username, future.completer());
        return future;
      })
      .setHandler(result -> {
        if (result.succeeded()) {
          log.debug("over succeeded");
          payload.set(new JsonObject().put("token", result.result()));
          context.next();
        } else {
          log.debug("over failed:{}", result.cause().getMessage());
          context.put(DefaultErrorHandler.ERROR_DETAILS, result.cause().getMessage());
          context.fail(500);
        }
      })
    ;

    JsonObject authInfo = new JsonObject()
      .put("username", username)
      .put("password", password);
    startFuture.complete(authInfo);

  }

  @GET("/current")
  @Auth(authority = MongoAuth.ROLE_PREFIX + Constants.Role.USER, method = AuthMethod.API_TOKEN)
  public void getApi(@User AccessTokenUser user,
                     Payload<JsonObject> payload, RoutingContext context) {
    String userId = user.principal().getString(UserSchemaType._id);
    mongoService.findOne(ProfileSchemaType.COLLECTION_NAME,
      new JsonObject().put(ProfileSchemaType._id, userId),
      null,
      result -> {
        if (result.succeeded()) {
          if (Objects.isNull(result.result())) {
            log.error("userId [{}] 不存在", userId);
            return;
          }

          payload.set(result.result());
          context.next();
        }
      });
  }

  @GET("/logout")
  @Auth(authority = MongoAuth.ROLE_PREFIX + Constants.Role.USER, method = AuthMethod.API_TOKEN)
  public void logout(@Header("User-Agent") String userAgent,
                     RoutingContext context) {
    String clientIp = context.request().remoteAddress().host();
    io.vertx.ext.auth.User user = context.user();
    String username = user.principal().getString(UserSchemaType.username);
    accessTokenService.checkDelete(clientIp, userAgent, username, result -> {
      if (result.failed()) {
        context.fail(result.cause());
        return;
      }

      user.clearCache();
      context.clearUser();
      context.next();
    });
  }

}
