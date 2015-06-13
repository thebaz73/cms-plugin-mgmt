package sparkle.cms.plugin.mgmt;

import sparkle.cms.domain.CmsSetting;

import java.util.List;

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
     * Get plugin type
     *
     * @return type
     */
    PluginType getType();

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
     * Get Settings
     *
     * @return plugin settings
     */
    List<CmsSetting> getSettings();

    /**
     * Set filter
     *
     * @param filter filter
     */
    void setFilter(String filter);

    /**
     * Activates plugin
     *
     * @throws PluginOperationException if error
     */
    void doActivate() throws PluginOperationException;

    /**
     * Deactivates plugin
     *
     * @throws PluginOperationException if error
     */
    void doDeactivate() throws PluginOperationException;

    /**
     * Executes plugin default start up tasks
     *
     * @throws PluginOperationException if error
     */
    void doExecuteStartupTasks() throws PluginOperationException;

    /**
     * Executes plugin default shutdown tasks
     *
     * @throws PluginOperationException if error
     */
    void doExecuteShutdownTasks() throws PluginOperationException;
}
