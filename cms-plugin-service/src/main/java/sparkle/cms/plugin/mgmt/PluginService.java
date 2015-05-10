package sparkle.cms.plugin.mgmt;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import sparkle.cms.plugin.mgmt.asset.AssetManagementPlugin;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * PluginService
 * Created by bazzoni on 09/05/2015.
 */
@Component
public class PluginService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ApplicationContext applicationContext;
    private AssetManagementPlugin assetManagementPlugin;
    private Map<String, Plugin> pluginMap;

    public AssetManagementPlugin getAssetManagementPlugin() {
        return assetManagementPlugin;
    }

    @PostConstruct
    private void initialize() {
        pluginMap = applicationContext.getBeansOfType(Plugin.class);
        reloadPlugins();
    }

    public void reloadPlugins() {
        for (Map.Entry<String, Plugin> entry : pluginMap.entrySet()) {
            logger.debug("Processing bean {}", entry.getKey());
            Plugin plugin = entry.getValue();
            try {
                plugin.doActivate();
                if (plugin.getStatus().equals(PluginStatus.ACTIVE)) {
                    if (plugin.getClass().isAssignableFrom(AssetManagementPlugin.class)) {
                        assetManagementPlugin = (AssetManagementPlugin) plugin;
                    }
                }
            } catch (PluginOperationException e) {
                logger.error("Unable to activate {}: cause {}", plugin.getName(), e.getMessage());
            }
        }
    }
}
