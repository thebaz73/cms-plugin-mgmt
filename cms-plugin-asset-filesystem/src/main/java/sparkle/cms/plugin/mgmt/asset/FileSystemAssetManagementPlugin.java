package sparkle.cms.plugin.mgmt.asset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import sparkle.cms.domain.CmsAsset;
import sparkle.cms.domain.CmsSetting;
import sparkle.cms.domain.SettingType;
import sparkle.cms.plugin.mgmt.PluginOperationException;
import sparkle.cms.plugin.mgmt.PluginStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import static sparkle.cms.plugin.mgmt.asset.AssetUtils.findAssetTypeByFileName;

/**
 * FileSystemAssetManagementPlugin
 * Created by bazzoni on 07/05/2015.
 */
@Component
public class FileSystemAssetManagementPlugin extends AbstractAssetManagementPlugin<FileContainer, FileAsset> {
    private Path baseFolder;

    @Value("classpath:/META-INF/filesystem-plugin.properties")
    private Resource resource;

    /**
     * Get spring initialized resource
     *
     * @return resource
     */
    @Override
    public Resource getResource() {
        return resource;
    }

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
     * @param path   internal path
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
     * @param siteId      site id
     * @param path        internal path
     * @param name        asset name
     * @param data        asset data
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
     * Initialize plugin settings
     *
     * @throws PluginOperationException if error
     */
    @Override
    protected void createSettings() throws PluginOperationException {
        settings.add(new CmsSetting(getCompoundKey("activate"), getSetting("activate", Boolean.class, false), SettingType.BOOL));
        settings.add(new CmsSetting(getCompoundKey("base.folder.path"), getSetting("base.folder.path", String.class, properties.getProperty("plugin.base.folder.path")), SettingType.TEXT));
    }

    /**
     * Validates plugin
     *
     * @throws PluginOperationException if error
     */
    @Override
    protected void doValidate() throws PluginOperationException {
        String folderName = getSetting("base.folder.path", String.class, properties.getProperty("plugin.base.folder.path"));
        if (!folderName.isEmpty() && !folderName.equals("<change me>")) {
            try {
                String folder = new File(folderName).getCanonicalPath();
                baseFolder = Paths.get(folder);
                if (Files.notExists(baseFolder)) {
                    throw new PluginOperationException(String.format("Invalid base path: %s", baseFolder));
                }
                status = PluginStatus.ACTIVE;
            } catch (IOException e) {
                //ignore
            }
        }
    }

    @Override
    protected void loadChildren(String siteId, FileContainer siteRepository) throws PluginOperationException {
        final File site = Paths.get(siteRepository.toString()).toFile();
        final File[] files = site.listFiles();
        if (files != null) {
            for (File file : files) {
                CmsAsset cmsAsset = new CmsAsset(siteId, file.getName(), new Date(), file.getName(), String.format("%s/%s", siteId, file.getName()));
                cmsAsset.setType(findAssetTypeByFileName(file.getName()));

                cmsAssetRepository.save(cmsAsset);
            }
        }
    }
}
