package sparkle.cms.plugin.mgmt;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
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
import sparkle.cms.data.CmsSettingRepository;
import sparkle.cms.domain.CmsSetting;
import sparkle.cms.plugin.mgmt.asset.*;

import java.net.UnknownHostException;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * AssetManagementPlugInTest
 * Created by bazzoni on 06/05/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackages = "sparkle.cms")
@ComponentScan(basePackages = "sparkle.cms")
@ContextConfiguration(classes = {AssetManagementPlugInTest.class})
public class AssetManagementPlugInTest extends AbstractMongoConfiguration {
    @Autowired
    private AssetManagementPlugin plugin;

    @Autowired
    private CmsSettingRepository cmsSettingRepository;

    private Container repository;

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
        cmsSettingRepository.save(new CmsSetting("dummy.activate", true));
        plugin.doActivate();
        repository = ((DummyAssetManagementPlugin) plugin).getRepository();
    }

    @Test
    public void testStatus() throws Exception {
        assertFalse(repository.hasChildren());
        assertFalse(plugin.getId().isEmpty());
        assertEquals("1.0", plugin.getVERSION());
        assertEquals("Dummy Plugin", plugin.getName());
        assertEquals(PluginStatus.ACTIVE, plugin.getStatus());
    }

    @Test
    public void testCreateSiteRepository() throws Exception {
        String siteId = plugin.createSiteRepository(UUID.randomUUID().toString());

        Container siteRepository = plugin.findSiteRepository(siteId);
        assertNotNull(siteRepository);
        assertEquals(DummyContainer.class, siteRepository.getClass());
    }

    @Test
    public void testDeleteSiteRepository() throws Exception {
        String siteId = plugin.createSiteRepository(UUID.randomUUID().toString());
        plugin.deleteSiteRepository(siteId);

        assertNull(plugin.findSiteRepository(siteId));
    }

    @Test
    public void testCreateFolder() throws Exception {
        String siteId = plugin.createSiteRepository(UUID.randomUUID().toString());
        String path = plugin.createFolder(siteId, "folder");

        Container siteRepository = plugin.findSiteRepository(siteId);
        assertTrue(siteRepository.hasChildren());

        Object folder = plugin.findFolder(siteId, path);
        assertNotNull(folder);
        assertEquals(DummyContainer.class, folder.getClass());
    }

    @Test
    public void testDeleteFolder() throws Exception {
        String siteId = plugin.createSiteRepository(UUID.randomUUID().toString());
        String path = plugin.createFolder(siteId, "folder");
        plugin.deleteFolder(siteId, path);

        assertNull(plugin.findFolder(siteId, path));
    }

    @Test
    public void testCreateAsset() throws Exception {
        String siteId = plugin.createSiteRepository(UUID.randomUUID().toString());
        String path = plugin.createFolder(siteId, "folder");
        String filename = plugin.createAsset(siteId, path, "filename", "data".getBytes(), "text/plain");

        Container folder = plugin.findFolder(siteId, path);
        assertTrue(folder.hasChildren());

        Asset asset = plugin.findAsset(siteId, path, filename);
        assertNotNull(asset);
        assertEquals(DummyAsset.class, asset.getClass());
        assertEquals("filename", asset.getUri());
        assertEquals("data", new String(asset.getContent()));
    }

    @Test
    public void testDeleteAsset() throws Exception {
        String siteId = plugin.createSiteRepository(UUID.randomUUID().toString());
        String path = plugin.createFolder(siteId, "folder");
        String filename = plugin.createAsset(siteId, path, "filename", "data".getBytes(), "text/plain");
        plugin.deleteAsset(siteId, path, filename);

        assertNull(plugin.findAsset(siteId, path, filename));
    }
}