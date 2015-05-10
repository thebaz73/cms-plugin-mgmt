package sparkle.cms.plugin.mgmt.asset;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.jena.atlas.lib.NotImplemented;
import org.fcrepo.client.*;
import org.fcrepo.kernel.RdfLexicon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sparkle.cms.plugin.mgmt.asset.utils.HttpHelper;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.http.HttpStatus.*;


/**
 * FedoraResourceImpl
 * Created by bazzoni on 08/05/2015.
 */
public class FedoraResourceImpl implements FedoraResource {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected FedoraRepository repository = null;

    protected HttpHelper httpHelper = null;

    protected String path = null;

    protected Node subject = null;

    protected Graph graph;

    private String etagValue = null;

    /**
     * FedoraResourceImpl constructor
     *
     * @param repository FedoraRepositoryImpl that created this resource
     * @param httpHelper HTTP helper for making repository requests
     * @param path       Repository path of this resource
     */
    public FedoraResourceImpl(final FedoraRepository repository, final HttpHelper httpHelper, final String path) {
        this.repository = repository;
        this.httpHelper = httpHelper;
        this.path = path;
        subject = NodeFactory.createURI(repository.getRepositoryUrl() + path);
    }

    @Override
    public void copy(final String destination) throws ReadOnlyException {
        // TODO Auto-generated method stub
        throw new NotImplemented("Method copy(final String destination) is not implemented.");
    }

    @Override
    public void delete() throws ReadOnlyException {
        if (!isWritable()) {
            throw new ReadOnlyException();
        }
        final HttpDelete delete = httpHelper.createDeleteMethod(path);
        final HttpDelete deleteTombstone = httpHelper.createDeleteMethod(String.format("%s/fcr:tombstone", path));
        try {
            if (doDelete(delete) == HttpStatus.SC_NO_CONTENT) {
                doDelete(deleteTombstone);
            }
        } catch (IOException e) {
            logger.debug("Cannot delete", e);
        } finally {
            delete.releaseConnection();
            deleteTombstone.releaseConnection();
        }
    }

    private int doDelete(HttpDelete delete) throws IOException, ReadOnlyException {
        final HttpResponse response = httpHelper.execute(delete);
        final StatusLine status = response.getStatusLine();
        if (status.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            logger.error("error delete resource {}: {} {}", delete.getURI().toString(), status.getStatusCode(), status.getReasonPhrase());
            throw new IOException("Resource not found");
        }
        return status.getStatusCode();
    }

    @Override
    public Date getCreatedDate() {
        return getDate(RdfLexicon.CREATED_DATE);
    }

    @Override
    public String getEtagValue() {
        return etagValue;
    }

    /**
     * set etagValue
     *
     * @param etagValue etag value
     */
    public void setEtagValue(final String etagValue) {
        this.etagValue = etagValue;
    }

    @Override
    public Date getLastModifiedDate() {
        return getDate(RdfLexicon.LAST_MODIFIED_DATE);
    }

    @Override
    public Collection<String> getMixins() {
        return getPropertyValues(RdfLexicon.HAS_MIXIN_TYPE);
    }

