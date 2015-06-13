package sparkle.cms.plugin.mgmt.asset;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.RepositoryFactoryImpl;
import org.junit.After;
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
import sparkle.cms.domain.SettingType;
import sparkle.cms.plugin.mgmt.PluginOperationException;
import sparkle.cms.plugin.mgmt.PluginStatus;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * JackrabbitAssetManagementPluginTest
 * Created by bazzoni on 11/06/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackages = "sparkle.cms")
@ComponentScan(basePackages = "sparkle.cms")
@ContextConfiguration(classes = {JackrabbitAssetManagementPluginTest.class})
public class JackrabbitAssetManagementPluginTest extends AbstractMongoConfiguration {
    private final String siteId = "site";

    @Autowired
    private AssetManagementPlugin<? extends Container, ? extends Asset> plugin;
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
        cmsSettingRepository.save(new CmsSetting("jackrabbit.activate", true, SettingType.BOOL));
        cmsSettingRepository.save(new CmsSetting("jackrabbit.repositoryURL", "file:///temp", SettingType.TEXT));
        cmsSettingRepository.save(new CmsSetting("jackrabbit.username", "admin", SettingType.TEXT));
        cmsSettingRepository.save(new CmsSetting("jackrabbit.password", "admin", SettingType.TEXT));

        final Session session = getSession();
        for (Node node : JcrUtils.getChildNodes(session.getRootNode())) {
            if (node.isNodeType(NodeType.NT_FOLDER)) {
                node.remove();
            }
        }
        session.save();
        session.logout();
    }

    @After
    public void tearDown() throws Exception {
        if (PluginStatus.ACTIVE.equals(plugin.getStatus())) {
            plugin.doDeactivate();
            plugin.doExecuteShutdownTasks();
        }
    }

    @Test
    public void testStatus() throws Exception {
        plugin.doActivate();
        assertFalse(plugin.getId().isEmpty());
        assertEquals("1.0", plugin.getVERSION());
        assertEquals("Jackrabbit Asset Management Plugin", plugin.getName());
        assertEquals(PluginStatus.ACTIVE, plugin.getStatus());
        plugin.doDeactivate();
        plugin.doExecuteShutdownTasks();
    }

    @Test
    public void testCreate() throws Exception {
        plugin.doActivate();
        String site = plugin.createSiteRepository(siteId);
        String folder = plugin.createFolder(siteId, "folder");
        ByteArrayOutputStream baos = readDataFromClasspath();
        String asset = plugin.createAsset(siteId, "folder", "img.png", baos.toByteArray(), "image/png");
        plugin.doDeactivate();
        plugin.doExecuteShutdownTasks();

        final Session session = getSession();

        Node siteNode = session.getRootNode().getNode(site);
        assertNotNull(siteNode);
        assertEquals(siteNode.getName(), siteId);
        Node folderNode = siteNode.getNode(folder);
        assertNotNull(folderNode);
        assertEquals(folderNode.getName(), folder);
        Node assetNode = folderNode.getNode(asset);
        assertNotNull(assetNode);
        assertEquals(assetNode.getName(), asset);
        final ByteArrayOutputStream content = new ByteArrayOutputStream();
        JcrUtils.readFile(assetNode, content);
        assertArrayEquals(content.toByteArray(), baos.toByteArray());

        session.logout();
    }

    @Test
    public void testDelete() throws Exception {
        final Session session = getSession();

        final Node siteNode = JcrUtils.getOrAddFolder(session.getRootNode(), siteId);
        final Node folderNode = JcrUtils.getOrAddFolder(siteNode, "folder");
        ByteArrayOutputStream baos = readDataFromClasspath();
        JcrUtils.putFile(folderNode, "img.png", "image/png", new ByteArrayInputStream(baos.toByteArray()));

        session.save();
        session.logout();

        plugin.doActivate();

        Asset asset;
        asset = plugin.findAsset(siteId, "folder", "img.png");
        assertNotNull(asset);
        plugin.deleteSiteRepository(siteId);
        asset = null;
        try {
            asset = plugin.findAsset(siteId, "folder", "img.png");
        } catch (PluginOperationException e) {
            assertNotNull(e);
        }
        assertNull(asset);
    }

    @Test
    public void testFind() throws Exception {
        final Session session = getSession();

        final Node siteNode = JcrUtils.getOrAddFolder(session.getRootNode(), siteId);
        final Node folderNode = JcrUtils.getOrAddFolder(siteNode, "folder");
        ByteArrayOutputStream baos = readDataFromClasspath();
        JcrUtils.putFile(folderNode, "img.png", "image/png", new ByteArrayInputStream(baos.toByteArray()));

        session.save();
        session.logout();
        plugin.doActivate();

        final Container site = plugin.findSiteRepository(siteId);
        assertNotNull(site);
        assertEquals(siteId, site.toString());

        final Container folder = plugin.findFolder(siteId, "folder");
        assertNotNull(folder);
        assertEquals("folder", folder.toString());

        Asset asset = plugin.findAsset(siteId, "folder", "img.png");
        assertNotNull(asset);
        assertArrayEquals(asset.getContent(), baos.toByteArray());
    }

    private Session getSession() throws RepositoryException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(JcrUtils.REPOSITORY_URI, "file:///temp");
        RepositoryFactory repositoryFactory = new RepositoryFactoryImpl();
        Repository repository = repositoryFactory.getRepository(parameters);
        return repository.login(new SimpleCredentials("test", "test".toCharArray()));
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