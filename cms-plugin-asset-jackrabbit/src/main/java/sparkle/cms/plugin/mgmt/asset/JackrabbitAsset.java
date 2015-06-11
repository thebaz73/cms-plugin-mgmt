package sparkle.cms.plugin.mgmt.asset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;

/**
 * JackrabbitAsset
 * Created by bazzoni on 10/06/2015.
 */
public class JackrabbitAsset implements Asset {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String uri;
    private final ByteArrayOutputStream baos;

    public JackrabbitAsset(String uri, ByteArrayOutputStream baos) {
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
