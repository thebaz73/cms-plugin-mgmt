package sparkle.cms.plugin.mgmt.search.solr;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.data.solr.core.SolrTemplate;

/**
 * SolrContentRepositoryFactory
 * Created by bazzoni on 31/05/2015.
 */
public class SolrContentRepositoryFactory {
    private final String solrServerUrl;

    public SolrContentRepositoryFactory(String solrServerUrl) {
        this.solrServerUrl = solrServerUrl;
    }

    public SolrTemplate createSolrTemplate() {
        return new SolrTemplate(new HttpSolrServer(solrServerUrl));
    }

}
