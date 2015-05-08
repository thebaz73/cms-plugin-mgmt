package ms.cms.plugin.mgmt.asset;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import ms.cms.plugin.mgmt.asset.utils.HttpHelper;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.jena.atlas.lib.NotImplemented;
import org.fcrepo.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.http.HttpStatus.*;
import static org.fcrepo.kernel.RdfLexicon.*;


/**
 * FedoraAsset
 * Created by bazzoni on 08/05/2015.
 */
public class FedoraAsset extends FedoraResourceImpl implements FedoraDatastream, Asset {
    protected static final Property REST_API_DIGEST = createProperty(REPOSITORY_NAMESPACE + "digest");

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private boolean hasContent;
    private Node contentSubject;

    /**
     * Constructor for FedoraAsset
     *
     * @param repository Repository that created this object.
     * @param httpHelper HTTP helper for making repository requests
     * @param path       Path of the datastream in the repository
     */
    public FedoraAsset(final FedoraRepository repository, final HttpHelper httpHelper, final String path) {
        super(repository, httpHelper, path);
        contentSubject = NodeFactory.createURI(
                repository.getRepositoryUrl() + path.substring(0, path.lastIndexOf("/")));
    }

    /**
     * Get Asset URI
     *
     * @return asset URI
     */
    @Override
    public String getUri() {
        try {
            return getContentDigest().toString();
        } catch (FedoraException e) {
            logger.debug("Fedora Repository related error.", e);
        }

        return null;
    }

    @Override
    public void setGraph(final Graph graph) {
        super.setGraph(graph);
        hasContent = getTriple(subject, DESCRIBES) != null;
    }

    @Override
    public boolean hasContent() throws FedoraException {
        return hasContent;
    }

    @Override
    public FedoraObject getObject() throws FedoraException {
        final String contentPath = path.substring(0, path.lastIndexOf("/"));
        return repository.getObject(contentPath.substring(0, contentPath.lastIndexOf("/")));
    }

    @Override
    public String getName() {
        final String p = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        final String[] paths = p.split("/");
        return paths[paths.length - 2];
    }

    @Override
    public URI getContentDigest() throws FedoraException {
        final Node contentDigest = getObjectValue(REST_API_DIGEST);
        try {
            if (contentDigest == null) {
                return null;
            }

            return new URI(contentDigest.getURI());
        } catch (final URISyntaxException e) {
            throw new FedoraException("Error parsing checksum URI: " + contentDigest.getURI(), e);
        }
    }

    @Override
    public Long getContentSize() throws FedoraException {
        final Node size = getObjectValue(HAS_SIZE);
        if (size == null) {
            return null;
        }

        return new Long(size.getLiteralValue().toString());
    }

    @Override
    public String getFilename() throws FedoraException {
        final Node filename = getObjectValue(HAS_ORIGINAL_NAME);
        if (filename == null) {
            return null;
        }

        return filename.getLiteralValue().toString();
    }

    @Override
    public String getContentType() throws FedoraException {
        final Node contentType = getObjectValue(HAS_MIME_TYPE);
        if (contentType == null) {
            return null;
        }

        return contentType.getLiteralValue().toString();
    }

    @Override
    public void updateContent(final FedoraContent content) throws FedoraException {
        final HttpPut put = httpHelper.createContentPutMethod(path, null, content);

        try {
            final HttpResponse response = httpHelper.execute(put);
            final StatusLine status = response.getStatusLine();
            final String uri = put.getURI().toString();

            if (status.getStatusCode() == SC_CREATED
                    || status.getStatusCode() == SC_NO_CONTENT) {
                logger.debug("content updated successfully for resource {}", uri);
            } else if (status.getStatusCode() == SC_FORBIDDEN) {
                logger.error("request for resource {} is not authorized.", uri);
                throw new ForbiddenException("request for resource " + uri + " is not authorized.");
            } else if (status.getStatusCode() == SC_NOT_FOUND) {
                logger.error("resource {} does not exist, cannot retrieve", uri);
                throw new NotFoundException("resource " + uri + " does not exist, cannot retrieve");
            } else if (status.getStatusCode() == SC_CONFLICT) {
                logger.error("checksum mismatch for {}", uri);
                throw new FedoraException("checksum mismatch for resource " + uri);
            } else {
                logger.error("error retrieving resource {}: {} {}", uri, status.getStatusCode(),
                        status.getReasonPhrase());
                throw new FedoraException("error retrieving resource " + uri + ": " + status.getStatusCode() + " " +
                        status.getReasonPhrase());
            }

            // update properties from server
            httpHelper.loadProperties(this);

        } catch (final FedoraException e) {
            throw e;
        } catch (final Exception e) {
            logger.error("could not encode URI parameter", e);
            throw new FedoraException(e);
        } finally {
            put.releaseConnection();
        }
    }

    @Override
    public InputStream getContent() throws FedoraException {
        final HttpGet get = httpHelper.createGetMethod(path, null);
        final String uri = get.getURI().toString();

        try {
            final HttpResponse response = httpHelper.execute(get);
            final StatusLine status = response.getStatusLine();

            if (status.getStatusCode() == SC_OK) {
                return response.getEntity().getContent();
            } else if (status.getStatusCode() == SC_FORBIDDEN) {
                logger.error("request for resource {} is not authorized.", uri);
                throw new ForbiddenException("request for resource " + uri + " is not authorized.");
            } else if (status.getStatusCode() == SC_NOT_FOUND) {
                logger.error("resource {} does not exist, cannot retrieve", uri);
                throw new NotFoundException("resource " + uri + " does not exist, cannot retrieve");
            } else {
                logger.error("error retrieving resource {}: {} {}", uri, status.getStatusCode(),
                        status.getReasonPhrase());
                throw new FedoraException("error retrieving resource " + uri + ": " + status.getStatusCode() + " " +
                        status.getReasonPhrase());
            }
        } catch (final Exception e) {
            logger.error("could not encode URI parameter", e);
            throw new FedoraException(e);
        } finally {
            get.releaseConnection();
        }
    }

    @Override
    public void checkFixity() {
        throw new NotImplemented("Method checkFixity() is not implemented");
    }

    private Node getObjectValue(final Property property) {
        if (!hasContent) {
            return null;
        }

        final Triple t = getTriple(contentSubject, property);
        if (t == null) {
            return null;
        }

        return t.getObject();
    }
}
