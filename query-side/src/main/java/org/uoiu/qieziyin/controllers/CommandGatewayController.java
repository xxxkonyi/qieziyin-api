package org.uoiu.qieziyin.controllers;

import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.auth.Auth;
import com.github.aesteve.vertx.nubes.annotations.auth.User;
import com.github.aesteve.vertx.nubes.annotations.mixins.ContentType;
import com.github.aesteve.vertx.nubes.annotations.params.RequestBody;
import com.github.aesteve.vertx.nubes.annotations.routing.http.POST;
import com.github.aesteve.vertx.nubes.annotations.services.Service;
import com.github.aesteve.vertx.nubes.auth.AuthMethod;
import com.github.aesteve.vertx.nubes.handlers.impl.DefaultErrorHandler;
import com.github.aesteve.vertx.nubes.marshallers.Payload;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.impl.JWTUser;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.uoiu.qieziyin.common.Constants;
import org.uoiu.qieziyin.services.CommandGatewayService;

import java.util.Map;
import java.util.Objects;

@Controller("/commandGateway/default")
@ContentType("application/json")
public class CommandGatewayController {

  @Service(CommandGatewayService.SERVICE_NAME)
  private CommandGatewayService commandGateway;

  private Vertx vertx;

  @POST
  @Auth(authority = Constants.Role.USER, method = AuthMethod.JWT)
  public void sendAndWait(@User JWTUser user,
                          @RequestBody JsonObject requestPayload,
                          Payload<JsonObject> payload, RoutingContext context) {
    Long timeout = requestPayload.getLong("timeout", DeliveryOptions.DEFAULT_TIMEOUT);
    Map headers = requestPayload.getJsonObject("headers", Constants.EMPTY_JSON_OBJECT).getMap();
    String address = requestPayload.getString("address");
    if (StringUtils.isEmpty(address)) {
      DefaultErrorHandler.badRequest(context, "address 不能为空");
      return;
    }

    Object body = requestPayload.getJsonObject("body");

    DeliveryOptions options = new DeliveryOptions()
      .setSendTimeout(timeout)
      .setHeaders(new CaseInsensitiveHeaders().addAll(headers));

    commandGateway.sendAndWait(address, body, options, result -> {
      if (result.succeeded()) {
        if (Objects.isNull(result.result())) {
          context.next();
          return;
        }

        Message replyMessage = result.result();
        JsonObject replyHeaders = new JsonObject();
        replyMessage.headers().forEach(t -> {
          replyHeaders.put(t.getKey(), t.getValue());
        });

        payload.set(new JsonObject()
          .put("address", replyMessage.address())
          .put("replyAddress", replyMessage.replyAddress())
          .put("headers", replyHeaders)
          .put("body", replyMessage.body())
        );
        context.next();
      } else {
        context.fail(result.cause());
      }
    });
  }

}
