package ms.cms.plugin.mgmt.asset;

/**
 * ContentAsset
 * Created by bazzoni on 08/05/2015.
 */
public interface ContentAsset extends Asset {
    /**
     * Get content as byte array
     *
     * @return byte array
     */
    byte[] getContent();
}
