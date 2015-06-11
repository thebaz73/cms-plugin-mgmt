package sparkle.cms.plugin.mgmt.asset;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.jackrabbit.core.RepositoryFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import sparkle.cms.domain.CmsSetting;
import sparkle.cms.domain.SettingType;
import sparkle.cms.plugin.mgmt.PluginOperationException;
import sparkle.cms.plugin.mgmt.PluginStatus;

import javax.jcr.*;
import javax.jcr.security.Privilege;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * JackrabbitAssetManagementPlugin
 * Created by bazzoni on 10/06/2015.
 */
@Component
public class JackrabbitAssetManagementPlugin extends AbstractAssetManagementPlugin<JackrabbitContainer, JackrabbitAsset> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Repository repository = null;
    private Session session = null;

    @Value("classpath:/META-INF/jackrabbit-plugin.properties")
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
        if (username.isEmpty() || username.equals("<change me>"))
            throw new PluginOperationException("Cannot define username");
        String password = getSetting("password", String.class, properties.getProperty("plugin.password"));
        if (password.isEmpty() || password.equals("<change me>"))
            throw new PluginOperationException("Cannot define password");

        if (!repositoryURL.equals("<change me>")) {
            if (!repositoryURL.endsWith("/")) {
                repositoryURL = String.format("%s/", repositoryURL);
            }

            Map<String, String> parameters = new HashMap<>();
            parameters.put(JcrUtils.REPOSITORY_URI, repositoryURL);
            RepositoryFactory repositoryFactory = new RepositoryFactoryImpl();
            try {
                repository = repositoryFactory.getRepository(parameters);
                session = repository.login(new SimpleCredentials(username, password.toCharArray()));
                Principal everyonePrincipal = AccessControlUtils.getEveryonePrincipal(session);
                AccessControlUtils.allow(session.getRootNode(), everyonePrincipal.getName(), Privilege.JCR_ALL);
                String user = session.getUserID();
                String name = repository.getDescriptor(Repository.REP_NAME_DESC);
                logger.debug("Logged in as " + user + " to a " + name + " repository.");
                status = PluginStatus.ACTIVE;
            } catch (RepositoryException e) {
                throw new PluginOperationException("Cannot create JCR repository", e);
            }
        }
    }

    /**
     * Load all repository assets into central CMS database
     *
     * @param siteId         site id
     * @param siteRepository container repository
     * @throws PluginOperationException if error
     */
    @Override
    protected void loadChildren(String siteId, JackrabbitContainer siteRepository) throws PluginOperationException {

    }

    /**
     * Executes specific finalization tasks
     *
     * @throws PluginOperationException
     */
    @Override
    protected void finalizeObjects() throws PluginOperationException {
        if (session != null) {
            session.logout();
            repository = null;
        }
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
            final Node siteNode = JcrUtils.getOrAddFolder(session.getRootNode(), siteId);
            session.save();
            return siteNode.getName();
        } catch (RepositoryException e) {
            throw new PluginOperationException("Cannot create node", e);
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
            final Node node = session.getRootNode().getNode(siteId);
            node.remove();
            session.save();
        } catch (RepositoryException e) {
            throw new PluginOperationException("Cannot create node", e);
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
            Node rootNode = session.getRootNode();
            final Node siteNode = JcrUtils.getOrAddFolder(rootNode, siteId);
            final Node folder = JcrUtils.getOrAddFolder(siteNode, path);
            session.save();
            return folder.getName();
        } catch (RepositoryException e) {
            throw new PluginOperationException("Cannot create node", e);
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
            final Node node = session.getRootNode().getNode(String.format("%s/%s", siteId, path));
            node.remove();
            session.save();
        } catch (RepositoryException e) {
            throw new PluginOperationException("Cannot create node", e);
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
            Node rootNode = session.getRootNode();
            final Node siteNode = JcrUtils.getOrAddFolder(rootNode, siteId);
            final Node folder = JcrUtils.getOrAddFolder(siteNode, path);
            final Node asset = JcrUtils.putFile(folder, name, contentType, new ByteArrayInputStream(data));
            session.save();
            return asset.getName();
        } catch (RepositoryException e) {
            throw new PluginOperationException("Cannot create node", e);
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
            String nodePath;
            if (path.endsWith("/") || path.isEmpty()) {
                nodePath = String.format("%s/%s%s", siteId, path, name);
            } else {
                nodePath = String.format("%s/%s/%s", siteId, path, name);
            }
            final Node node = session.getRootNode().getNode(nodePath);
            node.remove();
            session.save();
        } catch (RepositoryException e) {
            throw new PluginOperationException("Cannot create node", e);
        }
    }

    /**
     * Find a site Repository
     *
     * @param siteId siteId
     * @return site container
     */
    @Override
    public JackrabbitContainer findSiteRepository(String siteId) throws PluginOperationException {
        try {
            final Node node = session.getRootNode().getNode(siteId);
            return new JackrabbitContainer(node.hasNodes(), node.getName());
        } catch (RepositoryException e) {
            throw new PluginOperationException("Cannot find node", e);
        }
    }

    /**
     * Find a folder
     *
     * @param siteId siteId
     * @param path   internal path
     * @return folder container
     */
    @Override
    public JackrabbitContainer findFolder(String siteId, String path) throws PluginOperationException {
        try {
            final Node node = session.getRootNode().getNode(String.format("%s/%s", siteId, path));
            return new JackrabbitContainer(node.hasNodes(), node.getName());
        } catch (RepositoryException e) {
            throw new PluginOperationException("Cannot find node", e);
        }
    }

    /**
     * Find asset
     *
     * @param siteId siteId
     * @param path   internal path
     * @param name   asset name
     * @return asset
     */
    @Override
    public JackrabbitAsset findAsset(String siteId, String path, String name) throws PluginOperationException {
        try {
            String nodePath;
            if (path.endsWith("/") || path.isEmpty()) {
                nodePath = String.format("%s/%s%s", siteId, path, name);
            } else {
                nodePath = String.format("%s/%s/%s", siteId, path, name);
            }
            Node node = session.getRootNode().getNode(nodePath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JcrUtils.readFile(node, baos);
            return new JackrabbitAsset(node.getPath(), baos);
        } catch (RepositoryException | IOException e) {
            throw new PluginOperationException("Cannot read asset node", e);
        }
    }
}
