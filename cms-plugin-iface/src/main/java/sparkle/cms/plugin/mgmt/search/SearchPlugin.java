package sparkle.cms.plugin.mgmt.search;

import sparkle.cms.plugin.mgmt.Plugin;

import java.util.List;

/**
 * SearchPlugin
 * Created by bazzoni on 30/05/2015.
 */
public interface SearchPlugin<T extends SparkleDocument> extends Plugin {
    /**
     * Update a content in index
     *
     * @param id        document id
     * @param siteId    document siteId
     * @param name      document name
     * @param uri       document uri
     * @param date      document date
     * @param summary   document summary
     * @param content   document content
     */
    void addToIndex(String id, String siteId, String name, String uri, Long date, String summary, String content);

    /**
     * Delete an indexed document from Solr index
     *
     * @param id document id
     */
    void deleteFromIndex(String id);

    /**
     * Search index for specified term
     *
     *
     * @param siteId site id
     * @param searchTerm search term
     * @return found documents
     */
    List<T> search(String siteId, String searchTerm);

    /**
     * Update a content in index
     *
     * @param id        document id
     * @param siteId    document siteId
     * @param name      document name
     * @param uri       document uri
     * @param date      document date
     * @param summary   document summary
     * @param content   document content
     */
    void update(String id, String siteId, String name, String uri, Long date, String summary, String content);
}
