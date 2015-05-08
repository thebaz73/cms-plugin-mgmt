package ms.cms.plugin.mgmt.asset;

import ms.cms.plugin.mgmt.asset.utils.HttpHelper;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FedoraContainer
 * Created by bazzoni on 08/05/2015.
 */
public class FedoraContainer extends FedoraObjectImpl implements Container {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Constructor for FedoraObjectImpl
     *
     * @param repository FedoraRepository that created this object
     * @param httpHelper HTTP helper for making repository requests
     * @param path       Repository path
     */
    public FedoraContainer(FedoraRepository repository, HttpHelper httpHelper, String path) {
        super(repository, httpHelper, path);
    }

    /**
     * Checks if container as children
     *
     * @return true is children number &gt; 0
     */
    @Override
    public boolean hasChildren() {
        try {
            return getChildren(null).size() > 0;
        } catch (FedoraException e) {
            logger.debug("Getting data:", e);
            return false;
        }
    }
}
