package ms.cms.plugin.mgmt;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import junit.framework.TestCase;
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

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * AssetManagementPlugInTest
 * Created by bazzoni on 06/05/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackages = "ms.cms")
@ComponentScan(basePackages = "ms.cms")
@ContextConfiguration(classes = {AssetManagementPlugInTest.class})
public class AssetManagementPlugInTest extends AbstractMongoConfiguration {
    @Autowired
    private AssetManagementPlugin plugin;

    private Map<String, Object> repository;

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
        plugin.doActivate();
        repository = ((DummyAssetManagementPlugin) plugin).getRepository();
    }

    @Test
    public void testStatus() throws Exception {
        assertFalse(plugin.getId().isEmpty());
        assertEquals("1.0", plugin.getVERSION());
        assertEquals("Dummy Plugin", plugin.getName());
        assertEquals(PluginStatus.ACTIVE, plugin.getStatus());
    }

    @Test
    public void testCreateSiteRepository() throws Exception {
        String path = plugin.createSiteRepository(UUID.randomUUID().toString());

        assertEquals(HashMap.class, plugin.findSiteRepository(path).getClass());
    }

    @Test
    public void testDeleteSiteRepository() throws Exception {

    }

    @Test
    public void testCreateFolder() throws Exception {

    }

    @Test
    public void testDeleteFolder() throws Exception {

    }

    @Test
    public void testCreateAsset() throws Exception {

    }

    @Test
    public void testDeleteAsset() throws Exception {

    }
}