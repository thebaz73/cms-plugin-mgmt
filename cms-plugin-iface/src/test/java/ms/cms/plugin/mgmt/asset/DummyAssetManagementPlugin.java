package ms.cms.plugin.mgmt.asset;

import ms.cms.plugin.mgmt.PluginImpl;
import ms.cms.plugin.mgmt.PluginOperationException;
import ms.cms.plugin.mgmt.PluginStatus;
import org.springframework.stereotype.Component;

/**
 * DummyAssetManagementPlugin
 * Created by bazzoni on 06/05/2015.
 */
@SuppressWarnings("unchecked")
@Component
public class DummyAssetManagementPlugin extends PluginImpl implements AssetManagementPlugin<DummyContainer, DummyAsset> {
    private final DummyContainer repository = new DummyContainer();

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
     * @param siteId site id
     * @param path   internal path
     * @param name   asset  name
     * @param data   asset data
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public String createAsset(String siteId, String path, String name, byte[] data) throws PluginOperationException {
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
     * @param nodeId node id
     * @throws PluginOperationException if operation failure
     */
    @Override
    public void deleteAsset(String siteId, String nodeId) throws PluginOperationException {
        DummyContainer siteRepo = (DummyContainer) repository.get(siteId);
        DummyContainer folderRepo;
        if (nodeId.contains("/")) {
            String[] tokens = nodeId.split("/");
            folderRepo = (DummyContainer) siteRepo.get(tokens[0]);
            folderRepo.remove(tokens[1]);
        } else {
            folderRepo = siteRepo;
            folderRepo.remove(nodeId);
        }
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
     * @param nodeId node id
     * @return site repository
     */
    @Override
    public DummyAsset findAsset(String siteId, String nodeId) {
        DummyContainer siteRepo = (DummyContainer) repository.get(siteId);
        DummyContainer folderRepo;
        if (nodeId.contains("/")) {
            String[] tokens = nodeId.split("/");
            folderRepo = (DummyContainer) siteRepo.get(tokens[0]);
            return (DummyAsset) folderRepo.get(tokens[1]);
        } else {
            folderRepo = siteRepo;
            return (DummyAsset) folderRepo.get(nodeId);
        }
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
