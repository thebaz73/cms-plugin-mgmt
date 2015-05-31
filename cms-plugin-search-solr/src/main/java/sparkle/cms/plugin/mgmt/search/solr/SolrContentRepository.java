package sparkle.cms.plugin.mgmt.search.solr;

import org.springframework.data.domain.Sort;
import org.springframework.data.solr.repository.SolrCrudRepository;
import sparkle.cms.plugin.mgmt.search.SolrSparkleDocument;

import java.util.List;

/**
 * SolrContentRepository
 * Created by bazzoni on 26/05/2015.
 */
public interface SolrContentRepository extends SolrCrudRepository<SolrSparkleDocument, String> {
    /**
     * Find content matching title, summary or content
     *
     * @param name    content name
     * @param content content summmry
     * @param sort    sort
     * @return list of @SolrSparkleDocument
     */
    List<SolrSparkleDocument> findByNameContainingOrContentContaining(String name, String content, Sort sort);
}
