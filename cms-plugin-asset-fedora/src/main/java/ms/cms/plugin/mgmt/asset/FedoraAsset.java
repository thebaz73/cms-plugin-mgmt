package ms.cms.plugin.mgmt.asset;

import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * FedoraAsset
 * Created by bazzoni on 08/05/2015.
 */
public class FedoraAsset implements Asset {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final FedoraDatastream fedoraDatastream;

    public FedoraAsset(FedoraDatastream fedoraDatastream) {
        this.fedoraDatastream = fedoraDatastream;
    }


    /**
     * Get Asset URI
     *
     * @return asset URI
     */
    @Override
    public String getUri() {
        try {
            return fedoraDatastream.getContentDigest().toString();
        } catch (FedoraException e) {
            logger.debug("Fedora Repository related error.", e);
        }

        return null;
    }

    /**
     * Get content as byte array
     *
     * @return byte array
     */
    @Override
    public byte[] getContent() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            while (fedoraDatastream.getContent().read(b) != -1) {
                baos.write(b);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            logger.debug("I/O error.", e);
        } catch (FedoraException e) {
            logger.debug("Fedora Repository related error.", e);
        }

        return null;
    }
}
