package ms.cms.plugin.mgmt.asset;

import com.hp.hpl.jena.graph.Triple;
import ms.cms.plugin.mgmt.PluginOperationException;
import ms.cms.plugin.mgmt.asset.utils.HttpHelper;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.fcrepo.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * Creates a base repository container for site
     *
     * @param siteId site id
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public String createSiteRepository(String siteId) throws PluginOperationException {
        return null;
    }

    /**
     * Deletes a site repository container
     *
     * @param siteId site id
     * @throws PluginOperationException if operation failure
     */
    @Override
    public void deleteSiteRepository(String siteId) throws PluginOperationException {

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
        return null;
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

    }

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
    @Override
    public String createAsset(String siteId, String path, String name, byte[] data) throws PluginOperationException {
        return null;
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

    }

    /**
     * Find a site Repository
     *
     * @param siteId siteId
     * @return site container
     */
    @Override
    public FedoraContainer findSiteRepository(String siteId) {
        return null;
    }

    /**
     * Find a folder
     *
     * @param siteId siteId
     * @param path   internal path
     * @return folder container
     */
    @Override
    public FedoraContainer findFolder(String siteId, String path) {
        return null;
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
    public FedoraAsset findAsset(String siteId, String path, String name) {
        return null;
    }

    /**
     * Validates plugin
     *
     * @throws PluginOperationException if error
     */
    @Override
    protected void doValidate() throws PluginOperationException {

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
    public FedoraDatastream getDatastream(final String path) throws FedoraException {
        return (FedoraDatastream) httpHelper.loadProperties(new FedoraDatastreamImpl(this, httpHelper, path));
    }

    @Override
    public FedoraObject getObject(final String path) throws FedoraException {
        return (FedoraObject) httpHelper.loadProperties(new FedoraObjectImpl(this, httpHelper, path));
    }

    @Override
    public FedoraDatastream createDatastream(final String path, final FedoraContent content) throws FedoraException {
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
    public FedoraObject createObject(final String path) throws FedoraException {
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
    public FedoraDatastream findOrCreateDatastream(final String path) throws FedoraException {
        try {
            return getDatastream(path);
        } catch (NotFoundException ex) {
            return createDatastream(path, null);
        }
    }

    @Override
    public FedoraObject findOrCreateObject(final String path) throws FedoraException {
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
