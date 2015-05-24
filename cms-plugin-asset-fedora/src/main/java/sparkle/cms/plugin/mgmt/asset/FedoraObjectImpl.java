package sparkle.cms.plugin.mgmt.asset;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.FedoraResource;
import sparkle.cms.plugin.mgmt.asset.utils.HttpHelper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.fcrepo.kernel.RdfLexicon.CONTAINS;
import static org.fcrepo.kernel.RdfLexicon.HAS_MIXIN_TYPE;

/**
 * FedoraObjectImpl
 * Created by bazzoni on 09/05/2015.
 */
public class FedoraObjectImpl extends FedoraResourceImpl implements FedoraObject {
    private final static Node containerType = NodeFactory.createLiteral("fedora:Container");

    /**
     * FedoraObjectImpl constructor
     *
     * @param repository FedoraRepositoryImpl that created this resource
     * @param httpHelper HTTP helper for making repository requests
     * @param path       Repository path of this resource
     */
    public FedoraObjectImpl(FedoraRepository repository, HttpHelper httpHelper, String path) {
        super(repository, httpHelper, path);
    }

    /**
     * Get the Object and Datastream nodes that are children of the current Object.
     *
     * @param mixin If not null, limit to results that have this mixin.
     */
    public Collection<FedoraResource> getChildren(final String mixin) throws FedoraException {
        Node mixinLiteral = null;
        if (mixin != null) {
            mixinLiteral = NodeFactory.createLiteral(mixin);
        }
        final ExtendedIterator<Triple> it = graph.find(Node.ANY, CONTAINS.asNode(), Node.ANY);
        final Set<FedoraResource> set = new HashSet<>();
        while (it.hasNext()) {
            final Node child = it.next().getObject();
            if (mixin == null || graph.contains(child, HAS_MIXIN_TYPE.asNode(), mixinLiteral)) {
                final String path = child.getURI()
                        .replaceAll(repository.getRepositoryUrl(), "");
                if (graph.contains(child, HAS_MIXIN_TYPE.asNode(), containerType)) {
                    set.add(repository.getObject(path));
                } else {
                    set.add(repository.getDatastream(path));
                }
            }
        }
        return set;
    }
}
