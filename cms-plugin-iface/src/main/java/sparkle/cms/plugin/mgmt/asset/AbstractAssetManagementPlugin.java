package sparkle.cms.plugin.mgmt.asset;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import sparkle.cms.data.CmsSiteRepository;
import sparkle.cms.domain.CmsSite;
import sparkle.cms.plugin.mgmt.PluginImpl;
import sparkle.cms.plugin.mgmt.PluginOperationException;

/**
 * AbstractAssetManagementPlugin
 * Created by bazzoni on 07/05/2015.
 */
public abstract class AbstractAssetManagementPlugin<C extends Container, A extends Asset> extends PluginImpl implements AssetManagementPlugin<C, A> {
	@Autowired
	private CmsSiteRepository cmsSiteRepository;
	
	@Override
	public void doExecuteDefaultTasks() throws PluginOperationException {
		List<CmsSite> cmsSites = cmsSiteRepository.findAll();
		for (CmsSite cmsSite : cmsSites) {
			if(findSiteRepository(cmsSite.getId()) == null) {
				createSiteRepository(cmsSite.getId());
			}
		}
	}
}
