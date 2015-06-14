package sparkle.cms.plugin.mgmt.asset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

/**
 * OakAsset
 * Created by bazzoni on 14/06/2015.
 */
public class OakAsset implements Asset {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String uri;
    private final ByteArrayOutputStream baos;

    public OakAsset(String uri, ByteArrayOutputStream baos) {
        this.uri = uri;
        this.baos = baos;
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
        return baos.toByteArray();
    }
}
