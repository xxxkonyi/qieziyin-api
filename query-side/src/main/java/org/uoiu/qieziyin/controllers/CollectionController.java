package org.uoiu.qieziyin.controllers;

import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.auth.User;
import com.github.aesteve.vertx.nubes.annotations.mixins.ContentType;
import com.github.aesteve.vertx.nubes.annotations.params.RequestBody;
import com.github.aesteve.vertx.nubes.annotations.routing.http.GET;
import com.github.aesteve.vertx.nubes.annotations.routing.http.POST;
import com.github.aesteve.vertx.nubes.annotations.services.Service;
import com.github.aesteve.vertx.nubes.marshallers.Payload;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.impl.JWTUser;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;
import org.uoiu.qieziyin.common.Constants;

@Controller("/collections")
@ContentType("application/json")
public class CollectionController {

  @Service(Constants.MONGO_SERVICE_NAME)
  private MongoClient mongoService;

  @GET("featured")
  public void collectionsOfFeatured(@User JWTUser user,
                                    Payload<JsonObject> payload) {

  }

  @POST
  public void createCollection(@RequestBody JsonObject requestPayload, RoutingContext context) {

  }

}
