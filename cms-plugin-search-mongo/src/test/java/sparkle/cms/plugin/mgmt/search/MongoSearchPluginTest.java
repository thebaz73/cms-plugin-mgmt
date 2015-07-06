package sparkle.cms.plugin.mgmt.search;

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
import sparkle.cms.data.*;
import sparkle.cms.domain.*;
import sparkle.cms.plugin.mgmt.PluginStatus;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * MongoSearchPluginTest
 * Created by bazzoni on 01/06/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoSearchPluginTest.class})
@EnableMongoRepositories(basePackages = "sparkle.cms")
@ComponentScan
public class MongoSearchPluginTest extends AbstractMongoConfiguration {
    @Autowired
    private CmsSettingRepository cmsSettingRepository;
    @Autowired
    private CmsSiteRepository cmsSiteRepository;
    @Autowired
    private CmsUserRepository cmsUserRepository;
    @Autowired
    private CmsRoleRepository cmsRoleRepository;
    @Autowired
    private CmsContentRepository cmsContentRepository;

    private CmsSite cmsSite;

    @Autowired
    private MongoSearchPlugin plugin;

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
        cmsRoleRepository.deleteAll();
        cmsUserRepository.deleteAll();
        cmsSiteRepository.deleteAll();
        cmsContentRepository.deleteAll();
        cmsSettingRepository.save(new CmsSetting("mongo.activate", true, SettingType.BOOL));
        cmsSettingRepository.save(new CmsSetting("mongo.database.name", "cms-test", SettingType.TEXT));
        for (Role role : Role.ALL) {
            List<CmsRole> byRole = cmsRoleRepository.findByRole(role.getName());
            if (byRole.isEmpty()) {
                CmsRole cmsRole = new CmsRole(role.getName());
                cmsRoleRepository.save(cmsRole);
            }
        }
        CmsUser cmsUser = new CmsUser("lvoldemort", "avada!kedavra", "voldemort@evil.com", "Tom Riddle", new Date(),
                Arrays.asList(cmsRoleRepository.findByRole("ROLE_USER").get(0),
                        cmsRoleRepository.findByRole("ROLE_MANAGER").get(0)));
        cmsUserRepository.save(cmsUser);

        cmsSite = new CmsSite("evil.com", new Date(), "evil.com", WorkflowType.SELF_APPROVAL_WF, CommentApprovalMode.SELF_APPROVAL, cmsUser);
        cmsSiteRepository.save(cmsSite);

        plugin.doActivate();
    }

    @Test
    public void testStatus() throws Exception {
        assertFalse(plugin.getId().isEmpty());
        assertEquals("1.0", plugin.getVERSION());
        assertEquals("Mongo Search Plugin", plugin.getName());
        assertEquals(PluginStatus.ACTIVE, plugin.getStatus());
    }

    @Test
    public void testSearch() throws Exception {
        List<CmsContent> contentList = new ArrayList<>();
        contentList.add(new CmsContent(cmsSite.getId(), "Test01", "tabc01", "/test01", new Date(), randomAlphabetic(20), randomAlphabetic(200)));
        contentList.add(new CmsContent(cmsSite.getId(), "Test02", "tEbct02", "/test02", new Date(), randomAlphabetic(20), randomAlphabetic(200)));
        contentList.add(new CmsContent(cmsSite.getId(), "Test03", "Test03", "/test03", new Date(), randomAlphabetic(20), randomAlphabetic(200)));
        contentList.add(new CmsContent(cmsSite.getId(), "Test04", "Test04", "/test04", new Date(), randomAlphabetic(20), randomAlphabetic(200)));

        contentList.forEach(cmsContentRepository::save);

        List<MongoSparkleDocument> documentList;

        documentList = plugin.search(cmsSite.getId(), "bc");
        assertEquals(2, documentList.size());
        documentList = plugin.search(cmsSite.getId(), "eb");
        assertEquals(1, documentList.size());
        documentList = plugin.search(cmsSite.getId(), "est");
        assertEquals(2, documentList.size());
        documentList = plugin.search(cmsSite.getId(), "Te");
        assertEquals(3, documentList.size());
    }
}