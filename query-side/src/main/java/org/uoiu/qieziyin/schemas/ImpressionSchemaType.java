package org.uoiu.qieziyin.schemas;

public abstract class ImpressionSchemaType extends SchemaType {

  public static final String COLLECTION_NAME = "impression";

  public static final String title = "title";
  public static final String description = "description";
  /**
   * 内容，图片URL或者文字
   */
  public static final String content = "content";
  /**
   * 发生时间
   */
  public static final String eventDate = "eventDate";
  public static final String creatorId = "creatorId";
  public static final String collectionId = "collectionId";

}
