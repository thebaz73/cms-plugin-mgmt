package sparkle.cms.plugin.mgmt.search;

import sparkle.cms.domain.CmsContent;

/**
 * MongoSparkleDocument
 * Created by bazzoni on 01/06/2015.
 */
public class MongoSparkleDocument implements SparkleDocument {
    public static final String FIELD_NAME = "title";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_SITEID = "siteId";
    private final String id;
    private final String name;
    private final String content;

    public MongoSparkleDocument(CmsContent cmsContent) {
        this(cmsContent.getId(), cmsContent.getTitle(), cmsContent.getContent());
    }

    public MongoSparkleDocument(String id, String title, String content) {
        this.id = id;
        this.name = title;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }
}