package ms.cms.plugin.mgmt.asset.utils;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.shared.RandomOrderGraph;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.riot.lang.SinkTriplesToGraph;
import org.apache.jena.riot.system.StreamRDFBase;

import java.util.Iterator;

/**
 * RDFSinkFilter
 * Created by bazzoni on 08/05/2015.
 */
public class RDFSinkFilter extends StreamRDFBase {
    // properties to filter
    private final Node[] properties;
    // destination to send the triples filtered.
    private final Sink<Triple> dest;

    private RDFSinkFilter(final Sink<Triple> dest, final Node... properties) {
        this.dest = dest;
        this.properties = new Node[properties.length];
        System.arraycopy(properties, 0, this.properties, 0, properties.length);
    }

    /**
     * Filter the triples
     *
     * @param triples    Iterator of triples
     * @param properties Properties to include
     * @return Graph containing the fitlered triples
     */
    public static Graph filterTriples(
            final Iterator<Triple> triples,
            final Node... properties) {
        final Graph filteredGraph = new RandomOrderGraph(RandomOrderGraph.createDefaultGraph());
        final Sink<Triple> graphOutput = new SinkTriplesToGraph(true, filteredGraph);
        final RDFSinkFilter rdfFilter = new RDFSinkFilter(graphOutput, properties);
        rdfFilter.start();
        while (triples.hasNext()) {
            final Triple triple = triples.next();
            rdfFilter.triple(triple);
        }
        rdfFilter.finish();
        return filteredGraph;
    }

    @Override
    public void triple(final Triple triple) {
        for (final Node p : properties) {
            if (Node.ANY == p || triple.getPredicate().equals(p)) {
                dest.send(triple);
            }
        }
    }

    @Override
    public void finish() {
        // flush the buffered.
        dest.flush();
    }
}
