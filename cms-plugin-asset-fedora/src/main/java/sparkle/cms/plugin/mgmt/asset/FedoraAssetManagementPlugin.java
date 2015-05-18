package sparkle.cms.plugin.mgmt.asset;

import org.fcrepo.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import sparkle.cms.domain.CmsSetting;
import sparkle.cms.domain.SettingType;
import sparkle.cms.plugin.mgmt.PluginOperationException;
import sparkle.cms.plugin.mgmt.PluginStatus;

import java.io.ByteArrayInputStream;

/**
 * FedoraAssetManagementPlugin
 * Created by bazzoni on 08/05/2015.
 */
@Component
public class FedoraAssetManagementPlugin extends AbstractAssetManagementPlugin<FedoraContainer, FedoraAsset> {
    private FedoraRepository repository;

    @Value("classpath:/META-INF/fedora-plugin.properties")
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

    /**
     * Creates a base repository container for site
     *
     * @param siteId site id
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public String createSiteRepository(String siteId) throws PluginOperationException {
        try {
            FedoraObject fedoraObject = repository.createObject(siteId);
            return fedoraObject.getName();
        } catch (FedoraException e) {
            throw new PluginOperationException("Fedora Repository related error.", e);
        }
    }

    /**
     * Deletes a site repository container
     *
     * @param siteId site id
     * @throws PluginOperationException if operation failure
     */
    @Override
    public void deleteSiteRepository(String siteId) throws PluginOperationException {
        try {
            repository.findOrCreateObject(siteId).delete();
        } catch (FedoraException e) {
            throw new PluginOperationException("Fedora Repository related error.", e);
        }
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
        try {
            repository.findOrCreateObject(siteId);
            FedoraObject fedoraObject = repository.createObject(siteId + "/" + path);
            return fedoraObject.getName();
        } catch (FedoraException e) {
            throw new PluginOperationException("Fedora Repository related error.", e);
        }
    }

    /**
     * Deletes a folder
     *
     * @param siteId site id
     * @param path   internal path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public void deleteFolder(String siteId, String path) throws PluginOperationException {
        try {
            repository.findOrCreateObject(siteId + "/" + path).delete();
        } catch (FedoraException e) {
            throw new PluginOperationException("Fedora Repository related error.", e);
        }
    }

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
    @Override
    public String createAsset(String siteId, String path, String name, byte[] data, String contentType) throws PluginOperationException {
        try {
            FedoraContent content = new FedoraContent().setContent(new ByteArrayInputStream(data))
                    .setContentType(contentType);
            repository.findOrCreateObject(siteId);
            repository.findOrCreateObject(siteId + "/" + path);
            FedoraDatastream datastream = repository.createDatastream(siteId + "/" + path + "/" + name, content);
            return datastream.getName();
        } catch (FedoraException e) {
            throw new PluginOperationException("Fedora Repository related error.", e);
        }
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
        try {
            repository.findOrCreateDatastream(siteId + "/" + path + "/" + name).delete();
        } catch (FedoraException e) {
            throw new PluginOperationException("Fedora Repository related error.", e);
        }
    }

    /**
     * Find a site Repository
     *
     * @param siteId siteId
     * @return site container
     * @throws PluginOperationException if operation failure
     */
    @Override
    public FedoraContainer findSiteRepository(String siteId) throws PluginOperationException {
        try {
            return new FedoraContainer(repository.findOrCreateObject(siteId));
        } catch (FedoraException e) {
            throw new PluginOperationException("Fedora Repository related error.", e);
        }
    }

    /**
     * Find a folder
     *
     * @param siteId siteId
     * @param path   internal path
     * @return folder container
     * @throws PluginOperationException if operation failure
     */
    @Override
    public FedoraContainer findFolder(String siteId, String path) throws PluginOperationException {
        try {
            return new FedoraContainer(repository.findOrCreateObject(siteId + "/" + path));
        } catch (FedoraException e) {
            throw new PluginOperationException("Fedora Repository related error.", e);
        }
    }

    /**
     * Find asset
     *
     * @param siteId siteId
     * @param path   internal path
     * @param name   asset name
     * @return asset
     * @throws PluginOperationException if operation failure
     */
    @Override
    public FedoraAsset findAsset(String siteId, String path, String name) throws PluginOperationException {
        try {
            final FedoraDatastreamImpl fedoraDatastream = (FedoraDatastreamImpl) repository.findOrCreateDatastream(siteId + "/" + path + "/" + name);
            return new FedoraAsset(fedoraDatastream.getUri());
        } catch (FedoraException e) {
            throw new PluginOperationException("Fedora Repository related error.", e);
        }
    }

    /**
     * Initialize plugin settings
     *
     * @throws PluginOperationException if error
     */
    @Override
    protected void createSettings() throws PluginOperationException {
        settings.add(new CmsSetting(getCompoundKey("activate"), getSetting("activate", Boolean.class, false), SettingType.BOOL));
        settings.add(new CmsSetting(getCompoundKey("repositoryURL"), getSetting("repositoryURL", String.class, "<change me>"), SettingType.INET));
        settings.add(new CmsSetting(getCompoundKey("username"), getSetting("username", String.class, "<change me>"), SettingType.TEXT));
        settings.add(new CmsSetting(getCompoundKey("password"), getSetting("password", String.class, "<change me>"), SettingType.TEXT));
    }

    /**
     * Validates plugin
     *
     * @throws PluginOperationException if error
     */
    @Override
    protected void doValidate() throws PluginOperationException {
        String repositoryURL = getSetting("repositoryURL", String.class, properties.getProperty("plugin.repositoryURL"));
        if (repositoryURL.isEmpty()) {
            throw new PluginOperationException("Cannot define repository URL");
        }
        String username = getSetting("username", String.class, properties.getProperty("plugin.username"));
        if (username.isEmpty() || username.equals("<change me>")) username = null;
        String password = getSetting("password", String.class, properties.getProperty("plugin.password"));
        if (password.isEmpty() || password.equals("<change me>")) password = null;

        if (!repositoryURL.isEmpty() && !repositoryURL.equals("<change me>")) {
            this.repository = new FedoraRepositoryImpl(repositoryURL, username, password);
            status = PluginStatus.ACTIVE;
        }
    }
}
