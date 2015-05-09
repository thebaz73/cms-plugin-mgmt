package ms.cms.plugin.mgmt.asset;

/**
 * Asset
 * Created by bazzoni on 07/05/2015.
 */
public interface Asset {
    /**
     * Get Asset URI
     *
     * @return asset URI
     */
    String getUri();

    /**
     * Get content as byte array
     *
     * @return byte array
     */
    byte[] getContent();
}
