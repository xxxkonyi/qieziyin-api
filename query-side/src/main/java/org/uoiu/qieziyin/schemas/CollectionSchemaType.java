package org.uoiu.qieziyin.schemas;

import io.vertx.core.json.JsonObject;

import java.util.Objects;

public abstract class CollectionSchemaType extends SchemaType {

  public static final String COLLECTION_PUBLIC_TYPE_PUBLIC = "PUBLIC";
  public static final String COLLECTION_PUBLIC_TYPE_PRIVATE = "PRIVATE";

  public static final String COLLECTION_NAME = "collection";

  public static final String title = "title";
  public static final String description = "description";
  /**
   * 封面图片
   */
  public static final String cover = "cover";
  /**
   * 活动时间
   */
  public static final String eventDate = "eventDate";
  /**
   * 公开类型
   */
  public static final String publicType = "publicType";
  public static final String creatorId = "creatorId";

  public static boolean checkPubilcType(JsonObject collection, String currentUserId) {
    if (Objects.nonNull(collection) && Objects.nonNull(currentUserId)) {
      String creatorId = collection.getString(CollectionSchemaType.creatorId);
      if (!Objects.equals(creatorId, currentUserId)) {
        if (Objects.equals(collection.getString(CollectionSchemaType.publicType),
          CollectionSchemaType.COLLECTION_PUBLIC_TYPE_PRIVATE)) {
          return false;
        }
      }
    }
    return true;
  }

}
