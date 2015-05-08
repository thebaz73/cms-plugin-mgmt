package ms.cms.plugin.mgmt.asset;

import ms.cms.plugin.mgmt.asset.utils.HttpHelper;
import org.fcrepo.client.FedoraRepository;

/**
 * FedoraAsset
 * Created by bazzoni on 08/05/2015.
 */
public class FedoraAsset extends FedoraDatastreamImpl implements Asset {
    /**
     * Constructor for FedoraDatastreamImpl
     *
     * @param repository Repository that created this object.
     * @param httpHelper HTTP helper for making repository requests
     * @param path       Path of the datastream in the repository
     */
    public FedoraAsset(FedoraRepository repository, HttpHelper httpHelper, String path) {
        super(repository, httpHelper, path);
    }

    /**
     * Get Asset URI
     *
     * @return asset URI
     */
    @Override
    public String getUri() {
        return null;
    }
}
