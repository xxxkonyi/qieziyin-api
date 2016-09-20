package org.uoiu.qieziyin.schemas;

import com.google.common.collect.Lists;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.MongoAuth;
import org.uoiu.qieziyin.common.Constants;

import java.time.Instant;

public abstract class UserSchemaType extends SchemaType {

  public static final String COLLECTION_NAME = MongoAuth.DEFAULT_COLLECTION_NAME;

  public static final String username = MongoAuth.DEFAULT_USERNAME_FIELD;
  public static final String salt = MongoAuth.DEFAULT_SALT_FIELD;
  public static final String password = MongoAuth.DEFAULT_PASSWORD_FIELD;
  public static final String roles = MongoAuth.DEFAULT_ROLE_FIELD;
  public static final String permissions = MongoAuth.DEFAULT_PERMISSION_FIELD;

  public static final JsonObject SUPER_ADMIN = new JsonObject()
    .put(UserSchemaType._id, "sa")
    .put(UserSchemaType.username, "sa")
    .put(UserSchemaType.password, "sa")
    .put(ProfileSchemaType.name, "超级管理")
    .put(ProfileSchemaType.bio, "超级管理员")
    .put(UserSchemaType.roles, new JsonArray(Lists.newArrayList(Constants.Role.USER, Constants.Role.ADMIN, Constants.Role.SUPER_ADMIN)))
    .put(UserSchemaType.createdAt, Instant.parse("2016-05-01T00:00:00.00Z"));

}
