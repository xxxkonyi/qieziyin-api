package schemas;

import io.vertx.ext.auth.mongo.MongoAuth;

public abstract class ProfileSchemaType extends SchemaType {

  public static final String COLLECTION_NAME = MongoAuth.DEFAULT_COLLECTION_NAME + "Profile";

  public static final String name = "name";
  public static final String bio = "bio";

}
