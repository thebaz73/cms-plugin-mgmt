package ms.cms.plugin.mgmt;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import ms.cms.data.CmsSettingRepository;
import ms.cms.domain.CmsSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * PluginImpl
 * Created by bazzoni on 06/05/2015.
 */
public abstract class PluginImpl implements Plugin {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String VERSION = "1.0";

    @Value("classpath:/META-INF/plugin.properties")
    private Resource resource;

    @Autowired
    private CmsSettingRepository cmsSettingRepository;

    protected String id;
    protected String name;
    protected PluginStatus status;

    protected List<CmsSetting> settings;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PluginStatus getStatus() {
        return status;
    }

    @Override
    public String getVERSION() {
        return VERSION;
    }

    /**
     * Activates plugin
     */
    @Override
    public void doActivate() {
        status = PluginStatus.INSTALLED;
        try {
            Properties properties = PropertiesLoaderUtils.loadProperties(resource);
            if(!properties.contains("plugin.id")) {
                id = UUID.randomUUID().toString();
            }
            else {
                id = properties.getProperty("plugin.id");
            }
            name = properties.getProperty("plugin.name");
            settings = cmsSettingRepository.findByKey(id);
            status = PluginStatus.NOT_READY;
            doValidate();
        } catch (IOException e) {
            logger.error("Cannot load properties", e);
        }
    }

    /**
     * Validates plugin
     */
    protected abstract void doValidate();
}
