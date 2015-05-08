package ms.cms.plugin.mgmt.asset;

import com.hp.hpl.jena.graph.Triple;
import ms.cms.plugin.mgmt.PluginOperationException;
import ms.cms.plugin.mgmt.PluginStatus;
import ms.cms.plugin.mgmt.asset.utils.HttpHelper;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.fcrepo.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import static org.apache.http.HttpStatus.*;

/**
 * FedoraAssetManagementPlugin
 * Created by bazzoni on 08/05/2015.
 */
public class FedoraAssetManagementPlugin extends AbstractAssetManagementPlugin<FedoraContainer, FedoraAsset> implements FedoraRepository {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected HttpHelper httpHelper;
    protected String repositoryURL;
    protected String username;
    protected String password;

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
            FedoraObject fedoraObject = createObject(siteId);
            return fedoraObject.getPath();
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
            findOrCreateObject(siteId).delete();
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
            FedoraObject fedoraObject = createObject(siteId + "/" + path);
            return fedoraObject.getPath();
        } catch (FedoraException e) {
            throw new PluginOperationException("Fedora Repository related error.", e);
        }
    }

    /**
     * Deletes a folder
     *
     * @param siteId site id
     * @param path internal path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public void deleteFolder(String siteId, String path) throws PluginOperationException {
        try {
            findOrCreateObject(siteId + "/" + path).delete();
        } catch (FedoraException e) {
            throw new PluginOperationException("Fedora Repository related error.", e);
        }
    }

    /**
     * Creates an asset for the site and folder
     *
     * @param siteId site id
     * @param path   internal path
     * @param name   asset name
     * @param data   asset data
     * @param contentType content type
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public String createAsset(String siteId, String path, String name, byte[] data, String contentType) throws PluginOperationException {
        try {
            FedoraContent content = new FedoraContent().setContent(new ByteArrayInputStream(data))
                    .setContentType(contentType);
            FedoraDatastream datastream = createDatastream(siteId + "/" + path + "/" + name, content);
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
            findOrCreateDatastream(siteId + "/" + path + "/" + name).delete();
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
            return findOrCreateObject(siteId);
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
            return findOrCreateObject(siteId + "/" + path);
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
            return findOrCreateDatastream(siteId + "/" + path + "/" + name);
        } catch (FedoraException e) {
            throw new PluginOperationException("Fedora Repository related error.", e);
        }
    }

    /**
     * Validates plugin
     *
     * @throws PluginOperationException if error
     */
    @Override
    protected void doValidate() throws PluginOperationException {
        repositoryURL = getSetting("repositoryUrl", String.class, properties.getProperty("plugin.repositoryURL"));
        if (repositoryURL.isEmpty()) {
            throw new PluginOperationException("Cannot define repository URL");
        }
        username = getSetting("username", String.class, properties.getProperty("plugin.username"));
        if (username.isEmpty()) username = null;
        password = getSetting("password", String.class, properties.getProperty("plugin.password"));
        if (password.isEmpty()) password = null;
        this.httpHelper = new HttpHelper(repositoryURL, username, password, false);

        status = PluginStatus.ACTIVE;
    }

    @Override
    public boolean exists(final String path) throws FedoraException {
        final HttpHead head = httpHelper.createHeadMethod(path);
        try {
            final HttpResponse response = httpHelper.execute(head);
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();
            final String uri = head.getURI().toString();
            if (statusCode == SC_OK) {
                return true;
            } else if (statusCode == SC_NOT_FOUND) {
                return false;
            } else if (statusCode == SC_FORBIDDEN) {
                logger.error("request for resource {} is not authorized.", uri);
                throw new ForbiddenException("request for resource " + uri + " is not authorized.");
            } else {
                logger.error("error checking resource {}: {} {}", uri, statusCode, status.getReasonPhrase());
                throw new FedoraException("error checking resource " + uri + ": " + statusCode + " " +
                        status.getReasonPhrase());
            }
        } catch (final Exception e) {
            logger.error("could not encode URI parameter", e);
            throw new FedoraException(e);
        } finally {
            head.releaseConnection();
        }
    }

    @Override
    public FedoraAsset getDatastream(final String path) throws FedoraException {
        return (FedoraAsset) httpHelper.loadProperties(new FedoraAsset(this, httpHelper, path));
    }

    @Override
    public FedoraContainer getObject(final String path) throws FedoraException {
        return (FedoraContainer) httpHelper.loadProperties(new FedoraContainer(this, httpHelper, path));
    }

    @Override
    public FedoraAsset createDatastream(final String path, final FedoraContent content) throws FedoraException {
        final HttpPut put = httpHelper.createContentPutMethod(path, null, content);
        try {
            final HttpResponse response = httpHelper.execute(put);
            final String uri = put.getURI().toString();
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();

            if (statusCode == SC_CREATED) {
                return getDatastream(path);
            } else if (statusCode == SC_FORBIDDEN) {
                logger.error("request to create resource {} is not authorized.", uri);
                throw new ForbiddenException("request to create resource " + uri + " is not authorized.");
            } else if (statusCode == SC_CONFLICT) {
                logger.error("resource {} already exists", uri);
                throw new FedoraException("resource " + uri + " already exists");
            } else {
                logger.error("error creating resource {}: {} {}", uri, statusCode, status.getReasonPhrase());
                throw new FedoraException("error retrieving resource " + uri + ": " + statusCode + " " +
                        status.getReasonPhrase());
            }
        } catch (final Exception e) {
            logger.error("could not encode URI parameter", e);
            throw new FedoraException(e);
        } finally {
            put.releaseConnection();
        }
    }

    @Override
    public FedoraContainer createObject(final String path) throws FedoraException {
        final HttpPut put = httpHelper.createPutMethod(path, null);
        try {
            final HttpResponse response = httpHelper.execute(put);
            final String uri = put.getURI().toString();
            final StatusLine status = response.getStatusLine();
            final int statusCode = status.getStatusCode();

            if (statusCode == SC_CREATED) {
                return getObject(path);
            } else if (statusCode == SC_FORBIDDEN) {
                logger.error("request to create resource {} is not authorized.", uri);
                throw new ForbiddenException("request to create resource " + uri + " is not authorized.");
            } else if (statusCode == SC_CONFLICT) {
                logger.error("resource {} already exists", uri);
                throw new FedoraException("resource " + uri + " already exists");
            } else {
                logger.error("error creating resource {}: {} {}", uri, statusCode, status.getReasonPhrase());
                throw new FedoraException("error retrieving resource " + uri + ": " + statusCode + " " +
                        status.getReasonPhrase());
            }
        } catch (final Exception e) {
            logger.error("could not encode URI parameter", e);
            throw new FedoraException(e);
        } finally {
            put.releaseConnection();
        }
    }

    @Override
    public FedoraAsset findOrCreateDatastream(final String path) throws FedoraException {
        try {
            return getDatastream(path);
        } catch (NotFoundException ex) {
            return createDatastream(path, null);
        }
    }

    @Override
    public FedoraContainer findOrCreateObject(final String path) throws FedoraException {
        try {
            return getObject(path);
        } catch (NotFoundException ex) {
            return createObject(path);
        }
    }

    @Override
    public Iterator<Triple> getNodeTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void registerNodeTypes(final InputStream cndStream) throws ReadOnlyException {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, String> getRepositoryNamespaces() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addNamespace(final String prefix, final String uri) throws ReadOnlyException {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeNamespace(final String prefix) throws ReadOnlyException {
        // TODO Auto-generated method stub

    }

    @Override
    public Long getRepositoryObjectCount() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getRepositorySize() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public String getRepositoryUrl() {
        return repositoryURL;
    }
}
