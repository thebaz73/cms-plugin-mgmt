package sparkle.cms.plugin.mgmt.search;

import sparkle.cms.plugin.mgmt.PluginImpl;
import sparkle.cms.plugin.mgmt.PluginOperationException;
import sparkle.cms.plugin.mgmt.PluginType;

/**
 * AbstractSearchPlugin
 * Created by bazzoni on 30/05/2015.
 */
public abstract class AbstractSearchPlugin<T extends SparkleDocument> extends PluginImpl implements SearchPlugin<T> {
    public AbstractSearchPlugin() {
        super(PluginType.SEARCH);
    }

    /**
     * Executes plugin default start up tasks
     *
     * @throws PluginOperationException if error
     */
    @Override
    public void doExecuteStartupTasks() throws PluginOperationException {
        //do nothing
    }

    /**
     * Executes plugin default shutdown tasks
     *
     * @throws PluginOperationException if error
     */
    @Override
    public void doExecuteShutdownTasks() throws PluginOperationException {
        //do nothing
    }
}
