package org.uoiu.qieziyin.controllers;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.auth.mongo.impl.MongoUser;

public class AccessTokenUser extends MongoUser {

  public AccessTokenUser(JsonObject result, MongoAuth authProvider) {
    super(result, authProvider);
  }

}
