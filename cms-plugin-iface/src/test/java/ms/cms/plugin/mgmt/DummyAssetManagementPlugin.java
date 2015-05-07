package ms.cms.plugin.mgmt;

import com.mongodb.util.Hash;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * DummyAssetManagementPlugin
 * Created by bazzoni on 06/05/2015.
 */
@SuppressWarnings("unchecked")
@Component
public class DummyAssetManagementPlugin extends PluginImpl implements AssetManagementPlugin<HashMap<String, Object>, Object> {
    private final Map<String, Object> repository = new HashMap<>();

    public Map<String, Object> getRepository() {
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
     * Find a site Repository
     *
     * @param siteId siteId
     * @return site repository
     */
    @Override
    public HashMap<String, Object> findSiteRepository(String siteId) {
        return (HashMap<String, Object>) repository.get(siteId);
    }

    /**
     * Find a site Repository
     *
     * @param siteId siteId
     * @param path path
     * @return site repository
     */
    @Override
    public HashMap<String, Object> findFolder(String siteId, String path) {
        HashMap<String, Object> siteRepo = (HashMap<String, Object>) repository.get(siteId);
        return (HashMap<String, Object>) siteRepo.get(path);
    }

    /**
     * Find a site Repository
     *
     * @param siteId siteId
     * @param nodeId node id
     * @return site repository
     */
    @Override
    public Object findAsset(String siteId, String nodeId) {
        HashMap<String, Object> siteRepo = (HashMap<String, Object>) repository.get(siteId);
        HashMap<String, Object> folderRepo;
        if(nodeId.contains("/")) {
            String[] tokens = nodeId.split("/");
            folderRepo = (HashMap<String, Object>) siteRepo.get(tokens[0]);
            return folderRepo.get(tokens[1]);
        }
        else {
            folderRepo = siteRepo;
            return folderRepo.get(nodeId);
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
