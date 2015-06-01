package sparkle.cms.plugin.mgmt.search;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

/**
 * SolrSparkleDocument
 * Created by bazzoni on 28/05/2015.
 */
@SolrDocument(solrCoreName = "sparkleDocs")
public class SolrSparkleDocument implements SparkleDocument {
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_SITEID = "siteId";

    @Id
    @Field
    private String id;

    @Field
    private String siteId;

    @Field
    private String name;
    private String uri;
    private long date;
    private String summary;

    @Field
    private String content;

    public SolrSparkleDocument() {
    }

    public static Builder getBuilder(String id, String name) {
        return new Builder(id, name);
    }

    @Override
    public String getId() {
        return id;
    }

    public String getSiteId() {
        return siteId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public Long getDate() {
        return date;
    }

    @Override
    public String getSummary() {
        return summary;
    }

    @Override
    public String getContent() {
        return content;
    }

    public static class Builder {
        private SolrSparkleDocument build;

        public Builder(String id, String name) {
            build = new SolrSparkleDocument();
            build.id = id;
            build.name = name;
        }

        public Builder siteId(String siteId) {
            build.siteId = siteId;
            return this;
        }

        public Builder uri(String uri) {
            build.uri = uri;
            return this;
        }

        public Builder date(long date) {
            build.date = date;
            return this;
        }

        public Builder summary(String summary) {
            build.summary = summary;
            return this;
        }

        public Builder content(String content) {
            build.content = content;
            return this;
        }

        public SolrSparkleDocument build() {
            return build;
        }
    }
}
