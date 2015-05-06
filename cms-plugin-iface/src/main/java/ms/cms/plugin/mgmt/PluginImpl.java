package ms.cms.plugin.mgmt;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * PluginImpl
 * Created by bazzoni on 06/05/2015.
 */
@Component
public abstract class PluginImpl implements Plugin {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String VERSION = "1.0";

    @Value("classpath:/extension.properties")
    private Resource resource;
    private Properties properties;
    private String id;
    private String name;
    private PluginStatus status;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    @PostConstruct
    public void doActivate() {
        status = PluginStatus.INSTALLED;
        try {
            properties = PropertiesLoaderUtils.loadProperties(resource);
            status = PluginStatus.NOT_READY;
            doValidate();
        } catch (IOException e) {
            logger.error("Cannot load properties", e);
        }
    }

}
