package sparkle.cms.plugin.mgmt.asset;

/**
 * DummyAsset
 * Created by bazzoni on 07/05/2015.
 */
public class DummyAsset implements Asset {
    private final String uri;
    private final byte[] data;

    public DummyAsset(String uri, byte[] data) {
        this.uri = uri;
        this.data = data;
    }

    /**
     * Returns Asset content bytes
     *
     * @return content bytes
     */
    @Override
    public byte[] getContent() {
        return data;
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
}
