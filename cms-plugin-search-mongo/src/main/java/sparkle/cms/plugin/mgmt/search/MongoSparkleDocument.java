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
    private final String uri;
    private final Long date;
    private final String summary;
    private final String content;

    public MongoSparkleDocument(CmsContent cmsContent) {
        this(cmsContent.getId(), cmsContent.getTitle(), cmsContent.getUri(), cmsContent.getModificationDate().getTime(), cmsContent.getSummary(), cmsContent.getContent());
    }

    public MongoSparkleDocument(String id, String title, String uri, Long date, String summary, String content) {
        this.id = id;
        this.name = title;
        this.uri = uri;
        this.date = date;
        this.summary = summary;
        this.content = content;
    }

    /**
     * Get Document id
     *
     * @return the id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Get Document name
     *
     * @return name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Get Document uri
     *
     * @return uri
     */
    @Override
    public String getUri() {
        return uri;
    }

    /**
     * Get Document date in millis
     *
     * @return date
     */
    @Override
    public Long getDate() {
        return date;
    }

    /**
     * Get Document summary
     *
     * @return summary
     */
    @Override
    public String getSummary() {
        return summary;
    }

    /**
     * Get Document content
     *
     * @return content
     */
    @Override
    public String getContent() {
        return content;
    }
}