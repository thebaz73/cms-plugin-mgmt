package sparkle.cms.plugin.mgmt;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
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
import sparkle.cms.data.CmsRoleRepository;
import sparkle.cms.data.CmsSettingRepository;
import sparkle.cms.data.CmsUserRepository;
import sparkle.cms.domain.CmsRole;
import sparkle.cms.domain.CmsSetting;
import sparkle.cms.domain.CmsUser;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * PluginServiceTest
 * Created by bazzoni on 11/05/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@EnableMongoRepositories(basePackages = "sparkle.cms")
@ComponentScan(basePackages = "sparkle.cms")
@ContextConfiguration(classes = {PluginServiceTest.class})
public class PluginServiceTest extends AbstractMongoConfiguration {
    @Autowired
    private CmsUserRepository cmsUserRepository;
    @Autowired
    private CmsRoleRepository cmsRoleRepository;
    @Autowired
    private PluginService pluginService;

    @Autowired
    private CmsSettingRepository cmsSettingRepository;

    private CmsUser cmsUser;

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

        cmsUserRepository.deleteAll();
        List<CmsRole> cmsRoles = new ArrayList<>();
        cmsRoles.add(createCmsRole("ROLE_USER"));
        cmsRoles.add(createCmsRole("ROLE_MANAGER"));
        cmsUser = new CmsUser("John Doe", "john.doe@email.com", "jdoe", "jdoe", new Date(), cmsRoles);
        cmsUserRepository.save(cmsUser);
    }

    @After
    public void tearDown() throws Exception {
        cmsSettingRepository.deleteAll();
    }

    @Test
    public void testGetAssetManagementPlugin() throws Exception {

    }

    @Test
    public void testReloadPlugins() throws Exception {
        List<CmsSetting> all = cmsSettingRepository.findAll();
        assertEquals(0, all.size());
        pluginService.reloadPlugins(true);
        all = cmsSettingRepository.findAll();
        assertEquals(6, all.size());

    }

    private CmsRole createCmsRole(String roleName) {
        CmsRole cmsRole = new CmsRole(roleName);
        cmsRoleRepository.save(cmsRole);

        return cmsRole;
    }
}