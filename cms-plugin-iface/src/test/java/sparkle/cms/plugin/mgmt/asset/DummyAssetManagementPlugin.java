package sparkle.cms.plugin.mgmt.asset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import sparkle.cms.plugin.mgmt.PluginImpl;
import sparkle.cms.plugin.mgmt.PluginOperationException;
import sparkle.cms.plugin.mgmt.PluginStatus;

/**
 * DummyAssetManagementPlugin
 * Created by bazzoni on 06/05/2015.
 */
@Component
public class DummyAssetManagementPlugin extends PluginImpl implements AssetManagementPlugin<DummyContainer, DummyAsset> {
    private final DummyContainer repository = new DummyContainer();

    @Value("classpath:/META-INF/plugin.properties")
    private Resource resource;

    /**
     * Get spring initialized resource
     *
     * @return resource
     */
    @Override
    public Resource getResource() {
        return resource;
    }
    public DummyContainer getRepository() {
        return repository;
    }

    /**
     * Creates a base repository container for site
     *
     * @param siteId site id
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public String createSiteRepository(String siteId) throws PluginOperationException {
        repository.put(siteId, new DummyContainer());

        return siteId;
    }

    /**
     * Deletes a site repository container
     *
     * @param siteId site id
     * @throws PluginOperationException if operation failure
     */
    @Override
    public void deleteSiteRepository(String siteId) throws PluginOperationException {
        repository.remove(siteId);
    }

    /**
     * Creates a folder container for site
     *
     * @param siteId site id
     * @param path   internal path
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public String createFolder(String siteId, String path) throws PluginOperationException {
        DummyContainer siteRepo = (DummyContainer) repository.get(siteId);
        siteRepo.put(path, new DummyContainer());

        return path;
    }

    /**
     * Deletes a folder
     *
     * @param siteId site id
     * @param nodeId node id
     * @throws PluginOperationException if operation failure
     */
    @Override
    public void deleteFolder(String siteId, String nodeId) throws PluginOperationException {
        DummyContainer siteRepo = (DummyContainer) repository.get(siteId);
        siteRepo.remove(nodeId);
    }

    /**
     * Creates an asset for the site and folder
     *
     * @param siteId      site id
     * @param path        internal path
     * @param name        asset  name
     * @param data        asset data
     * @param contentType content type
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public String createAsset(String siteId, String path, String name, byte[] data, String contentType) throws PluginOperationException {
        DummyContainer siteRepo = (DummyContainer) repository.get(siteId);
        DummyContainer folderRepo;
        if (path.isEmpty()) {
            folderRepo = siteRepo;
        } else {
            folderRepo = (DummyContainer) siteRepo.get(path);
        }
        folderRepo.put(name, new DummyAsset(name, data));

        return name;
    }

    /**
     * Deletes an asset
     *
     * @param siteId site id
     * @param path   internal path
     * @param name   asset name
     * @throws PluginOperationException if operation failure
     */
    @Override
    public void deleteAsset(String siteId, String path, String name) throws PluginOperationException {
        DummyContainer siteRepo = (DummyContainer) repository.get(siteId);
        DummyContainer folderRepo;
        if (path.isEmpty()) {
            folderRepo = siteRepo;
        } else {
            folderRepo = (DummyContainer) siteRepo.get(path);
        }
        folderRepo.remove(name);
    }

    /**
     * Find a site Repository
     *
     * @param siteId siteId
     * @return site repository
     */
    @Override
    public DummyContainer findSiteRepository(String siteId) {
        return (DummyContainer) repository.get(siteId);
    }

    /**
     * Find a site Repository
     *
     * @param siteId siteId
     * @param path   path
     * @return site repository
     */
    @Override
    public DummyContainer findFolder(String siteId, String path) {
        DummyContainer siteRepo = (DummyContainer) repository.get(siteId);
        return (DummyContainer) siteRepo.get(path);
    }

    /**
     * Find a site Repository
     *
     * @param siteId siteId
     * @param path   internal path
     * @param name   asset name
     * @return site repository
     */
    @Override
    public DummyAsset findAsset(String siteId, String path, String name) {
        DummyContainer siteRepo = (DummyContainer) repository.get(siteId);
        DummyContainer folderRepo;
        if (path.isEmpty()) {
            folderRepo = siteRepo;

        } else {
            folderRepo = (DummyContainer) siteRepo.get(path);
        }
        return (DummyAsset) folderRepo.get(name);
    }

    @Override
    protected void createSettings() {

    }

    /**
     * Validates plugin
     */
    @Override
    protected void doValidate() {
        repository.clear();
        status = PluginStatus.ACTIVE;
    }
}
