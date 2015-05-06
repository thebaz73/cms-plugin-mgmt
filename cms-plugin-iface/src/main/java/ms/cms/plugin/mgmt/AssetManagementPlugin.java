package ms.cms.plugin.mgmt;

/**
 * AssetManagementPlugin
 * Created by bazzoni on 06/05/2015.
 */
public interface AssetManagementPlugin extends Plugin {
    /**
     * Creates a base repository container for site
     *
     * @param siteId site id
     * @return asset path
     * @throws PluginOperationException if operation failure
     *
     */
    public String createSiteRepository(String siteId) throws PluginOperationException;
    /**
     * Deletes a site repository container
     *
     * @param siteId site id
     * @throws PluginOperationException if operation failure
     *
     */
    public void deleteSiteRepository(String siteId) throws PluginOperationException;
    /**
     * Creates a folder container for site
     *
     * @param siteId site id
     * @param path internal path
     * @return asset path
     * @throws PluginOperationException if operation failure
     *
     */
    public String createFolder(String siteId, String path) throws PluginOperationException;
    /**
     * Deletes a folder
     *
     * @param siteId site id
     * @param nodeId node id
     * @throws PluginOperationException if operation failure
     *
     */
    public void deleteFolder(String siteId, String nodeId) throws PluginOperationException;
    /**
     * Creates an asset for the site and folder
     *
     * @param siteId site id
     * @param path internal path
     * @param filename asset file name
     * @return asset path
     * @throws PluginOperationException if operation failure
     *
     */
    public String createAsset(String siteId, String path, String filename) throws PluginOperationException;
    /**
     * Deletes an asset
     *
     * @param siteId site id
     * @param nodeId node id
     * @throws PluginOperationException if operation failure
     *
     */
    public void deleteAsset(String siteId, String nodeId) throws PluginOperationException;
}
