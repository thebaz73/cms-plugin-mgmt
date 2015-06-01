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
     * Executes plugin default tasks
     *
     * @throws PluginOperationException if error
     */
    @Override
    public void doExecuteDefaultTasks() throws PluginOperationException {
        //do nothing
    }
}
