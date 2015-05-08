package ms.cms.plugin.mgmt.asset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * FileAsset
 * Created by bazzoni on 07/05/2015.
 */
public class FileAsset extends File implements Asset {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ByteBuffer data;

    /**
     * Creates a new <code>File</code> instance from a parent abstract
     * pathname and a child pathname string.
     * <p>
     * <p> If <code>parent</code> is <code>null</code> then the new
     * <code>File</code> instance is created as if by invoking the
     * single-argument <code>File</code> constructor on the given
     * <code>child</code> pathname string.
     * <p>
     * <p> Otherwise the <code>parent</code> abstract pathname is taken to
     * denote a directory, and the <code>child</code> pathname string is taken
     * to denote either a directory or a file.  If the <code>child</code>
     * pathname string is absolute then it is converted into a relative
     * pathname in a system-dependent way.  If <code>parent</code> is the empty
     * abstract pathname then the new <code>File</code> instance is created by
     * converting <code>child</code> into an abstract pathname and resolving
     * the result against a system-dependent default directory.  Otherwise each
     * pathname string is converted into an abstract pathname and the child
     * abstract pathname is resolved against the parent.
     *
     * @param parent The parent abstract pathname
     * @param child  The child pathname string
     * @param data   The asset data
     * @throws NullPointerException If <code>child</code> is <code>null</code>
     */
    public FileAsset(File parent, String child, byte[] data) {
        super(parent, child);
        if (exists()) {
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
        return toURI().toString();
    }

    private void saveData(byte[] data) {
        Path path = Paths.get(toURI());
        try {
            Files.deleteIfExists(path);
            Files.createFile(path);
            ByteBuffer bb = ByteBuffer.wrap(data);
            try (SeekableByteChannel sbc =
                         Files.newByteChannel(Paths.get(toURI()), StandardOpenOption.WRITE)) {
                sbc.write(bb);
            } catch (IOException e) {
                logger.debug("Cannot load resource", e);
            }
        } catch (IOException e) {
            logger.debug("Cannot load resource", e);
        }
    }

    private void loadData() {
        try (SeekableByteChannel sbc = Files.newByteChannel(Paths.get(toURI()), StandardOpenOption.READ)) {
            data = ByteBuffer.allocate((int) sbc.size());
            sbc.read(data);
        } catch (IOException e) {
            logger.debug("Cannot load resource", e);
        }
    }
}
