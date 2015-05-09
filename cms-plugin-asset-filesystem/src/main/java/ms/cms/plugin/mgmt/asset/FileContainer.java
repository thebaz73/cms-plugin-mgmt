package ms.cms.plugin.mgmt.asset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * FileContainer
 * Created by bazzoni on 07/05/2015.
 */
public class FileContainer implements Container {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Path path;

    public FileContainer(Path path) {
        this.path = path;
        loadData();
    }

    /**
     * Checks if container as children
     *
     * @return true is children number &gt; 0
     */
    @Override
    public boolean hasChildren() {
        return path.toFile().list().length > 0;
    }

    private void loadData() {
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                logger.debug("Creating folder: " + path.toString());
            } catch (IOException e) {
                logger.debug("Creating folder: " + path.toString(), e);
            }
        }
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
