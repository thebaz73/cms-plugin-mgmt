package sparkle.cms.plugin.mgmt.asset;

import sparkle.cms.plugin.mgmt.Plugin;
import sparkle.cms.plugin.mgmt.PluginOperationException;

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
     * @param path   internal path
     * @throws PluginOperationException if operation failure
     */
    void deleteFolder(String siteId, String path) throws PluginOperationException;

    /**
     * Creates an asset for the site and folder
     *
     * @param siteId      site id
     * @param path        internal path
     * @param name        asset name
     * @param data        asset data
     * @param contentType content type
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    String createAsset(String siteId, String path, String name, byte[] data, String contentType) throws PluginOperationException;

    /**
     * Deletes an asset
     *
     * @param siteId site id
     * @param path   internal path
     * @param name   asset name
     * @throws PluginOperationException if operation failure
     */
    void deleteAsset(String siteId, String path, String name) throws PluginOperationException;

    /**
     * Find a site Repository
     *
     * @param siteId siteId
     * @return site container
     */
    C findSiteRepository(String siteId) throws PluginOperationException;

    /**
     * Find a folder
     *
     * @param siteId siteId
     * @param path   internal path
     * @return folder container
     */
    C findFolder(String siteId, String path) throws PluginOperationException;

    /**
     * Find asset
     *
     * @param siteId siteId
     * @param path   internal path
     * @param name   asset name
     * @return asset
     */
    A findAsset(String siteId, String path, String name) throws PluginOperationException;
}
