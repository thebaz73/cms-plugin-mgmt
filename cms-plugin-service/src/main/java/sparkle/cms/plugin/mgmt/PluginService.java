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
import sparkle.cms.plugin.mgmt.asset.Asset;
import sparkle.cms.plugin.mgmt.asset.AssetManagementPlugin;
import sparkle.cms.plugin.mgmt.asset.Container;
import sparkle.cms.service.AbstractCmsSettingAwareService;

import javax.annotation.PostConstruct;

import java.util.List;
import java.util.Map;

/**
 * PluginService
 * Created by bazzoni on 09/05/2015.
 */
@Component
public class PluginService extends AbstractCmsSettingAwareService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private CmsUserRepository cmsUserRepository;
    @Autowired
    private CmsSettingRepository cmsSettingRepository;
    private AssetManagementPlugin<? extends Container, ? extends Asset> assetManagementPlugin;
    private Map<String, Plugin> pluginMap;

    public AssetManagementPlugin<? extends Container, ? extends Asset> getAssetManagementPlugin() {
        return assetManagementPlugin;
    }

    @PostConstruct
    private void initialize() {
        pluginMap = applicationContext.getBeansOfType(Plugin.class);
        doSettingAwareReload(false);
    }

    /**
     * Actually executes reload activities
     */
    @SuppressWarnings("unchecked")
	@Override
    protected void doActualReload() {
        for (Map.Entry<String, Plugin> entry : pluginMap.entrySet()) {
            logger.debug("Processing bean {}", entry.getKey());
            Plugin plugin = entry.getValue();
            try {
                plugin.doActivate();
                if (plugin.getStatus().equals(PluginStatus.ACTIVE)) {
                    if (AssetManagementPlugin.class.isAssignableFrom(plugin.getClass())) {
                        assetManagementPlugin = (AssetManagementPlugin<? extends Container, ? extends Asset>) plugin;
                    }
                }
            } catch (PluginOperationException e) {
                logger.error("Unable to activate {}: cause {}", plugin.getName(), e.getMessage());
            }
        }
    }

    /**
     * Set-up service default settings
     */
    @Override
    protected void setDefaultSettings() {
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
}
