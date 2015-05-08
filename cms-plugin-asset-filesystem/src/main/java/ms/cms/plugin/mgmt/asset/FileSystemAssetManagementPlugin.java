package ms.cms.plugin.mgmt.asset;

import ms.cms.plugin.mgmt.PluginOperationException;
import ms.cms.plugin.mgmt.PluginStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * FileSystemAssetManagementPlugin
 * Created by bazzoni on 07/05/2015.
 */
@Component
public class FileSystemAssetManagementPlugin extends AbstractAssetManagementPlugin<FileContainer, FileAsset> {
    private FileContainer baseFolder;

    /**
     * Creates a base repository container for site
     *
     * @param siteId site id
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public String createSiteRepository(String siteId) throws PluginOperationException {
        FileContainer siteRepository = new FileContainer(Paths.get(baseFolder.getAbsolutePath(), siteId).toUri());
        return siteRepository.getName();
    }

    /**
     * Deletes a site repository container
     *
     * @param siteId site id
     * @throws PluginOperationException if operation failure
     */
    @Override
    public void deleteSiteRepository(String siteId) throws PluginOperationException {
        FileContainer siteRepository = new FileContainer(Paths.get(baseFolder.getAbsolutePath(), siteId).toUri());

        if (siteRepository.hasChildren()) return;

        if (!siteRepository.delete()) {
            throw new PluginOperationException(String.format("Cannot delete repository:%s", siteRepository.getAbsolutePath()));
        }
    }

    /**
     * Creates a folder container for site
     *
     * @param siteId site id
     * @param path   internal path
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public String createFolder(String siteId, String path) throws PluginOperationException {
        FileContainer folder = new FileContainer(Paths.get(baseFolder.getAbsolutePath(), siteId, path).toUri());

        return folder.getName();
    }

    /**
     * Deletes a folder
     *
     * @param siteId site id
     * @param nodeId node id
     * @throws PluginOperationException if operation failure
     */
    @Override
    public void deleteFolder(String siteId, String nodeId) throws PluginOperationException {
        FileContainer folder = new FileContainer(Paths.get(baseFolder.getAbsolutePath(), siteId, nodeId).toUri());

        Path directory = Paths.get(folder.toURI());
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new PluginOperationException(String.format("Cannot delete repository:%s", folder.getAbsolutePath()));
        }
    }

    /**
     * Creates an asset for the site and folder
     *
     * @param siteId site id
     * @param path   internal path
     * @param name   asset name
     * @param data   asset data
     * @param contentType content type
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public String createAsset(String siteId, String path, String name, byte[] data, String contentType) throws PluginOperationException {
        FileContainer folder = new FileContainer(Paths.get(baseFolder.getAbsolutePath(), siteId, path).toUri());
        FileAsset asset = new FileAsset(folder, name, data);
        return asset.getName();
    }

    /**
     * Deletes an asset
     *
     * @param siteId site id
     * @param path   internal path
     * @param name   asset name
     * @throws PluginOperationException if operation failure
     */
    @Override
    public void deleteAsset(String siteId, String path, String name) throws PluginOperationException {
        FileContainer folder = new FileContainer(Paths.get(baseFolder.getAbsolutePath(), siteId, path).toUri());
        FileAsset asset = new FileAsset(folder, name, null);
        if (!asset.delete()) {
            throw new PluginOperationException(String.format("Cannot delete repository:%s", asset.getAbsolutePath()));
        }
    }

    /**
     * Find a site Repository
     *
     * @param siteId siteId
     * @return site repository
     */
    @Override
    public FileContainer findSiteRepository(String siteId) {
        return new FileContainer(Paths.get(baseFolder.getAbsolutePath(), siteId).toUri());
    }

    /**
     * Find a folder
     *
     * @param siteId siteId
     * @param path   internal path
     * @return folder container
     */
    @Override
    public FileContainer findFolder(String siteId, String path) {
        return new FileContainer(Paths.get(baseFolder.getAbsolutePath(), siteId, path).toUri());
    }

    /**
     * Find a asset
     *
     * @param siteId siteId
     * @param path   internal path
     * @param name   asset name
     * @return asset
     */
    @Override
    public FileAsset findAsset(String siteId, String path, String name) {
        return new FileAsset(new FileContainer(Paths.get(baseFolder.getAbsolutePath(), siteId, path).toUri()), name, null);
    }

    /**
     * Validates plugin
     *
     * @throws PluginOperationException if error
     */
    @Override
    protected void doValidate() throws PluginOperationException {
        String folderName = getSetting("base.folder.path", String.class, properties.getProperty("plugin.base.folder.path"));
        baseFolder = new FileContainer(Paths.get(folderName).toUri());
        if (!baseFolder.exists()) {
            throw new PluginOperationException(String.format("Cannot create base path: %s", baseFolder.getAbsolutePath()));
        }
        status = PluginStatus.ACTIVE;
    }
}
