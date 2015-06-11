package sparkle.cms.plugin.mgmt.asset;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.core.RepositoryFactoryImpl;
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

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    private Repository repository;

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
        Map<String, String> parameters = new HashMap<>();
        parameters.put(JcrUtils.REPOSITORY_URI, "file:///temp");
        RepositoryFactory repositoryFactory = new RepositoryFactoryImpl();
        this.repository = repositoryFactory.getRepository(parameters);
        final Session session = repository.login();

        for (Node node : JcrUtils.getChildNodes(session.getRootNode())) {
            if (node.isNodeType(NodeType.NT_FOLDER)) {
                node.remove();
            }
        }
        session.logout();

        plugin.doActivate();
    }

//    @Test
//    public void testStatus() throws Exception {
//        assertFalse(plugin.getId().isEmpty());
//        assertEquals("1.0", plugin.getVERSION());
//        assertEquals("Jackrabbit Asset Management Plugin", plugin.getName());
//        assertEquals(PluginStatus.ACTIVE, plugin.getStatus());
//    }

    @Test
    public void testCreateSiteRepository() throws Exception {
        String site = plugin.createSiteRepository(siteId);
        final Session session = repository.login("test");
        final Node node = session.getNode(siteId);
        assertNotNull(node);
        assertEquals(node.getName(), site);
        session.save();
        session.logout();
    }

//    @Test
//    public void testDeleteSiteRepository() throws Exception {
//        repository.findOrCreateObject(siteId);
//        assertTrue(repository.exists(siteId));
//        plugin.deleteSiteRepository(siteId);
//        assertFalse(repository.exists(siteId));
//    }
//
//    @Test
//    public void testCreateFolder() throws Exception {
//        String folder1 = plugin.createFolder(siteId, "folder1");
//        assertTrue(repository.exists(siteId + "/" + "folder1"));
//        final FedoraObject fedoraObject = repository.getObject(siteId + "/" + "folder1");
//        assertEquals(fedoraObject.getName(), folder1);
//    }
//
//    @Test
//    public void testDeleteFolder() throws Exception {
//        repository.findOrCreateObject(siteId);
//        repository.findOrCreateObject(siteId + "/folder1");
//        plugin.deleteFolder(siteId, "folder1");
//        assertFalse(repository.exists(siteId + "/" + "folder1"));
//    }
//
//    @Test
//    public void testCreateAsset() throws Exception {
//        ByteArrayOutputStream baos = readDataFromClasspath();
//        plugin.createAsset(siteId, "folder1", "img.png", baos.toByteArray(), "image/png");
//        assertTrue(repository.exists(siteId + "/folder1/img.png"));
//        final FedoraDatastreamImpl fedoraDatastream = (FedoraDatastreamImpl) repository.findOrCreateDatastream(siteId + "/folder1/img.png");
//        assertEquals("http://192.168.108.129:8080/rest/site/folder1/img.png", new FedoraAsset(fedoraDatastream.getUri()).getUri());
//    }
//
//    @Test
//    public void testDeleteAsset() throws Exception {
//        ByteArrayOutputStream baos = readDataFromClasspath();
//        FedoraContent content = new FedoraContent().setContent(new ByteArrayInputStream(baos.toByteArray()))
//                .setContentType("image/png");
//        repository.findOrCreateObject(siteId);
//        repository.findOrCreateObject(siteId + "/folder1");
//        repository.createDatastream(siteId + "/folder1/img.png", content);
//        plugin.deleteAsset(siteId, "folder1", "img.png");
//        assertFalse(repository.exists(siteId + "/folder1/img.png"));
//    }
//
//    @Test
//    public void testFindSiteRepository() throws Exception {
//        repository.createObject(siteId);
//        Container repository = plugin.findSiteRepository(siteId);
//        assertEquals(FedoraContainer.class, repository.getClass());
//    }
//
//    @Test
//    public void testFindFolder() throws Exception {
//        String path = "folder1";
//        repository.findOrCreateObject(siteId);
//        repository.findOrCreateObject(siteId + "/folder1");
//        Container repository = plugin.findFolder(siteId, path);
//        assertEquals(FedoraContainer.class, repository.getClass());
//    }
//
//    @Test
//    public void testFindAsset() throws Exception {
//        String path = "folder1";
//        String name = "logo_java.png";
//
//        ByteArrayOutputStream baos = readDataFromClasspath();
//        FedoraContent content = new FedoraContent().setContent(new ByteArrayInputStream(baos.toByteArray()))
//                .setContentType("image/png");
//        repository.findOrCreateObject(siteId);
//        repository.findOrCreateObject(siteId + "/" + path);
//        repository.createDatastream(siteId + "/" + path + "/" + name, content);
//
//        Asset asset = plugin.findAsset(siteId, path, name);
//        assertEquals(FedoraAsset.class, asset.getClass());
//        assertEquals("http://192.168.108.129:8080/rest/site/folder1/logo_java.png", asset.getUri());
//    }

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