package ms.cms.plugin.mgmt.asset;

import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FedoraContainer
 * Created by bazzoni on 08/05/2015.
 */
public class FedoraContainer implements Container {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final FedoraObject fedoraObject;

    public FedoraContainer(FedoraObject fedoraObject) {
        this.fedoraObject = fedoraObject;
    }

    /**
     * Checks if container as children
     *
     * @return true is children number &gt; 0
     */
    @Override
    public boolean hasChildren() {
        try {
            return fedoraObject.getChildren(null).size() > 0;
        } catch (FedoraException e) {
            logger.debug("Getting data:", e);
            return false;
        }
    }
}
