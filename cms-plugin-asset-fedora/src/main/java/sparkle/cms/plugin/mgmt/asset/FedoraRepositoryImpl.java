package sparkle.cms.plugin.mgmt.asset;

import com.hp.hpl.jena.graph.Triple;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.fcrepo.client.*;
import org.slf4j.Logger;
import sparkle.cms.plugin.mgmt.asset.utils.HttpHelper;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import static org.apache.http.HttpStatus.*;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * FedoraRepositoryImpl
 * Created by bazzoni on 09/05/2015.
 */
public class FedoraRepositoryImpl implements FedoraRepository {
    private static final Logger logger = getLogger(FedoraRepositoryImpl.class);

    protected HttpHelper httpHelper;
    protected String repositoryURL;

    protected FedoraRepositoryImpl() {
        // for subclasses
    }

    /**
     * Constructor that takes the repository url
     *
     * @param repositoryURL Fedora base URL.
     */
    public FedoraRepositoryImpl(final String repositoryURL) {
        this(repositoryURL, null, null);
    }

    /**
     * Constructor
     *
     * @param repositoryURL Repository base URL
     * @param username      Repository username
     * @param password      Repository password
     */
    public FedoraRepositoryImpl(final String repositoryURL, final String username, final String password) {
        this.repositoryURL = repositoryURL;
        this.httpHelper = new HttpHelper(repositoryURL, username, password, false);
    }

    /**
     * Constructor that takes the pre-configured HttpClient
     *
     * @param repositoryURL Repository baseURL
     * @param httpClient    Pre-configured httpClient
     */
    public FedoraRepositoryImpl(final String repositoryURL, final HttpClient httpClient) {
        this.repositoryURL = repositoryURL;
        this.httpHelper = new HttpHelper(repositoryURL, httpClient, false);
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
