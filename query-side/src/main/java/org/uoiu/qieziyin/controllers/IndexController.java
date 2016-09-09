package org.uoiu.qieziyin.controllers;

import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.mixins.ContentType;
import com.github.aesteve.vertx.nubes.annotations.routing.http.GET;
import com.github.aesteve.vertx.nubes.marshallers.Payload;
import io.vertx.core.json.JsonObject;

@Controller("/")
@ContentType("application/json")
public class IndexController {

  @GET
  public void indexJson(Payload<JsonObject> payload) {
    payload.set(new JsonObject().put("status", "up"));
  }

}
