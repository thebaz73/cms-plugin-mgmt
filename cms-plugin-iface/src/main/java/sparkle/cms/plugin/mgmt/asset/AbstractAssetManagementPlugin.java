package sparkle.cms.plugin.mgmt.asset;

import org.springframework.beans.factory.annotation.Autowired;
import sparkle.cms.data.CmsAssetRepository;
import sparkle.cms.data.CmsSiteRepository;
import sparkle.cms.domain.CmsSite;
import sparkle.cms.plugin.mgmt.PluginImpl;
import sparkle.cms.plugin.mgmt.PluginOperationException;
import sparkle.cms.plugin.mgmt.PluginType;

import java.util.List;

/**
 * AbstractAssetManagementPlugin
 * Created by bazzoni on 07/05/2015.
 */
public abstract class AbstractAssetManagementPlugin<C extends Container, A extends Asset> extends PluginImpl implements AssetManagementPlugin<C, A> {
	@Autowired
    protected CmsSiteRepository cmsSiteRepository;

    @Autowired
    protected CmsAssetRepository cmsAssetRepository;

    public AbstractAssetManagementPlugin() {
        super(PluginType.ASSET_MGMT);
    }

    /**
     * Executes plugin default start up tasks
     *
     * @throws PluginOperationException if error
     */
    @Override
    public void doExecuteStartupTasks() throws PluginOperationException {
        cmsAssetRepository.deleteAll();
        List<CmsSite> cmsSites = cmsSiteRepository.findAll();
		for (CmsSite cmsSite : cmsSites) {
            final C siteRepository = findSiteRepository(cmsSite.getId());
            if (siteRepository == null) {
                createSiteRepository(cmsSite.getId());
			} else {
                if (siteRepository.hasChildren()) {
                    loadChildren(cmsSite.getId(), siteRepository);
                }
            }
        }
	}

    /**
     * Executes plugin default shutdown tasks
     *
     * @throws PluginOperationException if error
     */
    @Override
    public void doExecuteShutdownTasks() throws PluginOperationException {
        finalizeObjects();
    }

    /**
     * Load all repository assets into central CMS database
     *
     * @param siteId         site id
     * @param siteRepository container repository
     * @throws PluginOperationException if error
     */
    protected abstract void loadChildren(String siteId, C siteRepository) throws PluginOperationException;

    /**
     * Executes specific finalization tasks
     *
     * @throws PluginOperationException
     */
    protected abstract void finalizeObjects() throws PluginOperationException;
}
