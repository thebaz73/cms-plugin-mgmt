package sparkle.cms.plugin.mgmt.search;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;
import sparkle.cms.domain.CmsSetting;
import sparkle.cms.domain.SettingType;
import sparkle.cms.plugin.mgmt.PluginOperationException;
import sparkle.cms.plugin.mgmt.PluginStatus;
import sparkle.cms.plugin.mgmt.search.solr.SolrContentRepositoryFactory;

import java.util.List;

/**
 * SolrSearchPlugin
 * Created by bazzoni on 30/05/2015.
 */
@Component
public class SolrSearchPlugin extends AbstractSearchPlugin<SolrSparkleDocument> {

    @Value("classpath:/META-INF/solr-plugin.properties")
    private Resource resource;
    private SolrTemplate solrTemplate;

    /**
     * Get spring initialized resource
     *
     * @return resource
     */
    @Override
    public Resource getResource() {
        return resource;
    }


    /**
     * Initialize plugin settings
     *
     * @throws PluginOperationException if error
     */
    @Override
    protected void createSettings() throws PluginOperationException {
        settings.add(new CmsSetting(getCompoundKey("activate"), getSetting("activate", Boolean.class, false), SettingType.BOOL));
        settings.add(new CmsSetting(getCompoundKey("server.url"), getSetting("server.url", String.class, properties.getProperty("plugin.server.url")), SettingType.TEXT));
    }

    /**
     * Validates plugin
     *
     * @throws PluginOperationException if error
     */
    @Override
    protected void doValidate() throws PluginOperationException {
        String solrServerUrl = getSetting("server.url", String.class, properties.getProperty("plugin.server.url"));
        if (solrServerUrl.isEmpty()) {
            throw new PluginOperationException("Cannot define repository URL");
        }

        if (!solrServerUrl.equals("<change me>")) {
            if (!solrServerUrl.endsWith("/")) {
                solrServerUrl = String.format("%s/", solrServerUrl);
                SolrContentRepositoryFactory repositoryFactory = new SolrContentRepositoryFactory(solrServerUrl);
                solrTemplate = repositoryFactory.createSolrTemplate();
            }
            status = PluginStatus.ACTIVE;
        }
    }

    /**
     * Add a content to index
     *
     * @param id      document id
     * @param name    document name
     * @param content document content
     */
    @Override
    public void addToIndex(String id, String name, String content) {
        final SolrSparkleDocument document = SolrSparkleDocument.getBuilder(id, name)
                .content(content)
                .build();
        solrTemplate.saveBean(document);
        solrTemplate.commit();
    }

    /**
     * Delete an indexed document from Solr index
     *
     * @param id document id
     */
    @Override
    public void deleteFromIndex(String id) {
        solrTemplate.deleteById(id);
        solrTemplate.commit();
    }

    /**
     * Search index for specified term
     *
     * @param searchTerm search term
     * @return found documents
     */
    @Override
    public List<SolrSparkleDocument> search(String searchTerm) {
        String[] words = searchTerm.split(" ");

        Criteria conditions = createSearchConditions(words);
        SimpleQuery search = new SimpleQuery(conditions);
        search.addSort(sortByIdDesc());

        return solrTemplate.queryForPage(search, SolrSparkleDocument.class).getContent();
    }

    /**
     * Update a content in index
     *
     * @param id      document id
     * @param name    document name
     * @param content document content
     */
    @Override
    public void update(String id, String name, String content) {
        final SolrSparkleDocument document = SolrSparkleDocument.getBuilder(id, name)
                .content(content)
                .build();
        solrTemplate.saveBean(document);
        solrTemplate.commit();
    }


    private Criteria createSearchConditions(String[] words) {
        Criteria conditions = null;

        for (String word : words) {
            if (conditions == null) {
                conditions = new Criteria(SolrSparkleDocument.FIELD_NAME).contains(word)
                        .or(new Criteria(SolrSparkleDocument.FIELD_CONTENT).contains(word));
            } else {
                conditions = conditions.or(new Criteria(SolrSparkleDocument.FIELD_NAME).contains(word))
                        .or(new Criteria(SolrSparkleDocument.FIELD_CONTENT).contains(word));
            }
        }

        return conditions;
    }

    private Sort sortByIdDesc() {
        return new Sort(Sort.Direction.DESC, SolrSparkleDocument.FIELD_ID);
    }
}
