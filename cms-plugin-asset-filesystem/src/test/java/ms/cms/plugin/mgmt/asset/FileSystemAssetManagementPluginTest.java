package ms.cms.plugin.mgmt.asset;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import ms.cms.data.CmsSettingRepository;
import ms.cms.domain.CmsSetting;
import ms.cms.plugin.mgmt.PluginStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * FileSystemAssetManagementPluginTest
 * Created by bazzoni on 07/05/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackages = "ms.cms")
@ComponentScan(basePackages = "ms.cms")
@ContextConfiguration(classes = {FileSystemAssetManagementPluginTest.class})
public class FileSystemAssetManagementPluginTest extends AbstractMongoConfiguration {
    private final String baseFolder = System.getProperty("java.io.tmpdir");
    private final String siteId = UUID.randomUUID().toString();

    @Autowired
    private AssetManagementPlugin plugin;

    @Autowired
    private CmsSettingRepository cmsSettingRepository;

    public String getDatabaseName() {
        return "cms-test";
    }

    @Bean
    public Mongo mongo() throws UnknownHostException {
        MongoClient client = new MongoClient("192.168.108.129");
        client.setWriteConcern(WriteConcern.SAFE);
        return client;
    }

    @Bean
    public MongoTemplate mongoTemplate() throws UnknownHostException {
        return new MongoTemplate(mongo(), getDatabaseName());
    }

    @Before
    public void setUp() throws Exception {
        cmsSettingRepository.deleteAll();
        cmsSettingRepository.save(new CmsSetting("filesystem.activate", true));
        cmsSettingRepository.save(new CmsSetting("filesystem.base.folder.path", baseFolder));
        plugin.doActivate();
    }

    @Test
    public void testStatus() throws Exception {
        assertFalse(plugin.getId().isEmpty());
        assertEquals("1.0", plugin.getVERSION());
        assertEquals("FileSystem Asset Management Plugin", plugin.getName());
        assertEquals(PluginStatus.ACTIVE, plugin.getStatus());
        assertTrue(Paths.get(baseFolder).toFile().exists());
    }

    @Test
    public void testCreateSiteRepository() throws Exception {
        String repository = plugin.createSiteRepository(siteId);
        File file = Paths.get(baseFolder, siteId).toFile();
        assertEquals(repository, file.getName());
        assertTrue(file.exists());
    }

    @Test
    public void testDeleteSiteRepository() throws Exception {
        plugin.createSiteRepository(siteId);
        File file = Paths.get(baseFolder, siteId).toFile();
        assertTrue(file.exists());
        plugin.deleteSiteRepository(siteId);
        assertFalse(file.exists());
    }

    @Test
    public void testCreateFolder() throws Exception {
        String repository = plugin.createFolder(siteId, "folder1");
        File folder1 = Paths.get(baseFolder, siteId, "folder1").toFile();
        assertEquals(repository, folder1.getName());
        assertTrue(folder1.exists());

        repository = plugin.createFolder(siteId, "folder1/folder2");
        File folder2 = Paths.get(baseFolder, siteId, "folder1/folder2").toFile();
        assertEquals(repository, folder2.getName());
        assertTrue(folder2.exists());
    }

    @Test
    public void testDeleteFolder() throws Exception {
        plugin.createFolder(siteId, "folder1/folder2");

        File folder1 = Paths.get(baseFolder, siteId, "folder1").toFile();
        assertTrue(folder1.exists());

        File folder2 = Paths.get(baseFolder, siteId, "folder1/folder2").toFile();
        assertTrue(folder2.exists());

        plugin.deleteFolder(siteId, "folder2");
        assertTrue(folder2.exists());
        assertTrue(folder1.exists());

        plugin.deleteFolder(siteId, "folder1/folder2");
        assertFalse(folder2.exists());
        assertTrue(folder1.exists());

        plugin.deleteFolder(siteId, "folder1");
        assertFalse(folder2.exists());
        assertFalse(folder1.exists());

        plugin.createFolder(siteId, "folder1/folder2");
        plugin.deleteFolder(siteId, "folder1");
        assertFalse(folder2.exists());
        assertFalse(folder1.exists());
    }

    @Test
    public void testCreateAsset() throws Exception {
        ByteArrayOutputStream baos = readDataFromClasspath();
        Files.createFile(Paths.get(baseFolder, "me.jpg"));
        plugin.createAsset(siteId, "folder1/folder2", "img.jpg", baos.toByteArray());
        byte[] created_data = Files.readAllBytes(Paths.get(baseFolder, siteId, "folder1/folder2/img.jpg"));
        assertArrayEquals(created_data, baos.toByteArray());
    }

    @Test
    public void testDeleteAsset() throws Exception {
        ByteArrayOutputStream baos = readDataFromClasspath();
        Files.createFile(Paths.get(baseFolder, "me.jpg"));
        plugin.createAsset(siteId, "folder1/folder2", "img.jpg", baos.toByteArray());
        assertTrue(Paths.get(baseFolder, siteId, "folder1/folder2/img.jpg").toFile().exists());
        plugin.deleteAsset(siteId, "folder1/folder2", "img.jpg");
        assertFalse(Paths.get(baseFolder, siteId, "folder1/folder2/img.jpg").toFile().exists());
    }

    @Test
    public void testFindSiteRepository() throws Exception {
        Files.createDirectories(Paths.get(baseFolder, siteId));
        Container repository = plugin.findSiteRepository(siteId);
        assertEquals(FileContainer.class, repository.getClass());
        assertTrue(((FileContainer) repository).exists());
    }

    @Test
    public void testFindFolder() throws Exception {
        String path = "folder1/folder2";
        Files.createDirectories(Paths.get(baseFolder, siteId, path));
        Container repository = plugin.findFolder(siteId, path);
        assertEquals(FileContainer.class, repository.getClass());
        assertTrue(((FileContainer) repository).exists());
    }

    @Test
    public void testFindAsset() throws Exception {
        String path = "folder1/folder2";
        String name = "me.jpg";

        Files.createDirectories(Paths.get(baseFolder, siteId, path));
        ByteArrayOutputStream baos = readDataFromClasspath();
        Path file = Files.createFile(Paths.get(baseFolder, siteId, path, name));
        Files.write(file, baos.toByteArray());

        Asset asset = plugin.findAsset(siteId, path, name);
        assertEquals(FileAsset.class, asset.getClass());
        assertTrue(((FileAsset) asset).exists());
        assertArrayEquals(asset.getContent(), baos.toByteArray());
    }

    private ByteArrayOutputStream readDataFromClasspath() throws IOException {
        InputStream inputStream = (getClass().getResourceAsStream("/me.jpg"));
        Files.deleteIfExists(Paths.get(baseFolder, "me.jpg"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        while (inputStream.read(b) != -1) {
            baos.write(b);
        }
        return baos;
    }
}