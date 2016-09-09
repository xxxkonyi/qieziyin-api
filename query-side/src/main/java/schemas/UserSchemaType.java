package schemas;

import io.vertx.ext.auth.mongo.MongoAuth;

public abstract class UserSchemaType extends SchemaType {

  public static final String COLLECTION_NAME = MongoAuth.DEFAULT_COLLECTION_NAME;

  public static final String username = MongoAuth.DEFAULT_USERNAME_FIELD;
  public static final String salt = MongoAuth.DEFAULT_SALT_FIELD;
  public static final String password = MongoAuth.DEFAULT_PASSWORD_FIELD;
  public static final String roles = MongoAuth.DEFAULT_ROLE_FIELD;
  public static final String permissions = MongoAuth.DEFAULT_PERMISSION_FIELD;

}
