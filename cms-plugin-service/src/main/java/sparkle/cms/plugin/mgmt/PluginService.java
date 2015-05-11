package sparkle.cms.plugin.mgmt;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import sparkle.cms.data.CmsSettingRepository;
import sparkle.cms.data.CmsUserRepository;
import sparkle.cms.domain.CmsSetting;
import sparkle.cms.domain.CmsUser;
import sparkle.cms.domain.Role;
import sparkle.cms.plugin.mgmt.asset.AssetManagementPlugin;

import javax.annotation.PostConstruct;
import java.util.List;
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
    @Autowired
    private CmsUserRepository cmsUserRepository;
    @Autowired
    private CmsSettingRepository cmsSettingRepository;
    private AssetManagementPlugin assetManagementPlugin;
    private Map<String, Plugin> pluginMap;
    private boolean initialized = false;

    public AssetManagementPlugin getAssetManagementPlugin() {
        return assetManagementPlugin;
    }

    @PostConstruct
    private void initialize() {
        pluginMap = applicationContext.getBeansOfType(Plugin.class);
        reloadPlugins(false);
    }

    private void setDefaultSettings() {
        for (Map.Entry<String, Plugin> entry : pluginMap.entrySet()) {
            logger.debug("Handling settings for bean {}", entry.getKey());
            Plugin plugin = entry.getValue();
            final List<CmsUser> cmsUsers = cmsUserRepository.findAll();
            for (CmsUser cmsUser : cmsUsers) {
                if (cmsUser.getRoles().stream().anyMatch(r -> r.getRole().equals(Role.ROLE_MANAGER))) {
                    plugin.setFilter(cmsUser.getId());
                    for (CmsSetting cmsSetting : plugin.getSettings()) {
                        cmsSetting.setFilter(cmsUser.getId());
                        if (cmsSettingRepository.findByKeyAndFilter(cmsSetting.getKey(), cmsSetting.getFilter()).isEmpty()) {
                            cmsSettingRepository.save(cmsSetting);
                        }
                    }
                }
            }
        }
        initialized = true;
    }

    public void reloadPlugins(boolean force) {
        if (!initialized || force) {
            setDefaultSettings();
        }
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