    @Override
    public String getName() {
        final String p = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        final String[] paths = p.split("/");
        return paths[paths.length - 1];
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Iterator<Triple> getProperties() {
        return graph.find(Node.ANY, Node.ANY, Node.ANY);
    }

    @Override
    public Long getSize() {
        return (long) graph.size();
    }

    @Override
    public void move(final String destination) throws ReadOnlyException {
        // TODO Auto-generated method stub
        throw new NotImplemented("Method move(final String destination) is not implemented.");
    }

    @Override
    public void updateProperties(final String sparqlUpdate) throws FedoraException {
        final HttpPatch patch = httpHelper.createPatchMethod(path, sparqlUpdate);

        try {
            final HttpResponse response = httpHelper.execute(patch);
            final StatusLine status = response.getStatusLine();
            final String uri = patch.getURI().toString();

            if (status.getStatusCode() == SC_NO_CONTENT) {
                logger.debug("triples updated successfully for resource {}", uri);
            } else if (status.getStatusCode() == SC_FORBIDDEN) {
                logger.error("updating resource {} is not authorized.", uri);
                throw new ForbiddenException("updating resource " + uri + " is not authorized.");
            } else if (status.getStatusCode() == SC_NOT_FOUND) {
                logger.error("resource {} does not exist, cannot update", uri);
                throw new NotFoundException("resource " + uri + " does not exist, cannot update");
            } else if (status.getStatusCode() == SC_CONFLICT) {
                logger.error("resource {} is locked", uri);
                throw new FedoraException("resource is locked: " + uri);
            } else {
                logger.error("error updating resource {}: {} {}", uri, status.getStatusCode(),
                        status.getReasonPhrase());
                throw new FedoraException("error updating resource " + uri + ": " + status.getStatusCode() + " " +
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
            patch.releaseConnection();
        }
    }

    @Override
    public void updateProperties(final InputStream updatedProperties, final String contentType)
            throws FedoraException {

        final HttpPut put = httpHelper.createTriplesPutMethod(path, updatedProperties, contentType);

        try {
            final HttpResponse response = httpHelper.execute(put);
            final StatusLine status = response.getStatusLine();
            final String uri = put.getURI().toString();

            if (status.getStatusCode() == SC_NO_CONTENT) {
                logger.debug("triples updated successfully for resource {}", uri);
            } else if (status.getStatusCode() == SC_FORBIDDEN) {
                logger.error("updating resource {} is not authorized.", uri);
                throw new ForbiddenException("updating resource " + uri + " is not authorized.");
            } else if (status.getStatusCode() == SC_NOT_FOUND) {
                logger.error("resource {} does not exist, cannot update", uri);
                throw new NotFoundException("resource " + uri + " does not exist, cannot update");
            } else if (status.getStatusCode() == SC_CONFLICT) {
                logger.error("resource {} is locked", uri);
                throw new FedoraException("resource is locked: " + uri);
            } else {
                logger.error("error updating resource {}: {} {}", uri, status.getStatusCode(),
                        status.getReasonPhrase());
                throw new FedoraException("error updating resource " + uri + ": " + status.getStatusCode() + " " +
                        status.getReasonPhrase());
            }

            // update properties from server
            httpHelper.loadProperties(this);

        } catch (final FedoraException e) {
            throw e;
        } catch (final Exception e) {
            logger.error("Error executing request", e);
            throw new FedoraException(e);
        } finally {
            put.releaseConnection();
        }
    }

    @Override
    public boolean isWritable() {
        final Collection<String> values = getPropertyValues(RdfLexicon.WRITABLE);
        if (values != null && values.size() > 0) {
            final Iterator<String> it = values.iterator();
            return Boolean.parseBoolean(it.next());
        }
        return false;
    }

    @Override
    public void createVersionSnapshot(final String label) throws FedoraException {
        final HttpPost postVersion = httpHelper.createPostMethod(path + "/fcr:versions", null);
        try {
            postVersion.setHeader("Slug", label);
            final HttpResponse response = httpHelper.execute(postVersion);
            final StatusLine status = response.getStatusLine();
            final String uri = postVersion.getURI().toString();

            if (status.getStatusCode() == SC_NO_CONTENT) {
                logger.debug("new version created for resource at {}", uri);
            } else if (status.getStatusCode() == SC_CONFLICT) {
                logger.debug("The label {} is in use by another version.", label);
                throw new FedoraException("The label \"" + label + "\" is in use by another version.");
            } else if (status.getStatusCode() == SC_FORBIDDEN) {
                logger.error("updating resource {} is not authorized.", uri);
                throw new ForbiddenException("updating resource " + uri + " is not authorized.");
            } else if (status.getStatusCode() == SC_NOT_FOUND) {
                logger.error("resource {} does not exist, cannot create version", uri);
                throw new NotFoundException("resource " + uri + " does not exist, cannot create version");
            } else {
                logger.error("error updating resource {}: {} {}", uri, status.getStatusCode(),
                        status.getReasonPhrase());
                throw new FedoraException("error updating resource " + uri + ": " + status.getStatusCode() + " " +
                        status.getReasonPhrase());
            }
        } catch (IOException e) {
            logger.error("Error executing request", e);
            throw new FedoraException(e);
        } finally {
            postVersion.releaseConnection();
        }
    }

    /**
     * Get the properties graph
     *
     * @return Graph containing properties for this resource
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Update the properties graph
     */
    public void setGraph(final Graph graph) {
        this.graph = graph;
    }

    private Date getDate(final Property property) {
        Date date = null;
        final Triple t = getTriple(subject, property);
        if (t != null) {
            final String dateValue = t.getObject().getLiteralValue().toString();
            try {
                date = dateFormat.parse(dateValue);
            } catch (final ParseException e) {
                logger.debug("Invalid date format error: " + dateValue);
            }
        }
        return date;
    }

    /**
     * Return all the values of a property
     *
     * @param property The Property to get values for
     * @return Collection of values
     */
    protected Collection<String> getPropertyValues(final Property property) {
        final ExtendedIterator<Triple> iterator = graph.find(Node.ANY,
                property.asNode(),
                Node.ANY);
        final Set<String> set = new HashSet<>();
        while (iterator.hasNext()) {
            final Node object = iterator.next().getObject();
            if (object.isLiteral()) {
                set.add(object.getLiteralValue().toString());
            } else if (object.isURI()) {
                set.add(object.getURI());
            } else {
                set.add(object.toString());
            }
        }
        return set;
    }

    protected Triple getTriple(final Node subject, final Property property) {
        final ExtendedIterator<Triple> it = graph.find(subject, property.asNode(), null);
        try {
            if (it.hasNext()) {
                return it.next();
            } else {
                return null;
            }
        } finally {
            it.close();
        }
    }

}
