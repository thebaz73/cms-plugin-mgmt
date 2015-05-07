package ms.cms.plugin.mgmt.asset;

import ms.cms.plugin.mgmt.Plugin;
import ms.cms.plugin.mgmt.PluginOperationException;

/**
 * AssetManagementPlugin
 * Created by bazzoni on 06/05/2015.
 */
public interface AssetManagementPlugin<C extends Container, A extends Asset> extends Plugin {
    /**
     * Creates a base repository container for site
     *
     * @param siteId site id
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    String createSiteRepository(String siteId) throws PluginOperationException;

    /**
     * Deletes a site repository container
     *
     * @param siteId site id
     * @throws PluginOperationException if operation failure
     */
    void deleteSiteRepository(String siteId) throws PluginOperationException;

    /**
     * Creates a folder container for site
     *
     * @param siteId site id
     * @param path   internal path
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    String createFolder(String siteId, String path) throws PluginOperationException;

    /**
     * Deletes a folder
     *
     * @param siteId site id
     * @param nodeId node id
     * @throws PluginOperationException if operation failure
     */
    void deleteFolder(String siteId, String nodeId) throws PluginOperationException;

    /**
     * Creates an asset for the site and folder
     *
     * @param siteId site id
     * @param path   internal path
     * @param name   asset name
     * @param data   asset data
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    String createAsset(String siteId, String path, String name, byte[] data) throws PluginOperationException;

    /**
     * Deletes an asset
     *
     * @param siteId site id
     * @param nodeId node id
     * @throws PluginOperationException if operation failure
     */
    void deleteAsset(String siteId, String nodeId) throws PluginOperationException;

    /**
     * Find a site Repository
     *
     * @param siteId siteId
     * @return site repository
     */
    C findSiteRepository(String siteId);

    /**
     * Find a site Repository
     *
     * @param siteId siteId
     * @return site repository
     */
    C findFolder(String siteId, String path);

    /**
     * Find a site Repository
     *
     * @param siteId siteId
     * @return site repository
     */
    A findAsset(String siteId, String nodeId);
}
