package org.uoiu.qieziyin.schemas;

import io.vertx.ext.auth.mongo.MongoAuth;

public abstract class AccessTokenSchemaType extends SchemaType {

  public static final String COLLECTION_NAME = MongoAuth.DEFAULT_COLLECTION_NAME + "AccessToken";

  public static final String clientIp = "clientIp";
  public static final String userAgent = "userAgent";
  public static final String username = "username";
  public static final String accessedAt = "accessedAt";
  public static final String token = "token";

}
