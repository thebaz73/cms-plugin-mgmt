package ms.cms.plugin.mgmt.asset;

/**
 * Container
 * Created by bazzoni on 07/05/2015.
 */
public interface Container {
    /**
     * Checks if container as children
     *
     * @return true is children number &gt; 0
     */
    boolean hasChildren();

    /**
     * Get children number
     *
     * @return children number
     */
    int getChildrenNumber();
}
