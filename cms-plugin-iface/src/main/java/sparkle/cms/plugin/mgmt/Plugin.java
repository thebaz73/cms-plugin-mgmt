package sparkle.cms.plugin.mgmt;

/**
 * Plugin
 * Created by bazzoni on 06/05/2015.
 */
public interface Plugin {
    /**
     * Get plugin identification code
     *
     * @return id
     */
    String getId();

    /**
     * Get plugin name
     *
     * @return name
     */
    String getName();

    /**
     * Get actual status
     *
     * @return status
     */
    PluginStatus getStatus();

    /**
     * Get VERSION
     *
     * @return VERSION
     */
    String getVERSION();

    /**
     * Activates plugin
     *
     * @throws PluginOperationException if error
     */
    void doActivate() throws PluginOperationException;
}
