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
    private Path baseFolder;

    /**
     * Creates a base repository container for site
     *
     * @param siteId site id
     * @return asset path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public String createSiteRepository(String siteId) throws PluginOperationException {
        final Path path = Paths.get(baseFolder.toString(), siteId);
        FileContainer container = new FileContainer(path);
        return container.toString();
    }

    /**
     * Deletes a site repository container
     *
     * @param siteId site id
     * @throws PluginOperationException if operation failure
     */
    @Override
    public void deleteSiteRepository(String siteId) throws PluginOperationException {
        final Path path = Paths.get(baseFolder.toString(), siteId);
        FileContainer container = new FileContainer(path);

        if (container.hasChildren()) return;

        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new PluginOperationException(String.format("Cannot delete repository:%s", path));
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
        final Path folder = Paths.get(baseFolder.toString(), siteId, path);
        FileContainer container = new FileContainer(folder);
        return container.toString();
    }

    /**
     * Deletes a folder
     *
     * @param siteId site id
     * @param path internal path
     * @throws PluginOperationException if operation failure
     */
    @Override
    public void deleteFolder(String siteId, String path) throws PluginOperationException {
        final Path folder = Paths.get(baseFolder.toString(), siteId, path);
        FileContainer container = new FileContainer(folder);

        if (container.hasChildren()) return;

        try {
            Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
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
            throw new PluginOperationException(String.format("Cannot delete repository:%s", folder));
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
        final Path folder = Paths.get(baseFolder.toString(), siteId, path);
        FileContainer container = new FileContainer(folder);
        final Path file = Paths.get(container.toString(), name);
        FileAsset asset = new FileAsset(file, data);
        return asset.toString();
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
        final Path folder = Paths.get(baseFolder.toString(), siteId, path);
        FileContainer container = new FileContainer(folder);
        final Path file = Paths.get(container.toString(), name);
        FileAsset asset = new FileAsset(file);
        try {
            Files.delete(file);
        } catch (IOException e) {
            throw new PluginOperationException(String.format("Cannot delete repository:%s", asset.toString()));
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
        return new FileContainer(Paths.get(baseFolder.toString(), siteId));
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
        return new FileContainer(Paths.get(baseFolder.toString(), siteId, path));
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
        return new FileAsset(Paths.get(baseFolder.toString(), siteId, path, name));
    }

    /**
     * Validates plugin
     *
     * @throws PluginOperationException if error
     */
    @Override
    protected void doValidate() throws PluginOperationException {
        String folderName = getSetting("base.folder.path", String.class, properties.getProperty("plugin.base.folder.path"));
        baseFolder = Paths.get(folderName);
        if (Files.notExists(baseFolder)) {
            throw new PluginOperationException(String.format("Cannot create base path: %s", baseFolder));
        }
        status = PluginStatus.ACTIVE;
    }
}
