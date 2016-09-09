package org.uoiu.qieziyin.common;

public abstract class Constants {

  public static final String EMPTY_STRING = "";

  public static final String CONFIG_NAME = "app-config";

  public static final String ENV_APP_HOST = "APP_HOST";
  public static final String ENV_APP_PORT = "APP_PORT";

  public static final String ENV_MONGODB_CONNECTION = "MONGODB_CONNECTION";
  public static final String ENV_MONGODB_DB_NAME = "MONGODB_DB_NAME";

  public static final String MONGO_SERVICE_NAME = "mongoService";
  public static final String AUTH_PROVIDER_SERVICE_NAME = "authProvider";

  public class Role {
    public static final String USER = "USER";
    public static final String ADMIN = "ADMIN";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";
  }

}
