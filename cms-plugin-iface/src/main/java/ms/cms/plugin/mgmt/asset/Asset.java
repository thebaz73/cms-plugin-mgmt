package ms.cms.plugin.mgmt.asset;

/**
 * Asset
 * Created by bazzoni on 07/05/2015.
 */
public interface Asset {
    /**
     * Returns Asset content bytes
     *
     * @return content bytes
     */
    byte[] getContent();

    /**
     * Get Asset URI
     *
     * @return asset URI
     */
    String getUri();
}
