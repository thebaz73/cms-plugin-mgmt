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

    @Id
    @Field
    private String id;

    @Field
    private String name;

    @Field
    private String content;

    public SolrSparkleDocument() {
    }

    public static Builder getBuilder(String id, String name) {
        return new Builder(id, name);
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

    @Override
    public String toString() {
        return "SolrSparkleDocument{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    public static class Builder {
        private SolrSparkleDocument build;

        public Builder(String id, String name) {
            build = new SolrSparkleDocument();
            build.id = id;
            build.name = name;
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
