package sparkle.cms.plugin.mgmt.asset;


/**
 * FedoraAsset
 * Created by bazzoni on 08/05/2015.
 */
public class FedoraAsset implements Asset {
    private final String uri;

    public FedoraAsset(String uri) {
        this.uri = uri;
    }


    /**
     * Get Asset URI
     *
     * @return asset URI
     */
    @Override
    public String getUri() {
        return uri;
    }

    /**
     * Get content as byte array
     *
     * @return byte array
     */
    @Override
    public byte[] getContent() {
        return null;
    }
}
