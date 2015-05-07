package ms.cms.plugin.mgmt.asset;

import ms.cms.plugin.mgmt.PluginImpl;

/**
 * AbstractAssetManagementPlugin
 * Created by bazzoni on 07/05/2015.
 */
public abstract class AbstractAssetManagementPlugin<C extends Container, A extends Asset> extends PluginImpl implements AssetManagementPlugin<C, A> {
}
