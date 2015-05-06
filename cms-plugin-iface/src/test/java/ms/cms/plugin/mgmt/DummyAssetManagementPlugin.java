package ms.cms.plugin.mgmt;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * DummyAssetManagementPlugin
 * Created by bazzoni on 06/05/2015.
 */
@SuppressWarnings("unchecked")
public class DummyAssetManagementPlugin extends PluginImpl implements AssetManagementPlugin {
    private final Map<String, Object> repository = new HashMap<>();
    /**
     * Creates a base repository container for site
     *
     * @param siteId site id
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public String createSiteRepository(String siteId) throws PluginOperationException {
        repository.put(siteId, new HashMap<>());

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
        HashMap<String, Object> siteRepo = (HashMap<String, Object>) repository.get(siteId);
        siteRepo.put(path, new HashMap<String, HashMap<String, Object>>());

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
        HashMap<String, Object> siteRepo = (HashMap<String, Object>) repository.get(siteId);
        siteRepo.remove(nodeId);
    }

    /**
     * Creates an asset for the site and folder
     *
     * @param siteId   site id
     * @param path     internal path
     * @param filename asset file name
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public String createAsset(String siteId, String path, String filename) throws PluginOperationException {
        HashMap<String, Object> siteRepo = (HashMap<String, Object>) repository.get(siteId);
        HashMap<String, Object> folderRepo;
        if(path.isEmpty()) {
            folderRepo = siteRepo;
        }
        else {
            folderRepo = (HashMap<String, Object>) siteRepo.get(path);
        }
        HashMap<String, Object> asset = new HashMap<>();
        asset.put(filename, new File(filename));
        folderRepo.put(filename, asset);

        return filename;
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
        HashMap<String, Object> siteRepo = (HashMap<String, Object>) repository.get(siteId);
        HashMap<String, Object> folderRepo;
        if(nodeId.contains("/")) {
            String[] tokens = nodeId.split("/");
            folderRepo = (HashMap<String, Object>) siteRepo.get(tokens[0]);
            folderRepo.remove(tokens[1]);
        }
        else {
            folderRepo = siteRepo;
            folderRepo.remove(nodeId);
        }
    }


    /**
     * Validates plugin
     */
    @Override
    public void doValidate() {
        repository.clear();
    }
}
