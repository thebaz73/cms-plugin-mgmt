package ms.cms.plugin.mgmt;

/**
 * Plugin
 * Created by bazzoni on 06/05/2015.
 */
public interface Plugin {
    /**
     * Get plugin identification code
     * @return id
     */
    public String getId();

    /**
     * Get plugin name
     * @return name
     */
    public String getName();

    /**
     * Get actual status
     * @return status
     */
    public PluginStatus getStatus();

    /**
     * Get VERSION
     * @return VERSION
     */
    public String getVERSION();

    /**
     * Activates plugin
     */
    public void doActivate();

    /**
     * Validates plugin
     */
    void doValidate();
}
