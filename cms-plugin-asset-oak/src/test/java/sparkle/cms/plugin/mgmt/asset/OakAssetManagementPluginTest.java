package sparkle.cms.plugin.mgmt.asset;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.document.DocumentMK;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import sparkle.cms.data.CmsSettingRepository;
import sparkle.cms.domain.CmsSetting;
import sparkle.cms.domain.SettingType;
import sparkle.cms.plugin.mgmt.PluginOperationException;
import sparkle.cms.plugin.mgmt.PluginStatus;

import javax.jcr.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

/**
 * OakAssetManagementPluginTest
 * Created by bazzoni on 17/06/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackages = "sparkle.cms")
@ComponentScan(basePackages = "sparkle.cms")
@ContextConfiguration(classes = {OakAssetManagementPluginTest.class})
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class})
public class OakAssetManagementPluginTest extends AbstractMongoConfiguration {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String siteId = "site";

    @Autowired
    private AssetManagementPlugin<? extends Container, ? extends Asset> plugin;
    @Autowired
    private CmsSettingRepository cmsSettingRepository;
    private DocumentNodeStore documentNodeStore;
    private Session session;

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
        cmsSettingRepository.save(new CmsSetting("oak.activate", true, SettingType.BOOL));
        cmsSettingRepository.save(new CmsSetting("oak.dbName", "cms-test-asset", SettingType.TEXT));
        cmsSettingRepository.save(new CmsSetting("oak.username", "admin", SettingType.TEXT));
        cmsSettingRepository.save(new CmsSetting("oak.password", "admin", SettingType.TEXT));

        session = openSession();
        try {
            final Node root = session.getRootNode();
            if (root.hasNode(siteId)) {
                Node siteNode = root.getNode(siteId);
                siteNode.remove();
                session.save();
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        closeSession();
        plugin.doActivate();
    }

    @After
    public void tearDown() throws Exception {
        plugin.doDeactivate();
        plugin.doExecuteShutdownTasks();
        closeSession();
    }

    @Test
    public void testRepository() throws Exception {
        assertFalse(plugin.getId().isEmpty());
        assertEquals("1.0", plugin.getVERSION());
        assertEquals("Oak Asset Management Plugin", plugin.getName());
        assertEquals(PluginStatus.ACTIVE, plugin.getStatus());

        String site = plugin.createSiteRepository(siteId);
        String folder = plugin.createFolder(siteId, "folder");
        ByteArrayOutputStream baos = readDataFromClasspath();
        String asset = plugin.createAsset(siteId, "folder", "img.png", baos.toByteArray(), "image/png");


//        final Node rootNode = session.getRootNode();
//        assertTrue(rootNode.hasNode(site));
//        Node siteNode = rootNode.getNode(site);
//        assertNotNull(siteNode);
//        assertEquals(siteNode.getName(), siteId);
//        assertTrue(siteNode.hasNode(folder));
//        Node folderNode = siteNode.getNode(folder);
//        assertNotNull(folderNode);
//        assertEquals(folderNode.getName(), folder);
//        assertTrue(folderNode.hasNode(asset));
//        Node assetNode = folderNode.getNode(asset);
//        assertNotNull(assetNode);
//        assertEquals(assetNode.getName(), asset);
//        final ByteArrayOutputStream content = new ByteArrayOutputStream();
//        JcrUtils.readFile(assetNode, content);
//        assertArrayEquals(content.toByteArray(), baos.toByteArray());


        Container siteContainer = plugin.findSiteRepository(siteId);
        assertNotNull(siteContainer);
        assertEquals(siteId, siteContainer.toString());

        Container folderContainer = plugin.findFolder(siteId, "folder");
        assertNotNull(folderContainer);
        assertEquals("folder", folderContainer.toString());

        Asset imageAsset = plugin.findAsset(siteId, "folder", "img.png");
        assertNotNull(imageAsset);
        assertArrayEquals(imageAsset.getContent(), baos.toByteArray());

        plugin.deleteSiteRepository(siteId);
        imageAsset = null;
        try {
            imageAsset = plugin.findAsset(siteId, "folder", "img.png");
        } catch (PluginOperationException e) {
            assertNotNull(e);
        }
        assertNull(imageAsset);
    }

    private Session openSession() throws RepositoryException, UnknownHostException {
        documentNodeStore = new DocumentMK.Builder().
                setMongoDB(mongo().getDB("cms-test-asset")).getNodeStore();
        Repository repository = new Jcr(new Oak(documentNodeStore)).createRepository();
        return repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
    }

    private void closeSession() throws RepositoryException {
        session.logout();
        documentNodeStore.dispose();
    }

    private ByteArrayOutputStream readDataFromClasspath() throws IOException {
        InputStream inputStream = (getClass().getResourceAsStream("/logo_java.png"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        while (inputStream.read(b) != -1) {
            baos.write(b);
        }
        return baos;
    }
}