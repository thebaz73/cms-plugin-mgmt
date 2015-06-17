package sparkle.cms.plugin.mgmt.asset;

import com.mongodb.Mongo;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.document.DocumentMK;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import sparkle.cms.domain.CmsAsset;
import sparkle.cms.domain.CmsSetting;
import sparkle.cms.domain.SettingType;
import sparkle.cms.plugin.mgmt.PluginOperationException;
import sparkle.cms.plugin.mgmt.PluginStatus;

import javax.jcr.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import static sparkle.cms.plugin.mgmt.asset.AssetUtils.findAssetTypeByFileName;


/**
 * OakAssetManagementPlugin
 * Created by bazzoni on 14/06/2015.
 */
@Component
public class OakAssetManagementPlugin extends AbstractAssetManagementPlugin<OakContainer, OakAsset> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Repository repository = null;
    private Session session = null;

    @Value("classpath:/META-INF/oak-plugin.properties")
    private Resource resource;

    @Autowired
    private Mongo mongo;
    private DocumentNodeStore documentNodeStore;

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
        settings.add(new CmsSetting(getCompoundKey("dbName"), getSetting("dbName", String.class, "<change me>"), SettingType.TEXT));
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
        String dbName = getSetting("dbName", String.class, properties.getProperty("plugin.dbName"));
        if (dbName.isEmpty()) {
            throw new PluginOperationException("Cannot define database name");
        }
        String username = getSetting("username", String.class, properties.getProperty("plugin.username"));
        if (username.isEmpty() || username.equals("<change me>"))
            throw new PluginOperationException("Cannot define username");
        String password = getSetting("password", String.class, properties.getProperty("plugin.password"));
        if (password.isEmpty() || password.equals("<change me>"))
            throw new PluginOperationException("Cannot define password");

        if (!dbName.equals("<change me>")) {
            try {
                documentNodeStore = new DocumentMK.Builder().
                        setMongoDB(mongo.getDB(dbName)).getNodeStore();
                repository = new Jcr(new Oak(documentNodeStore)).createRepository();
                session = repository.login(new SimpleCredentials(username, password.toCharArray()));

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
    protected void loadChildren(String siteId, OakContainer siteRepository) throws PluginOperationException {
        try {
            final Node siteNode = JcrUtils.getOrAddFolder(session.getRootNode(), siteId);
            final Iterable<Node> childNodes = JcrUtils.getChildNodes(siteNode);
            for (Node node : childNodes) {
                CmsAsset cmsAsset = new CmsAsset(siteId, node.getName(), new Date(), node.getName(), String.format("%s/%s", siteId, node.getName()));
                cmsAsset.setType(findAssetTypeByFileName(node.getName()));

                cmsAssetRepository.save(cmsAsset);
            }
        } catch (RepositoryException e) {
            throw new PluginOperationException("Fedora Repository related error.", e);
        }
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
            documentNodeStore.dispose();
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
            if (session.getRootNode().hasNode(siteId)) {
                final Node node = session.getRootNode().getNode(siteId);
                node.remove();
                session.save();
            }
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
        final String nodePath = String.format("%s/%s", siteId, path);
        try {
            if (session.getRootNode().hasNode(nodePath)) {
                final Node node = session.getRootNode().getNode(nodePath);
                node.remove();
                session.save();
            }
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
            final Node folder = path.isEmpty() ? siteNode : JcrUtils.getOrAddFolder(siteNode, path);
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
        String nodePath;
        if (path.endsWith("/") || path.isEmpty()) {
            nodePath = String.format("%s/%s%s", siteId, path, name);
        } else {
            nodePath = String.format("%s/%s/%s", siteId, path, name);
        }
        try {
            if (session.getRootNode().hasNode(nodePath)) {
                final Node node = session.getRootNode().getNode(nodePath);
                node.remove();
                session.save();
            }
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
    public OakContainer findSiteRepository(String siteId) throws PluginOperationException {
        try {
            OakContainer oakContainer = null;
            if (session.getRootNode().hasNode(siteId)) {
                final Node node = session.getRootNode().getNode(siteId);
                oakContainer = new OakContainer(node.hasNodes(), node.getName());
            }
            return oakContainer;
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
    public OakContainer findFolder(String siteId, String path) throws PluginOperationException {
        try {
            OakContainer oakContainer = null;
            if (session.getRootNode().hasNode(siteId)) {
                final Node node = session.getRootNode().getNode(String.format("%s/%s", siteId, path));
                oakContainer = new OakContainer(node.hasNodes(), node.getName());
            }
            return oakContainer;
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
    public OakAsset findAsset(String siteId, String path, String name) throws PluginOperationException {
        String nodePath;
        if (path.endsWith("/") || path.isEmpty()) {
            nodePath = String.format("%s/%s%s", siteId, path, name);
        } else {
            nodePath = String.format("%s/%s/%s", siteId, path, name);
        }
        try {
            OakAsset oakAsset = null;
            if (session.getRootNode().hasNode(nodePath)) {
                Node node = session.getRootNode().getNode(nodePath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                JcrUtils.readFile(node, baos);
                oakAsset = new OakAsset(node.getPath(), baos);
            }
            return oakAsset;
        } catch (RepositoryException | IOException e) {
            throw new PluginOperationException("Cannot read asset node", e);
        }
    }
}
