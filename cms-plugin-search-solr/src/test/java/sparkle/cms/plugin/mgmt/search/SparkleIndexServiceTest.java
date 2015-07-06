package sparkle.cms.plugin.mgmt.search;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import sparkle.cms.data.CmsContentRepository;
import sparkle.cms.data.CmsRoleRepository;
import sparkle.cms.data.CmsSiteRepository;
import sparkle.cms.data.CmsUserRepository;
import sparkle.cms.domain.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * SparkleIndexServiceTest
 * Created by bazzoni on 28/05/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SparkleIndexServiceTest.class, MongoConfig.class, CmsSolrIntegrationConfig.class})
@ComponentScan
@SpringBootApplication
public class SparkleIndexServiceTest {
    @Autowired
    private CmsSiteRepository cmsSiteRepository;
    @Autowired
    private CmsUserRepository cmsUserRepository;
    @Autowired
    private CmsRoleRepository cmsRoleRepository;
    @Autowired
    private CmsContentRepository cmsContentRepository;

    private CmsSite cmsSite;

    @Before
    public void setUp() throws Exception {
        cmsRoleRepository.deleteAll();
        cmsUserRepository.deleteAll();
        cmsSiteRepository.deleteAll();
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
    }

    @Test
    public void testSolrIntegration() {
//        List<CmsContent> contentList = new ArrayList<>();
//        contentList.add(new CmsContent(cmsSite.getId(), "Test01", "Test01", "/test01", new Date(), randomAlphanumeric(20), randomAlphabetic(200)));
//        contentList.add(new CmsContent(cmsSite.getId(), "Test02", "Test03", "/test02", new Date(), randomAlphanumeric(20), randomAlphabetic(200)));
//        contentList.add(new CmsContent(cmsSite.getId(), "Test03", "Test03", "/test03", new Date(), randomAlphanumeric(20), randomAlphabetic(200)));
//        contentList.add(new CmsContent(cmsSite.getId(), "Test04", "Test04", "/test04", new Date(), randomAlphanumeric(20), randomAlphabetic(200)));
//
//        List<CmsContent> savedContentList = new ArrayList<>();
//        for (CmsContent cmsContent : contentList) {
//            savedContentList.add(cmsContentRepository.save(cmsContent));
//        }
//
//        for (CmsContent cmsContent : savedContentList) {
//            sparkleIndexService.addToIndex(cmsContent);
//        }
//
//        final List<SolrSparkleDocument> foundDocuments = sparkleIndexService.search("test");
//
//        foundDocuments.forEach(foundDocument -> {
//            System.out.println(foundDocument.toString());
//        });
    }
}