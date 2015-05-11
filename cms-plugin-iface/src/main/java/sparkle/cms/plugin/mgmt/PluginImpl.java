package sparkle.cms.plugin.mgmt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import sparkle.cms.data.CmsSettingRepository;
import sparkle.cms.domain.CmsSetting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * PluginImpl
 * Created by bazzoni on 06/05/2015.
 */
public abstract class PluginImpl implements Plugin {
    private static final String VERSION = "1.0";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected String id;
    protected String name;
    protected PluginStatus status;
    protected Properties properties;
    protected List<CmsSetting> settings;
    protected String filter;
    @Value("classpath:/META-INF/plugin.properties")
    private Resource resource;
    @Autowired
    private CmsSettingRepository cmsSettingRepository;

    public PluginImpl() {
        settings = new ArrayList<>();
        try {
            initialize();
        } catch (PluginOperationException e) {
            logger.error("Cannot initialize properties", e);
        }
    }

    /**
     * Initialize plugin settings
     *
     * @throws PluginOperationException if error
     */
    protected abstract void initialize() throws PluginOperationException;

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

    @Override
    public List<CmsSetting> getSettings() {
        return settings;
    }

    @Override
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Activates plugin
     *
     * @throws PluginOperationException if error
     */
    @Override
    public void doActivate() throws PluginOperationException {
        status = PluginStatus.INSTALLED;
        try {
            properties = PropertiesLoaderUtils.loadProperties(resource);
            if (!properties.containsKey("plugin.id")) {
                id = UUID.randomUUID().toString();
            } else {
                id = properties.getProperty("plugin.id");
            }
            name = properties.getProperty("plugin.name");

            if (getSetting("activate", Boolean.class, Boolean.parseBoolean(properties.getProperty("plugin.activate")))) {
                status = PluginStatus.NOT_READY;
                doValidate();
            }
        } catch (IOException e) {
            logger.error("Cannot load properties", e);
        }
    }

    /**
     * Validates plugin
     *
     * @throws PluginOperationException if error
     */
    protected abstract void doValidate() throws PluginOperationException;

    /**
     * Get a setting according the type chosen
     *
     * @param key   setting key
     * @param clazz class parameter
     * @param <T>   type
     * @return setting value
     * @throws PluginOperationException
     */
    protected <T> T getSetting(String key, Class<T> clazz, T defaultValue) throws PluginOperationException {
        String compoundKey = getCompoundKey(key);
        List<CmsSetting> settings = cmsSettingRepository.findByKeyAndFilter(compoundKey, filter);
        if (!settings.isEmpty() && settings.get(0).getKey().equals(compoundKey)) {
            return clazz.cast(settings.get(0).getValue());
        }
        if (defaultValue != null) {
            return defaultValue;
        }

        throw new PluginOperationException("Setting not found");
    }

    protected String getCompoundKey(String key) {
        return String.format("%s.%s", id, key);
    }
}
