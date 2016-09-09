package org.uoiu.qieziyin.controllers;

import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.mixins.ContentType;
import com.github.aesteve.vertx.nubes.annotations.params.RequestBody;
import com.github.aesteve.vertx.nubes.annotations.routing.http.GET;
import com.github.aesteve.vertx.nubes.annotations.routing.http.POST;
import com.github.aesteve.vertx.nubes.annotations.services.Service;
import com.github.aesteve.vertx.nubes.context.PaginationContext;
import com.github.aesteve.vertx.nubes.marshallers.Payload;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

@Controller("/collections")
@ContentType("application/json")
public class CollectionController {

  @Service("mongo")
  private MongoClient mongo;
  @Service("eventBus")
  private EventBus eventBus;

  @GET("featured")
  public void collectionsOfFeatured(PaginationContext pageContext, Payload<JsonObject> payload) {

  }

  @POST
  public void createCollection(RoutingContext context, @RequestBody JsonObject payload) {
    JsonObject collection = new JsonObject();


    eventBus.publish("collection.created", collection);
  }

}
