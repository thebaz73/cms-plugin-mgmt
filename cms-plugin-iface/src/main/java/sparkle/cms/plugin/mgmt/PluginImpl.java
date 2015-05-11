package sparkle.cms.plugin.mgmt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import sparkle.cms.data.CmsSettingRepository;
import sparkle.cms.domain.CmsSetting;

import javax.annotation.PostConstruct;
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
    @Autowired
    private CmsSettingRepository cmsSettingRepository;

    public PluginImpl() {
        settings = new ArrayList<>();
    }

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
     * Get spring initialized resource
     *
     * @return resource
     */
    public abstract Resource getResource();

    /**
     * Activates plugin
     *
     * @throws PluginOperationException if error
     */
    @Override
    public void doActivate() throws PluginOperationException {
        status = PluginStatus.INSTALLED;
        if (getSetting("activate", Boolean.class, Boolean.parseBoolean(properties.getProperty("plugin.activate")))) {
            status = PluginStatus.NOT_READY;
            doValidate();
        }
    }

    /**
     * Initialize plugin settings
     *
     * @throws PluginOperationException if error
     */
    @PostConstruct
    protected void initialize() throws PluginOperationException {
        try {
            properties = PropertiesLoaderUtils.loadProperties(getResource());
            if (!properties.containsKey("plugin.id")) {
                id = UUID.randomUUID().toString();
            } else {
                id = properties.getProperty("plugin.id");
            }
            name = properties.getProperty("plugin.name");
        } catch (IOException e) {
            logger.error("Cannot load properties", e);
        }
        createSettings();
    }

    /**
     * Initialize plugin settings
     *
     * @throws PluginOperationException if error
     */
    protected abstract void createSettings() throws PluginOperationException;

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
