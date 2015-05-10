package sparkle.cms.plugin.mgmt.asset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * FileAsset
 * Created by bazzoni on 07/05/2015.
 */
public class FileAsset implements Asset {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Path path;
    private ByteBuffer data;

    public FileAsset(Path path) {
        this(path, null);
    }

    public FileAsset(Path path, byte[] data) {
        this.path = path;
        if (data == null) {
            loadData();
        } else {
            saveData(data);
        }
    }

    /**
     * Returns Asset content bytes
     *
     * @return content bytes
     */
    @Override
    public byte[] getContent() {
        return data.array();
    }

    /**
     * Get Asset URI
     *
     * @return asset URI
     */
    @Override
    public String getUri() {
        return path.toString();
    }

    private void saveData(byte[] data) {
        try {
            Files.deleteIfExists(path);
            Files.createFile(path);
            ByteBuffer bb = ByteBuffer.wrap(data);
            try (SeekableByteChannel sbc =
                         Files.newByteChannel(path, StandardOpenOption.WRITE)) {
                sbc.write(bb);
            } catch (IOException e) {
                logger.debug("Cannot load resource", e);
            }
        } catch (IOException e) {
            logger.debug("Cannot load resource", e);
        }
    }

    private void loadData() {
        try (SeekableByteChannel sbc = Files.newByteChannel(path, StandardOpenOption.READ)) {
            data = ByteBuffer.allocate((int) sbc.size());
            sbc.read(data);
        } catch (IOException e) {
            logger.debug("Cannot load resource", e);
        }
    }

    @Override
    public String toString() {
        return getUri();
    }
}
